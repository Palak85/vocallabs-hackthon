package com.raj.document_qna_assistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.ChatRequest;
import com.raj.document_qna_assistant.dto.ChatResponse;
import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.ConversationAnalysis;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.Frustration;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.LabelConfidence;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.NlpDetails;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.Urgency;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.Message;
import com.raj.document_qna_assistant.repository.ConversationRepository;
import com.raj.document_qna_assistant.repository.MessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private ChatModel chatModel;

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private NlpService nlpService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private ObjectMapper objectMapper;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        TenantContext.setCurrentTenant("test-tenant");
        chatService = new ChatService(
                retrievalService,
                chatModel,
                conversationRepository,
                messageRepository,
                objectMapper,
                transactionTemplate,
                nlpService,
                eventPublisher
        );
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testChatWithNlpEnrichmentSuccess() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Title", Instant.now(), Instant.now());
        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));

        NlpAnalysisResponse nlpResponse = new NlpAnalysisResponse(
                true,
                convId.toString(),
                "msg_1",
                new NlpDetails(
                        new LabelConfidence("en", 0.99),
                        new LabelConfidence("banking", 0.95),
                        new LabelConfidence("transaction_failed", 0.92),
                        new LabelConfidence("negative", 0.90),
                        new LabelConfidence("frustrated", 0.85),
                        new Frustration(75, "high"),
                        new Urgency("high", 0.90),
                        List.of()
                ),
                new ConversationAnalysis("increasing")
        );
        when(nlpService.analyze(any(NlpAnalysisRequest.class))).thenReturn(nlpResponse);

        Document chunk = new Document("chunk_1", "UPI refund takes 24 to 48 hours to process.", Map.of("title", "UPI FAQ", "page_number", 1));
        when(retrievalService.retrieveChunks(anyString(), eq("test-tenant"), any(), anyInt(), anyDouble()))
                .thenReturn(List.of(chunk));

        org.springframework.ai.chat.model.ChatResponse aiChatResponse = new org.springframework.ai.chat.model.ChatResponse(
                List.of(new Generation(new AssistantMessage("Your failed transaction will be credited back within 48 hours.")))
        );
        when(chatModel.call(any(Prompt.class))).thenReturn(aiChatResponse);

        ChatRequest request = new ChatRequest(convId, "My UPI transaction failed and money got debited.", "cust_123", "banking");
        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("Your failed transaction will be credited back within 48 hours.", response.answer());
        assertEquals(convId, response.conversationId());
        assertEquals(1, response.sources().size());
        assertNotNull(response.nlp());
        assertEquals("transaction_failed", response.nlp().nlp().intent().label());

        verify(messageRepository, times(2)).save(any(Message.class));
    }

    @Test
    void testChatStreamEmitsNlpEscalationAlertAndTokens() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Stream Title", Instant.now(), Instant.now());
        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));

        NlpAnalysisResponse nlpResponse = new NlpAnalysisResponse(
                true,
                convId.toString(),
                "msg_stream",
                new NlpDetails(
                        new LabelConfidence("en", 0.99),
                        new LabelConfidence("banking", 0.95),
                        new LabelConfidence("transaction_failed", 0.92),
                        new LabelConfidence("negative", 0.90),
                        new LabelConfidence("frustrated", 0.85),
                        new Frustration(72, "high"),
                        new Urgency("medium", 0.80),
                        List.of()
                ),
                new ConversationAnalysis("increasing")
        );
        when(nlpService.analyze(any(NlpAnalysisRequest.class))).thenReturn(nlpResponse);

        Document chunk = new Document("chunk_2", "Payment troubleshooting steps...", Map.of("title", "Help", "page_number", 2));
        when(retrievalService.retrieveChunks(anyString(), eq("test-tenant"), any(), anyInt(), anyDouble()))
                .thenReturn(List.of(chunk));

        org.springframework.ai.chat.model.ChatResponse streamChunk1 = new org.springframework.ai.chat.model.ChatResponse(
                List.of(new Generation(new AssistantMessage("Hello! ")))
        );
        org.springframework.ai.chat.model.ChatResponse streamChunk2 = new org.springframework.ai.chat.model.ChatResponse(
                List.of(new Generation(new AssistantMessage("We are resolving your issue.")))
        );
        when(chatModel.stream(any(Prompt.class))).thenReturn(Flux.just(streamChunk1, streamChunk2));

        ChatRequest request = new ChatRequest(convId, "My payment is stuck", "cust_123", "support");
        Flux<ServerSentEvent<String>> stream = chatService.chatStream(request);

        List<ServerSentEvent<String>> events = stream.collectList().block();
        assertNotNull(events);
        // Events: nlp, escalation_alert (because score is 72 high), token 1, token 2, sources, done
        assertEquals(6, events.size());

        assertEquals("nlp", events.get(0).event());
        assertTrue(events.get(0).data().contains("transaction_failed"));

        assertEquals("escalation_alert", events.get(1).event());
        assertTrue(events.get(1).data().contains("recommended"));

        assertEquals("token", events.get(2).event());
        assertEquals("Hello! ", events.get(2).data());

        assertEquals("token", events.get(3).event());
        assertEquals("We are resolving your issue.", events.get(3).data());

        assertEquals("sources", events.get(4).event());
        assertEquals("done", events.get(5).event());
    }

    @Test
    void testChatStreamInHumanTakeoverMode() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Human Mode Title", Instant.now(), Instant.now());
        conv.setMode("HUMAN");
        conv.setAssignedAgent("Supervisor Alex");
        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));

        NlpAnalysisResponse nlpResponse = new NlpAnalysisResponse(
                true,
                convId.toString(),
                "msg_human",
                new NlpDetails(
                        new LabelConfidence("en", 0.99),
                        new LabelConfidence("general", 0.80),
                        new LabelConfidence("help", 0.85),
                        new LabelConfidence("neutral", 0.80),
                        new LabelConfidence("neutral", 0.80),
                        new Frustration(20, "low"),
                        new Urgency("low", 0.50),
                        List.of()
                ),
                new ConversationAnalysis("stable")
        );
        when(nlpService.analyze(any(NlpAnalysisRequest.class))).thenReturn(nlpResponse);

        ChatRequest request = new ChatRequest(convId, "Hello are you there?", "cust_123", null);
        Flux<ServerSentEvent<String>> stream = chatService.chatStream(request);

        List<ServerSentEvent<String>> events = stream.collectList().block();
        assertNotNull(events);
        assertEquals(3, events.size());

        assertEquals("nlp", events.get(0).event());
        assertEquals("human_agent_active", events.get(1).event());
        assertEquals("done", events.get(2).event());

        // LLM and Vector search should NOT be called
        verify(chatModel, never()).stream(any(Prompt.class));
        verify(retrievalService, never()).retrieveChunks(anyString(), anyString(), any(), anyInt(), anyDouble());
    }
}
