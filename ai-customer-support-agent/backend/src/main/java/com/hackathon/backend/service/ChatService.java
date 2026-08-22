package com.hackathon.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.dto.ChatRequest;
import com.hackathon.backend.dto.ChatResponse;
import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.entity.CitationAnalytics;
import com.hackathon.backend.entity.Conversation;
import com.hackathon.backend.entity.LlmAnalytics;
import com.hackathon.backend.entity.Message;
import com.hackathon.backend.entity.NlpAnalytics;
import com.hackathon.backend.entity.RetrievalAnalytics;
import com.hackathon.backend.repository.CitationAnalyticsRepository;
import com.hackathon.backend.repository.ConversationRepository;
import com.hackathon.backend.repository.LlmAnalyticsRepository;
import com.hackathon.backend.repository.MessageRepository;
import com.hackathon.backend.repository.NlpAnalyticsRepository;
import com.hackathon.backend.repository.RetrievalAnalyticsRepository;
import com.hackathon.backend.service.decision.DecisionEngine;
import com.hackathon.backend.service.domain.DomainHandler;
import com.hackathon.backend.service.domain.DomainRouter;
import com.hackathon.backend.service.monitor.ContinuousMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NlpAnalyticsRepository nlpAnalyticsRepository;
    private final RetrievalAnalyticsRepository retrievalAnalyticsRepository;
    private final CitationAnalyticsRepository citationAnalyticsRepository;
    private final LlmAnalyticsRepository llmAnalyticsRepository;

    private final NlpService nlpService;
    private final RefusalService refusalService;
    private final ContinuousMonitor continuousMonitor;
    private final DecisionEngine decisionEngine;
    private final DomainRouter domainRouter;

    @Autowired(required = false)
    private VectorStore vectorStore;

    @Autowired(required = false)
    private ChatModel chatModel;

    private final ObjectMapper objectMapper;

    @Value("${rag.retrieval.top-k:4}")
    private int topK;

    @Value("${rag.retrieval.similarity-threshold:0.50}")
    private double similarityThreshold;

    @Value("${spring.ai.google.genai.chat.options.model:gemini-2.5-flash}")
    private String geminiModelName;

    @Transactional
    public ChatResponse processChat(ChatRequest request) {
        String originalMessage = request.getMessage() != null ? request.getMessage().trim() : "";
        if (originalMessage.isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty or blank");
        }

        // 1. Manage Conversation
        String conversationId = request.getConversationId();
        int turnCount = 1;
        if (conversationId == null || conversationId.isBlank() || !conversationRepository.existsById(conversationId)) {
            Conversation newConversation = Conversation.builder()
                    .id(conversationId != null && !conversationId.isBlank() ? conversationId : ("conv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12)))
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            conversationRepository.save(newConversation);
            conversationId = newConversation.getId();
        } else {
            conversationRepository.findById(conversationId).ifPresent(c -> {
                c.setUpdatedAt(LocalDateTime.now());
                conversationRepository.save(c);
            });
            turnCount = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).size() / 2 + 1;
        }

        // 2. Persist User Message Record
        String userMessageId = "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Message userMessage = Message.builder()
                .id(userMessageId)
                .conversationId(conversationId)
                .role("USER")
                .content(originalMessage)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(userMessage);

        log.info("[NLP REQUEST] conversation_id={}, message_id={}, text='{}'", conversationId, userMessageId, originalMessage);

        // 3. Perform Fresh NLP Analysis (FastAPI Microservice)
        NlpAnalysisResponse nlpAnalysis = null;
        try {
            nlpAnalysis = nlpService.analyze(originalMessage, conversationId, userMessageId);
        } catch (Exception e) {
            log.error("NLP analysis failed for messageId {}: {}", userMessageId, e.getMessage());
        }

        if (nlpAnalysis != null && nlpAnalysis.getNlp() != null) {
            var nlp = nlpAnalysis.getNlp();
            log.info("[NLP RESPONSE] domain={}, intent={}, sentiment={}, emotion={}, frustration={}/{}, urgency={}",
                    nlp.getDomain() != null ? nlp.getDomain().getLabel() : "unknown",
                    nlp.getIntent() != null ? nlp.getIntent().getLabel() : "unknown",
                    nlp.getSentiment() != null ? nlp.getSentiment().getLabel() : "neutral",
                    nlp.getEmotion() != null ? nlp.getEmotion().getLabel() : "neutral",
                    nlp.getFrustration() != null ? nlp.getFrustration().getScore() : 0,
                    nlp.getFrustration() != null ? nlp.getFrustration().getLevel() : "low",
                    nlp.getUrgency() != null ? nlp.getUrgency().getLevel() : "low"
            );
            saveNlpAnalytics(userMessageId, nlpAnalysis);
        }

        // 4. Refusal / Security Policy Check
        RefusalResult refusalResult = refusalService.evaluate(originalMessage, nlpAnalysis);
        if (!refusalResult.isAllowed()) {
            log.warn("Message {} was refused. Reason: {}", userMessageId, refusalResult.getReason());
            String assistantMessageId = "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            Message assistantMessage = Message.builder()
                    .id(assistantMessageId)
                    .conversationId(conversationId)
                    .role("ASSISTANT")
                    .content(refusalResult.getRefusalResponse())
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.save(assistantMessage);

            return ChatResponse.builder()
                    .success(true)
                    .messageId(assistantMessageId)
                    .conversationId(conversationId)
                    .response(refusalResult.getRefusalResponse())
                    .answer(refusalResult.getRefusalResponse())
                    .status("RESOLVED")
                    .escalated(false)
                    .nlp(buildTelemetryDto(nlpAnalysis))
                    .build();
        }

        // 5. Continuous Monitor Assessment
        ContinuousMonitor.HealthAssessment healthAssessment = continuousMonitor.assess(nlpAnalysis, turnCount, originalMessage);
        log.info("[CONVERSATION] turn_count={}, frustration_trend={}, risk_level={}",
                turnCount, healthAssessment.getFrustrationTrend(), healthAssessment.getRiskLevel());

        // 6. Decision Engine Evaluation
        DecisionEngine.DecisionResult decision = decisionEngine.evaluate(healthAssessment, nlpAnalysis, originalMessage);
        log.info("[DECISION] route={}, recommended_tool={}, escalated={}",
                healthAssessment.getDomain(), decision.getRecommendedTool(), decision.isEscalated());

        // 7. Handle Human Escalation
        if (decision.isEscalated()) {
            log.warn("[ESCALATION] Human escalation triggered for conversation {}. Reason: {}",
                    conversationId, decision.getEscalationReason());
            String assistantMessageId = "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            String escalationResponse = decision.getCustomerEscalationResponse();

            Message assistantMessage = Message.builder()
                    .id(assistantMessageId)
                    .conversationId(conversationId)
                    .role("ASSISTANT")
                    .content(escalationResponse)
                    .createdAt(LocalDateTime.now())
                    .build();
            messageRepository.save(assistantMessage);

            log.info("[FINAL RESPONSE] status=ESCALATED, response='{}'", escalationResponse);

            return ChatResponse.builder()
                    .success(true)
                    .messageId(assistantMessageId)
                    .conversationId(conversationId)
                    .response(escalationResponse)
                    .answer(escalationResponse)
                    .status("ESCALATED")
                    .escalated(true)
                    .nlp(buildTelemetryDto(nlpAnalysis))
                    .build();
        }

        // 8. Route to Dedicated Domain Handler
        DomainHandler domainHandler = domainRouter.route(healthAssessment.getDomain());
        DomainHandler.DomainResponse domainResponse = domainHandler.handle(originalMessage, nlpAnalysis, decision, turnCount);

        log.info("[TOOL] domain={}, tool_name={}, executed={}, result='{}'",
                domainHandler.getDomain(), domainResponse.toolName(), domainResponse.toolExecuted(), domainResponse.responseText());

        String finalResponseText = domainResponse.responseText();
        String finalStatus = domainResponse.resolutionStatus();

        // 9. Optional RAG Knowledge Base Search for Informational Queries
        if ("general_query".equalsIgnoreCase(healthAssessment.getIntent()) && vectorStore != null) {
            String enrichedQuery = buildEnrichedQuery(originalMessage, nlpAnalysis);
            try {
                SearchRequest searchRequest = SearchRequest.builder()
                        .query(enrichedQuery)
                        .topK(topK)
                        .similarityThreshold(similarityThreshold)
                        .build();
                List<org.springframework.ai.document.Document> retrievedDocs = vectorStore.similaritySearch(searchRequest);
                if (retrievedDocs != null && !retrievedDocs.isEmpty() && chatModel != null) {
                    Prompt prompt = buildGroundedPrompt(originalMessage, retrievedDocs);
                    org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);
                    if (aiResponse != null && aiResponse.getResult() != null && aiResponse.getResult().getOutput() != null) {
                        String ragAnswer = aiResponse.getResult().getOutput().getText();
                        if (ragAnswer != null && !ragAnswer.isBlank()) {
                            finalResponseText = ragAnswer;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("RAG retrieval skipped: {}", e.getMessage());
            }
        }

        // 10. Persist Assistant Response
        String assistantMessageId = "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        Message assistantMessage = Message.builder()
                .id(assistantMessageId)
                .conversationId(conversationId)
                .role("ASSISTANT")
                .content(finalResponseText)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMessage);

        log.info("[FINAL RESPONSE] status={}, response='{}'", finalStatus, finalResponseText);

        return ChatResponse.builder()
                .success(true)
                .messageId(assistantMessageId)
                .conversationId(conversationId)
                .response(finalResponseText)
                .answer(finalResponseText)
                .status(finalStatus)
                .escalated(false)
                .nlp(buildTelemetryDto(nlpAnalysis))
                .build();
    }

    private ChatResponse.NlpTelemetryDto buildTelemetryDto(NlpAnalysisResponse nlpAnalysis) {
        if (nlpAnalysis == null || nlpAnalysis.getNlp() == null) {
            return null;
        }
        var nlp = nlpAnalysis.getNlp();
        String trend = "stable";
        if (nlpAnalysis.getConversationAnalysis() != null) {
            trend = nlpAnalysis.getConversationAnalysis().getFrustrationTrend();
        }

        return ChatResponse.NlpTelemetryDto.builder()
                .language(nlp.getLanguage() != null ? nlp.getLanguage().getLabel() : "en")
                .languageConfidence(nlp.getLanguage() != null ? nlp.getLanguage().getConfidence() : 1.0)
                .domain(nlp.getDomain() != null ? nlp.getDomain().getLabel() : "unknown")
                .domainConfidence(nlp.getDomain() != null ? nlp.getDomain().getConfidence() : 0.0)
                .intent(nlp.getIntent() != null ? nlp.getIntent().getLabel() : "general_query")
                .intentConfidence(nlp.getIntent() != null ? nlp.getIntent().getConfidence() : 0.0)
                .sentiment(nlp.getSentiment() != null ? nlp.getSentiment().getLabel() : "neutral")
                .sentimentConfidence(nlp.getSentiment() != null ? nlp.getSentiment().getConfidence() : 0.0)
                .emotion(nlp.getEmotion() != null ? nlp.getEmotion().getLabel() : "neutral")
                .emotionConfidence(nlp.getEmotion() != null ? nlp.getEmotion().getConfidence() : 0.0)
                .frustrationScore(nlp.getFrustration() != null ? nlp.getFrustration().getScore() : 0)
                .frustrationLevel(nlp.getFrustration() != null ? nlp.getFrustration().getLevel() : "low")
                .urgency(nlp.getUrgency() != null ? nlp.getUrgency().getLevel() : "low")
                .urgencyConfidence(nlp.getUrgency() != null ? nlp.getUrgency().getConfidence() : 0.0)
                .frustrationTrend(trend)
                .entities(nlp.getEntityList())
                .build();
    }

    private String buildEnrichedQuery(String originalQuery, NlpAnalysisResponse nlpAnalysis) {
        if (nlpAnalysis == null || nlpAnalysis.getNlp() == null) {
            return originalQuery;
        }

        StringBuilder sb = new StringBuilder(originalQuery);
        var nlp = nlpAnalysis.getNlp();

        if (nlp.getIntent() != null && nlp.getIntent().getLabel() != null && !nlp.getIntent().getLabel().isBlank()) {
            sb.append(" ").append(nlp.getIntent().getLabel().replace("_", " "));
        }
        if (nlp.getDomain() != null && nlp.getDomain().getLabel() != null && !nlp.getDomain().getLabel().isBlank()) {
            sb.append(" ").append(nlp.getDomain().getLabel());
        }
        for (NlpAnalysisResponse.EntityItem item : nlp.getEntityList()) {
            if (item.getValue() != null && !item.getValue().isBlank()) {
                sb.append(" ").append(item.getValue());
            }
        }

        return sb.toString();
    }

    private Prompt buildGroundedPrompt(String userQuestion, List<org.springframework.ai.document.Document> retrievedDocs) {
        String contextText;
        if (retrievedDocs == null || retrievedDocs.isEmpty()) {
            contextText = "No relevant reference documents were found in the knowledge base.";
        } else {
            contextText = retrievedDocs.stream()
                    .map(org.springframework.ai.document.Document::getText)
                    .collect(Collectors.joining("\n\n---\n\n"));
        }

        String systemInstructions = "You are a knowledgeable and polite customer support AI assistant.\n\n"
                + "Instructions:\n"
                + "1. Answer the user's question clearly, concisely, and accurately based ONLY on the provided Context below.\n"
                + "2. If the answer cannot be found or deduced from the Context, inform the user politely that the information is not available in the documentation.\n"
                + "3. Do NOT invent information or assume facts not present in the Context.\n"
                + "4. Do NOT mention internal IDs, chunk numbers, or metadata tags to the user.\n\n"
                + "Context:\n" + contextText;

        SystemMessage systemMessage = new SystemMessage(systemInstructions);
        UserMessage userMessage = new UserMessage(userQuestion);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private void saveNlpAnalytics(String messageId, NlpAnalysisResponse nlpAnalysis) {
        try {
            var nlp = nlpAnalysis.getNlp();
            String entitiesJson = null;
            if (nlp.getEntities() != null) {
                entitiesJson = objectMapper.writeValueAsString(nlp.getEntities());
            }

            NlpAnalytics analytics = NlpAnalytics.builder()
                    .id("nlp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                    .messageId(messageId)
                    .language(nlp.getLanguage() != null ? nlp.getLanguage().getLabel() : null)
                    .languageConfidence(nlp.getLanguage() != null ? nlp.getLanguage().getConfidence() : null)
                    .domain(nlp.getDomain() != null ? nlp.getDomain().getLabel() : null)
                    .domainConfidence(nlp.getDomain() != null ? nlp.getDomain().getConfidence() : null)
                    .intent(nlp.getIntent() != null ? nlp.getIntent().getLabel() : null)
                    .intentConfidence(nlp.getIntent() != null ? nlp.getIntent().getConfidence() : null)
                    .sentiment(nlp.getSentiment() != null ? nlp.getSentiment().getLabel() : null)
                    .sentimentConfidence(nlp.getSentiment() != null ? nlp.getSentiment().getConfidence() : null)
                    .emotion(nlp.getEmotion() != null ? nlp.getEmotion().getLabel() : null)
                    .emotionConfidence(nlp.getEmotion() != null ? nlp.getEmotion().getConfidence() : null)
                    .frustrationScore(nlp.getFrustration() != null ? nlp.getFrustration().getScore() : null)
                    .frustrationLevel(nlp.getFrustration() != null ? nlp.getFrustration().getLevel() : null)
                    .urgencyLevel(nlp.getUrgency() != null ? nlp.getUrgency().getLevel() : null)
                    .urgencyConfidence(nlp.getUrgency() != null ? nlp.getUrgency().getConfidence() : null)
                    .entities(entitiesJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            nlpAnalyticsRepository.save(analytics);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize entities to JSON for messageId: {}", messageId, e);
        }
    }
}
