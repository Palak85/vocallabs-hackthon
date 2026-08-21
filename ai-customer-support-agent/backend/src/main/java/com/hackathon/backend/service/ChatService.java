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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
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
        if (conversationId == null || conversationId.isBlank() || !conversationRepository.existsById(conversationId)) {
            Conversation newConversation = Conversation.builder()
                    .id("conv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
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

        // 3. Perform NLP Analysis
        NlpAnalysisResponse nlpAnalysis = null;
        try {
            nlpAnalysis = nlpService.analyze(originalMessage);
        } catch (Exception e) {
            log.error("NLP analysis failed for messageId {}: {}", userMessageId, e.getMessage());
            // Proceed gracefully or rethrow depending on requirements. In this case, we proceed with fallback.
        }

        // 4. Persist NLP Analytics
        if (nlpAnalysis != null && nlpAnalysis.getNlp() != null) {
            saveNlpAnalytics(userMessageId, nlpAnalysis);
        }

        // 5. Refusal / Policy Check
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
                    .messageId(assistantMessageId)
                    .conversationId(conversationId)
                    .answer(refusalResult.getRefusalResponse())
                    .build();
        }

        // 6. Query Enrichment for Vector Search (keeps original user message intact)
        String enrichedQuery = buildEnrichedQuery(originalMessage, nlpAnalysis);

        // 7. Vector Retrieval
        List<org.springframework.ai.document.Document> retrievedDocs = new ArrayList<>();
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(enrichedQuery)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();
            retrievedDocs = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.error("Vector retrieval failed for query '{}': {}", enrichedQuery, e.getMessage());
        }

        // 8. Grounded LLM Generation via Gemini / Spring AI
        String assistantMessageId = "msg_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long startTime = System.currentTimeMillis();
        String generatedAnswer;
        Long promptTokens = null;
        Long completionTokens = null;

        try {
            Prompt prompt = buildGroundedPrompt(originalMessage, retrievedDocs);
            org.springframework.ai.chat.model.ChatResponse aiResponse = chatModel.call(prompt);

            if (aiResponse != null && aiResponse.getResult() != null && aiResponse.getResult().getOutput() != null) {
                generatedAnswer = aiResponse.getResult().getOutput().getText();
                if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
                    var usage = aiResponse.getMetadata().getUsage();
                    promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens().longValue() : null;
                    completionTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens().longValue() : null;
                }
            } else {
                generatedAnswer = "I'm sorry, I was unable to generate a response at this time.";
            }
        } catch (Exception e) {
            log.error("Gemini LLM call failed for messageId {}: {}", userMessageId, e.getMessage());
            generatedAnswer = "I apologize, but I encountered an error while processing your request. Please try again later.";
        }
        long latency = System.currentTimeMillis() - startTime;

        // 9. Persist Assistant Message
        Message assistantMessage = Message.builder()
                .id(assistantMessageId)
                .conversationId(conversationId)
                .role("ASSISTANT")
                .content(generatedAnswer)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMessage);

        // 10. Persist Retrieval, Citation, and LLM Analytics
        logRetrievalAndCitations(assistantMessageId, enrichedQuery, retrievedDocs);
        saveLlmAnalytics(assistantMessageId, geminiModelName, latency, promptTokens, completionTokens);

        // 11. Return Final Response (Citations and internal metadata are hidden from frontend)
        return ChatResponse.builder()
                .messageId(assistantMessageId)
                .conversationId(conversationId)
                .answer(generatedAnswer)
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
        if (nlp.getEntities() != null && !nlp.getEntities().isEmpty()) {
            for (Map.Entry<String, Object> entry : nlp.getEntities().entrySet()) {
                if (entry.getValue() != null) {
                    sb.append(" ").append(entry.getValue());
                }
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

        String systemInstructions = """
                You are a knowledgeable and polite customer support AI assistant.
                
                Instructions:
                1. Answer the user's question clearly, concisely, and accurately based ONLY on the provided Context below.
                2. If the answer cannot be found or deduced from the Context, inform the user politely that the information is not available in the documentation.
                3. Do NOT invent information or assume facts not present in the Context.
                4. Do NOT mention internal IDs, chunk numbers, or metadata tags to the user.
                
                Context:
                """ + contextText;

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

    private void logRetrievalAndCitations(String assistantMessageId, String query, List<org.springframework.ai.document.Document> docs) {
        if (docs == null || docs.isEmpty()) {
            return;
        }

        for (int rank = 0; rank < docs.size(); rank++) {
            org.springframework.ai.document.Document doc = docs.get(rank);
            Map<String, Object> metadata = doc.getMetadata();

            String documentId = metadata != null && metadata.containsKey("documentId") ? String.valueOf(metadata.get("documentId")) : "unknown";
            String chunkId = doc.getId();
            Double score = doc.getScore();
            Integer pageNumber = null;
            if (metadata != null && metadata.containsKey("pageNumber")) {
                try {
                    pageNumber = Integer.parseInt(metadata.get("pageNumber").toString());
                } catch (NumberFormatException ignored) {
                }
            }

            // Retrieval log
            RetrievalAnalytics retrievalLog = RetrievalAnalytics.builder()
                    .id("ret_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                    .messageId(assistantMessageId)
                    .query(query)
                    .documentId(documentId)
                    .chunkId(chunkId)
                    .rank(rank + 1)
                    .similarityScore(score)
                    .createdAt(LocalDateTime.now())
                    .build();
            retrievalAnalyticsRepository.save(retrievalLog);

            // Citation log (Internal only)
            CitationAnalytics citationLog = CitationAnalytics.builder()
                    .id("cit_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                    .messageId(assistantMessageId)
                    .documentId(documentId)
                    .chunkId(chunkId)
                    .pageNumber(pageNumber)
                    .similarityScore(score)
                    .rank(rank + 1)
                    .createdAt(LocalDateTime.now())
                    .build();
            citationAnalyticsRepository.save(citationLog);
        }
    }

    private void saveLlmAnalytics(String messageId, String model, long latency, Long promptTokens, Long completionTokens) {
        LlmAnalytics llmAnalytics = LlmAnalytics.builder()
                .id("llm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12))
                .messageId(messageId)
                .model(model)
                .latency(latency)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .createdAt(LocalDateTime.now())
                .build();
        llmAnalyticsRepository.save(llmAnalytics);
    }
}
