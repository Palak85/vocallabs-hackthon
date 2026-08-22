package com.raj.document_qna_assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.ChatRequest;
import com.raj.document_qna_assistant.dto.ChatResponse;
import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import com.raj.document_qna_assistant.dto.SourceDto;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.Message;
import com.raj.document_qna_assistant.repository.ConversationRepository;
import com.raj.document_qna_assistant.repository.MessageRepository;
import com.raj.document_qna_assistant.util.TokenEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final RetrievalService retrievalService;
    private final ChatModel chatModel;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final NlpService nlpService;

    @Value("${app.chat.top-k:5}")
    private int defaultTopK;

    @Value("${app.chat.similarity-threshold:0.62}")
    private double defaultThreshold;

    @Value("${app.chat.max-turns:6}")
    private int defaultMaxTurns;

    @Value("${app.chat.token-budget:3000}")
    private int tokenBudget;

    public ChatService(RetrievalService retrievalService,
                       ChatModel chatModel,
                       ConversationRepository conversationRepository,
                       MessageRepository messageRepository,
                       ObjectMapper objectMapper,
                       TransactionTemplate transactionTemplate,
                       NlpService nlpService) {
        this.retrievalService = retrievalService;
        this.chatModel = chatModel;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.objectMapper = objectMapper;
        this.transactionTemplate = transactionTemplate;
        this.nlpService = nlpService;
    }

    @Transactional
    public ChatResponse chat(ChatRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant ID context missing");
        }

        // 1. Resolve or create conversation
        UUID convId = request.conversationId();
        Conversation conv;
        if (convId == null) {
            convId = UUID.randomUUID();
            conv = new Conversation(convId, tenantId, truncateTitle(request.question()), Instant.now(), Instant.now());
            conversationRepository.save(conv);
        } else {
            conv = conversationRepository.findByIdAndTenantId(convId, tenantId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
        }

        // 2. Persist User Message
        UUID userMsgId = UUID.randomUUID();
        int userTokens = TokenEstimator.estimateTokens(request.question());
        Message userMsg = new Message(userMsgId, convId, "USER", request.question(), userTokens, null, null, Instant.now());
        messageRepository.save(userMsg);

        // 3. Perform NLP analysis
        String customerId = request.customerId() != null ? request.customerId() : "cust_anonymous";
        NlpAnalysisRequest nlpReq = new NlpAnalysisRequest(convId.toString(), userMsgId.toString(), customerId, request.question());
        NlpAnalysisResponse nlpResponse = nlpService.analyze(nlpReq);

        // 4. Update NLP metrics & evaluate escalation
        updateConversationNlpMetrics(conv, nlpResponse);

        // 5. Check if Human Takeover Mode is Active
        if ("HUMAN".equalsIgnoreCase(conv.getMode())) {
            String humanNotice = "A live support specialist is currently handling this session. Your message has been received by the agent.";
            return new ChatResponse(humanNotice, convId, List.of(), nlpResponse);
        }

        // 6. Query Enrichment: Combine question + NLP intent/domain/entities for vector search
        String enrichedQuery = buildEnrichedQuery(request.question(), nlpResponse);
        log.info("Enriched query for retrieval: '{}'", enrichedQuery);

        // 7. Retrieve chunks with enriched query
        List<org.springframework.ai.document.Document> chunks = retrievalService.retrieveChunks(
                enrichedQuery,
                tenantId,
                request.category(),
                defaultTopK,
                defaultThreshold
        );

        // 8. Refusal path if no chunks match threshold
        if (chunks.isEmpty()) {
            String refusalText = "not found in the available documents";
            UUID assistantMsgId = UUID.randomUUID();
            int assistantTokens = TokenEstimator.estimateTokens(refusalText);
            Message assistantMsg = new Message(assistantMsgId, convId, "ASSISTANT", refusalText, assistantTokens, null, 0L, Instant.now());
            messageRepository.save(assistantMsg);

            return new ChatResponse(refusalText, convId, List.of(), nlpResponse);
        }

        // 9. Format context & sources
        List<SourceDto> sources = formatSources(chunks);
        String contextString = formatContext(chunks);

        // 10. Build Prompt combining Context + NLP Insights + Customer Query + Bounded History
        Prompt prompt = buildPrompt(request.question(), contextString, nlpResponse, convId);

        // 11. Call LLM Model and measure latency
        long startTime = System.currentTimeMillis();
        var response = chatModel.call(prompt);
        long latency = System.currentTimeMillis() - startTime;

        String answer = response.getResult().getOutput().getText();

        // 12. Persist Assistant Message and Sources
        UUID assistantMsgId = UUID.randomUUID();
        int assistantTokens = TokenEstimator.estimateTokens(answer);
        Message assistantMsg = new Message(assistantMsgId, convId, "ASSISTANT", answer, assistantTokens, "gemini",
                latency, Instant.now());
        messageRepository.save(assistantMsg);

        saveSources(assistantMsgId, chunks, sources);

        conv.setUpdatedAt(Instant.now());
        conversationRepository.save(conv);

        return new ChatResponse(answer, convId, sources, nlpResponse);
    }

    public Flux<ServerSentEvent<String>> chatStream(ChatRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return Flux.just(ServerSentEvent.<String>builder("Tenant ID context missing")
                    .event("error")
                    .build());
        }

        try {
            // 1. Resolve or create conversation
            final UUID convId;
            Conversation conv;
            if (request.conversationId() == null) {
                convId = UUID.randomUUID();
                conv = new Conversation(convId, tenantId, truncateTitle(request.question()), Instant.now(), Instant.now());
                conversationRepository.save(conv);
            } else {
                convId = request.conversationId();
                conv = conversationRepository.findByIdAndTenantId(convId, tenantId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));
            }

            // 2. Persist User Message
            UUID userMsgId = UUID.randomUUID();
            int userTokens = TokenEstimator.estimateTokens(request.question());
            Message userMsg = new Message(userMsgId, convId, "USER", request.question(), userTokens, null, null, Instant.now());
            messageRepository.save(userMsg);

            // 3. Call NLP Service
            String customerId = request.customerId() != null ? request.customerId() : "cust_anonymous";
            NlpAnalysisRequest nlpReq = new NlpAnalysisRequest(convId.toString(), userMsgId.toString(), customerId, request.question());
            NlpAnalysisResponse nlpResponse = nlpService.analyze(nlpReq);

            // 4. Update Conversation with latest NLP metrics & Evaluate Escalation
            boolean escalationAlertTriggered = updateConversationNlpMetrics(conv, nlpResponse);

            // Emit NLP analysis event first
            String nlpJson = objectMapper.writeValueAsString(nlpResponse);
            ServerSentEvent<String> nlpEvent = ServerSentEvent.<String>builder(nlpJson).event("nlp").build();

            // Prepare optional escalation event
            List<ServerSentEvent<String>> initialEvents = new ArrayList<>();
            initialEvents.add(nlpEvent);

            if (escalationAlertTriggered) {
                Map<String, Object> alertData = new HashMap<>();
                alertData.put("recommended", true);
                alertData.put("reason", conv.getEscalationReason());
                alertData.put("frustrationScore", conv.getLastFrustrationScore());
                alertData.put("frustrationLevel", conv.getLastFrustrationLevel());
                alertData.put("emotion", conv.getLastEmotion());
                alertData.put("intent", conv.getLastIntent());

                String alertJson = objectMapper.writeValueAsString(alertData);
                initialEvents.add(ServerSentEvent.<String>builder(alertJson).event("escalation_alert").build());
            }

            // 5. Check if Human Agent Takeover is active
            if ("HUMAN".equalsIgnoreCase(conv.getMode())) {
                String humanNotice = "A live support specialist is currently handling this session. Your message has been delivered to the agent.";
                ServerSentEvent<String> humanActiveEvent = ServerSentEvent.<String>builder(humanNotice).event("human_agent_active").build();
                ServerSentEvent<String> doneEvent = ServerSentEvent.<String>builder("").event("done").build();

                initialEvents.add(humanActiveEvent);
                initialEvents.add(doneEvent);
                return Flux.fromIterable(initialEvents);
            }

            // 6. Query Enrichment
            String enrichedQuery = buildEnrichedQuery(request.question(), nlpResponse);
            log.info("Enriched stream query for retrieval: '{}'", enrichedQuery);

            // 7. Retrieve Chunks
            List<org.springframework.ai.document.Document> chunks = retrievalService.retrieveChunks(
                    enrichedQuery,
                    tenantId,
                    request.category(),
                    defaultTopK,
                    defaultThreshold
            );

            // Refusal Path
            if (chunks.isEmpty()) {
                String refusalText = "not found in the available documents";
                UUID assistantMsgId = UUID.randomUUID();
                int assistantTokens = TokenEstimator.estimateTokens(refusalText);
                Message assistantMsg = new Message(assistantMsgId, convId, "ASSISTANT", refusalText, assistantTokens, null, 0L, Instant.now());
                messageRepository.save(assistantMsg);

                initialEvents.add(ServerSentEvent.<String>builder(refusalText).event("token").build());
                initialEvents.add(ServerSentEvent.<String>builder("[]").event("sources").build());
                initialEvents.add(ServerSentEvent.<String>builder("").event("done").build());

                return Flux.fromIterable(initialEvents);
            }

            // 8. Format Sources & Context
            List<SourceDto> sources = formatSources(chunks);
            String contextString = formatContext(chunks);

            // 9. Build Grounded Prompt
            Prompt prompt = buildPrompt(request.question(), contextString, nlpResponse, convId);

            // 10. Stream tokens from LLM
            StringBuilder answerAccumulator = new StringBuilder();
            long startTime = System.currentTimeMillis();

            Flux<ServerSentEvent<String>> tokenStream = chatModel.stream(prompt)
                    .map(aiResponse -> {
                        String text = aiResponse.getResult() != null && aiResponse.getResult().getOutput() != null
                                ? aiResponse.getResult().getOutput().getText() : "";
                        if (text != null) {
                            answerAccumulator.append(text);
                        }
                        return ServerSentEvent.<String>builder(text != null ? text : "")
                                .event("token")
                                .build();
                    })
                    .doOnCancel(() -> log.info("SSE Client cancelled subscription for conversation: {}", convId))
                    .doOnComplete(() -> {
                        long latency = System.currentTimeMillis() - startTime;
                        String finalAnswer = answerAccumulator.toString();

                        transactionTemplate.executeWithoutResult(status -> {
                            UUID assistantMsgId = UUID.randomUUID();
                            int assistantTokens = TokenEstimator.estimateTokens(finalAnswer);
                            Message assistantMsg = new Message(assistantMsgId, convId, "ASSISTANT", finalAnswer,
                                    assistantTokens, "gemini", latency, Instant.now());
                            messageRepository.save(assistantMsg);

                            saveSources(assistantMsgId, chunks, sources);
                        });
                    });

            Mono<ServerSentEvent<String>> sourcesEvent = Mono.fromCallable(() -> {
                String sourcesJson = objectMapper.writeValueAsString(sources);
                return ServerSentEvent.<String>builder(sourcesJson)
                        .event("sources")
                        .build();
            });

            Mono<ServerSentEvent<String>> doneEvent = Mono.just(
                    ServerSentEvent.<String>builder("").event("done").build()
            );

            return Flux.concat(Flux.fromIterable(initialEvents), tokenStream, sourcesEvent, doneEvent)
                    .onErrorResume(err -> {
                        log.error("Error occurred during SSE stream generation", err);
                        return Flux.just(ServerSentEvent.<String>builder(err.getMessage()).event("error").build());
                    });

        } catch (Exception e) {
            log.error("Failed to initiate SSE stream", e);
            return Flux.just(ServerSentEvent.<String>builder(e.getMessage()).event("error").build());
        }
    }

    private boolean updateConversationNlpMetrics(Conversation conv, NlpAnalysisResponse nlpResponse) {
        if (nlpResponse == null || nlpResponse.nlp() == null) {
            return false;
        }

        var nlp = nlpResponse.nlp();
        int frustrationScore = (nlp.frustration() != null && nlp.frustration().score() != null) ? nlp.frustration().score() : 0;
        String frustrationLevel = (nlp.frustration() != null && nlp.frustration().level() != null) ? nlp.frustration().level() : "low";
        String sentiment = (nlp.sentiment() != null && nlp.sentiment().label() != null) ? nlp.sentiment().label() : "neutral";
        String emotion = (nlp.emotion() != null && nlp.emotion().label() != null) ? nlp.emotion().label() : "neutral";
        String intent = (nlp.intent() != null && nlp.intent().label() != null) ? nlp.intent().label() : "information_lookup";
        String domain = (nlp.domain() != null && nlp.domain().label() != null) ? nlp.domain().label() : "general";

        conv.setLastFrustrationScore(frustrationScore);
        conv.setLastFrustrationLevel(frustrationLevel);
        conv.setLastSentiment(sentiment);
        conv.setLastEmotion(emotion);
        conv.setLastIntent(intent);
        conv.setLastDomain(domain);

        boolean isFrustrated = frustrationScore >= 70 || "high".equalsIgnoreCase(frustrationLevel);
        boolean isEscalationTrigger = isFrustrated || ("negative".equalsIgnoreCase(sentiment) && "high".equalsIgnoreCase(nlp.urgency() != null ? nlp.urgency().level() : "low"));

        if (isEscalationTrigger && !"ESCALATED".equalsIgnoreCase(conv.getEscalationStatus())) {
            conv.setEscalationStatus("RECOMMENDED");
            conv.setEscalationReason("Customer frustration score is " + frustrationScore + " (" + frustrationLevel + ") on intent '" + intent + "'. AI recommends switching to a live agent.");
            conversationRepository.save(conv);
            return true;
        } else if (!"ESCALATED".equalsIgnoreCase(conv.getEscalationStatus())) {
            conv.setEscalationStatus("NONE");
            conv.setEscalationReason(null);
            conversationRepository.save(conv);
            return false;
        } else {
            conversationRepository.save(conv);
            return false;
        }
    }

    private String buildEnrichedQuery(String rawQuestion, NlpAnalysisResponse nlpResponse) {
        if (nlpResponse == null || nlpResponse.nlp() == null) {
            return rawQuestion;
        }

        StringBuilder sb = new StringBuilder(rawQuestion);
        var nlp = nlpResponse.nlp();

        if (nlp.domain() != null && nlp.domain().label() != null) {
            sb.append(" ").append(nlp.domain().label());
        }
        if (nlp.intent() != null && nlp.intent().label() != null) {
            sb.append(" ").append(nlp.intent().label().replace("_", " "));
        }
        if (nlp.entities() != null) {
            for (var entity : nlp.entities()) {
                if (entity.value() != null && !entity.value().isBlank()) {
                    sb.append(" ").append(entity.value());
                }
            }
        }

        return sb.toString().trim();
    }

    private Prompt buildPrompt(String question, String context, NlpAnalysisResponse nlpResponse, UUID convId) {
        List<org.springframework.ai.chat.messages.Message> promptMessages = new ArrayList<>();

        String domain = "general";
        String intent = "information_lookup";
        String sentiment = "neutral";
        String emotion = "neutral";
        String frustrationLevel = "low";
        int frustrationScore = 0;
        String urgency = "low";
        String entitiesSummary = "None";

        if (nlpResponse != null && nlpResponse.nlp() != null) {
            var nlp = nlpResponse.nlp();
            if (nlp.domain() != null && nlp.domain().label() != null) domain = nlp.domain().label();
            if (nlp.intent() != null && nlp.intent().label() != null) intent = nlp.intent().label();
            if (nlp.sentiment() != null && nlp.sentiment().label() != null) sentiment = nlp.sentiment().label();
            if (nlp.emotion() != null && nlp.emotion().label() != null) emotion = nlp.emotion().label();
            if (nlp.frustration() != null) {
                frustrationLevel = nlp.frustration().level() != null ? nlp.frustration().level() : "low";
                frustrationScore = nlp.frustration().score() != null ? nlp.frustration().score() : 0;
            }
            if (nlp.urgency() != null && nlp.urgency().level() != null) urgency = nlp.urgency().level();
            if (nlp.entities() != null && !nlp.entities().isEmpty()) {
                StringBuilder entSb = new StringBuilder();
                for (var e : nlp.entities()) {
                    entSb.append(e.type()).append(": ").append(e.value()).append("; ");
                }
                entitiesSummary = entSb.toString();
            }
        }

        String systemPrompt = """
            You are a helpful, empathetic, and knowledgeable customer support AI assistant.
            Answer the customer's question based strictly on the provided Context below.
            
            Customer Emotional & Intent Context:
            - Domain: %s
            - Intent: %s
            - Sentiment: %s
            - Emotion: %s
            - Frustration Level: %s (%d/100)
            - Urgency: %s
            - Detected Entities: %s
            
            Guidelines:
            1. If the customer is frustrated or the urgency is high, acknowledge their situation with empathy and provide concise, actionable assistance.
            2. If specific entities (e.g. Transaction ID, Order ID) are detected, reference them where appropriate in your solution.
            3. Ground your answers strictly in the Context below. If the context does not contain enough information to answer, respond with "not found in the available documents".
            4. Do not speculate or invent facts.
            
            Context:
            ---
            %s
            """.formatted(domain, intent, sentiment, emotion, frustrationLevel, frustrationScore, urgency, entitiesSummary, context);

        promptMessages.add(new SystemMessage(systemPrompt));

        // Append recent conversation history
        List<Message> history = messageRepository.findAllByConversationId(convId);
        List<org.springframework.ai.chat.messages.Message> historyMessagesToInclude = new ArrayList<>();
        int accumulatedTokens = 0;
        int maxMessages = defaultMaxTurns * 2;

        for (int i = history.size() - 2; i >= 0; i--) {
            Message msg = history.get(i);
            int estTokens = msg.getTokenCount();

            if (historyMessagesToInclude.size() >= maxMessages || accumulatedTokens + estTokens > tokenBudget) {
                break;
            }

            accumulatedTokens += estTokens;
            if ("USER".equalsIgnoreCase(msg.getRole())) {
                historyMessagesToInclude.add(0, new UserMessage(msg.getContent()));
            } else if ("ASSISTANT".equalsIgnoreCase(msg.getRole())) {
                historyMessagesToInclude.add(0, new AssistantMessage(msg.getContent()));
            } else if ("AGENT".equalsIgnoreCase(msg.getRole())) {
                historyMessagesToInclude.add(0, new AssistantMessage(msg.getContent()));
            }
        }
        promptMessages.addAll(historyMessagesToInclude);
        promptMessages.add(new UserMessage(question));

        return new Prompt(promptMessages);
    }

    private List<SourceDto> formatSources(List<org.springframework.ai.document.Document> chunks) {
        List<SourceDto> sources = new ArrayList<>();
        for (org.springframework.ai.document.Document chunk : chunks) {
            Map<String, Object> metadata = chunk.getMetadata();
            String title = (String) metadata.getOrDefault("title", "Unknown");
            Integer page = (Integer) metadata.get("page_number");
            Double score = chunk.getScore();
            String content = chunk.getText();
            sources.add(new SourceDto(title, page, score, content));
        }
        return sources;
    }

    private String formatContext(List<org.springframework.ai.document.Document> chunks) {
        StringBuilder contextBuilder = new StringBuilder();
        for (org.springframework.ai.document.Document chunk : chunks) {
            Map<String, Object> metadata = chunk.getMetadata();
            String title = (String) metadata.getOrDefault("title", "Unknown");
            Integer page = (Integer) metadata.get("page_number");
            String content = chunk.getText();

            contextBuilder.append("Source: ").append(title);
            if (page != null) {
                contextBuilder.append(", Page: ").append(page);
            }
            contextBuilder.append("\nContent:\n").append(content).append("\n---\n");
        }
        return contextBuilder.toString();
    }

    private void saveSources(UUID assistantMsgId, List<org.springframework.ai.document.Document> chunks, List<SourceDto> sources) {
        for (int i = 0; i < chunks.size(); i++) {
            org.springframework.ai.document.Document chunk = chunks.get(i);
            SourceDto src = sources.get(i);
            try {
                UUID chunkId = UUID.fromString(chunk.getId());
                messageRepository.saveSource(assistantMsgId, chunkId, src.similarityScore());
            } catch (Exception e) {
                // Ignore invalid UUID string conversions
            }
        }
    }

    private String truncateTitle(String question) {
        if (question == null) {
            return "New Conversation";
        }
        return question.length() > 50 ? question.substring(0, 47) + "..." : question;
    }
}
