package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.MessageDto;
import com.raj.document_qna_assistant.dto.MonitoringConversationDto;
import com.raj.document_qna_assistant.dto.MonitoringDetailDto;
import com.raj.document_qna_assistant.dto.MonitoringStatsDto;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.Message;
import com.raj.document_qna_assistant.repository.ConversationRepository;
import com.raj.document_qna_assistant.repository.MessageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private ConversationRepository conversationRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MonitoringService monitoringService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("test-tenant");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testListMonitoredConversations() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "UPI Issue", Instant.now(), Instant.now());
        conv.setLastFrustrationScore(75);
        conv.setLastFrustrationLevel("high");
        conv.setEscalationStatus("RECOMMENDED");

        Message msg = new Message(UUID.randomUUID(), convId, "USER", "My transaction failed", 5, null, null, Instant.now());

        when(conversationRepository.findAllByTenantId("test-tenant")).thenReturn(List.of(conv));
        when(messageRepository.findAllByConversationId(convId)).thenReturn(List.of(msg));

        List<MonitoringConversationDto> list = monitoringService.listMonitoredConversations();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(convId, list.get(0).id());
        assertEquals("RECOMMENDED", list.get(0).escalationStatus());
        assertEquals(75, list.get(0).frustrationScore());
        assertEquals(1, list.get(0).messageCount());
    }

    @Test
    void testGetConversationDetail() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Refund Query", Instant.now(), Instant.now());
        conv.setMode("AI");

        Message msg = new Message(UUID.randomUUID(), convId, "USER", "Where is my refund?", 5, null, null, Instant.now());

        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));
        when(messageRepository.findAllByConversationId(convId)).thenReturn(List.of(msg));

        MonitoringDetailDto detail = monitoringService.getConversationDetail(convId);

        assertNotNull(detail);
        assertEquals(convId, detail.id());
        assertEquals("Refund Query", detail.title());
        assertEquals(1, detail.messages().size());
    }

    @Test
    void testTakeoverConversation() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Escalated Issue", Instant.now(), Instant.now());
        conv.setMode("AI");

        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));
        when(messageRepository.findAllByConversationId(convId)).thenReturn(List.of());

        MonitoringConversationDto updated = monitoringService.takeoverConversation(convId, "Sarah Supervisor");

        assertNotNull(updated);
        assertEquals("HUMAN", conv.getMode());
        assertEquals("Sarah Supervisor", conv.getAssignedAgent());
        assertEquals("ESCALATED", conv.getEscalationStatus());

        verify(conversationRepository, times(1)).save(conv);
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testHandbackConversation() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Handback Issue", Instant.now(), Instant.now());
        conv.setMode("HUMAN");
        conv.setAssignedAgent("Sarah Supervisor");
        conv.setEscalationStatus("ESCALATED");

        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));
        when(messageRepository.findAllByConversationId(convId)).thenReturn(List.of());

        MonitoringConversationDto updated = monitoringService.handbackConversation(convId);

        assertNotNull(updated);
        assertEquals("AI", conv.getMode());
        assertNull(conv.getAssignedAgent());
        assertEquals("RESOLVED", conv.getEscalationStatus());

        verify(conversationRepository, times(1)).save(conv);
    }

    @Test
    void testSendAgentMessage() {
        UUID convId = UUID.randomUUID();
        Conversation conv = new Conversation(convId, "test-tenant", "Live Session", Instant.now(), Instant.now());
        conv.setMode("HUMAN");

        when(conversationRepository.findByIdAndTenantId(convId, "test-tenant")).thenReturn(Optional.of(conv));

        MessageDto msg = monitoringService.sendAgentMessage(convId, "Agent John", "Hello, I am looking into your account now.");

        assertNotNull(msg);
        assertEquals("AGENT", msg.role());
        assertEquals("Hello, I am looking into your account now.", msg.content());

        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testGetMonitoringStats() {
        when(conversationRepository.findAllByTenantId("test-tenant")).thenReturn(List.of(new Conversation()));
        when(conversationRepository.countByTenantIdAndMode("test-tenant", "AI")).thenReturn(5);
        when(conversationRepository.countByTenantIdAndMode("test-tenant", "HUMAN")).thenReturn(2);
        when(conversationRepository.countByTenantIdAndEscalationStatus("test-tenant", "RECOMMENDED")).thenReturn(1);
        when(conversationRepository.countByTenantIdAndEscalationStatus("test-tenant", "ESCALATED")).thenReturn(2);
        when(conversationRepository.getAverageFrustrationScore("test-tenant")).thenReturn(48.5);

        MonitoringStatsDto stats = monitoringService.getMonitoringStats();

        assertNotNull(stats);
        assertEquals(5, stats.activeAiConversations());
        assertEquals(2, stats.activeHumanConversations());
        assertEquals(1, stats.escalationRecommendedCount());
        assertEquals(2, stats.escalatedCount());
        assertEquals(48.5, stats.averageFrustrationScore());
    }
}
