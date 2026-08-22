package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.MessageDto;
import com.raj.document_qna_assistant.dto.MonitoringConversationDto;
import com.raj.document_qna_assistant.dto.MonitoringDetailDto;
import com.raj.document_qna_assistant.dto.MonitoringStatsDto;
import com.raj.document_qna_assistant.dto.SourceDto;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.Message;
import com.raj.document_qna_assistant.repository.ConversationRepository;
import com.raj.document_qna_assistant.repository.MessageRepository;
import com.raj.document_qna_assistant.util.TokenEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MonitoringService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringService.class);

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MonitoringService(ConversationRepository conversationRepository,
                             MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    public List<MonitoringConversationDto> listMonitoredConversations() {
        String tenantId = requireTenant();
        List<Conversation> conversations = conversationRepository.findAllByTenantId(tenantId);

        List<MonitoringConversationDto> dtos = new ArrayList<>();
        for (Conversation conv : conversations) {
            List<Message> messages = messageRepository.findAllByConversationId(conv.getId());
            int msgCount = messages.size();
            String lastSnippet = msgCount > 0 ? truncate(messages.get(msgCount - 1).getContent(), 80) : "";

            dtos.add(new MonitoringConversationDto(
                    conv.getId(),
                    conv.getTitle(),
                    conv.getMode(),
                    conv.getAssignedAgent(),
                    conv.getEscalationStatus(),
                    conv.getEscalationReason(),
                    conv.getLastFrustrationScore(),
                    conv.getLastFrustrationLevel(),
                    conv.getLastSentiment(),
                    conv.getLastEmotion(),
                    conv.getLastIntent(),
                    conv.getLastDomain(),
                    conv.getCallStatus(),
                    msgCount,
                    lastSnippet,
                    conv.getCreatedAt(),
                    conv.getUpdatedAt()
            ));
        }
        return dtos;
    }

    public MonitoringDetailDto getConversationDetail(UUID conversationId) {
        String tenantId = requireTenant();
        Conversation conv = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        List<Message> messages = messageRepository.findAllByConversationId(conversationId);
        List<MessageDto> messageDtos = messages.stream().map(msg -> {
            List<SourceDto> sources = "ASSISTANT".equalsIgnoreCase(msg.getRole())
                    ? messageRepository.findSourcesForMessage(msg.getId()) : List.<SourceDto>of();
            return new MessageDto(
                    msg.getId(),
                    msg.getRole(),
                    msg.getContent(),
                    sources,
                    msg.getCreatedAt()
            );
        }).toList();

        return new MonitoringDetailDto(
                conv.getId(),
                conv.getTitle(),
                conv.getMode(),
                conv.getAssignedAgent(),
                conv.getEscalationStatus(),
                conv.getEscalationReason(),
                conv.getLastFrustrationScore(),
                conv.getLastFrustrationLevel(),
                conv.getLastSentiment(),
                conv.getLastEmotion(),
                conv.getLastIntent(),
                conv.getLastDomain(),
                conv.getCallStatus(),
                conv.getCreatedAt(),
                conv.getUpdatedAt(),
                messageDtos
        );
    }

    @Transactional
    public MonitoringConversationDto takeoverConversation(UUID conversationId, String agentName) {
        String tenantId = requireTenant();
        Conversation conv = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        String assignedAgent = (agentName != null && !agentName.isBlank()) ? agentName : "Live Support Supervisor";

        conv.setMode("HUMAN");
        conv.setAssignedAgent(assignedAgent);
        conv.setEscalationStatus("ESCALATED");
        conv.setUpdatedAt(Instant.now());
        conversationRepository.save(conv);

        // Record handover system notification in transcript
        String handoverNotice = "Support Supervisor (" + assignedAgent + ") has taken over the live conversation.";
        Message noticeMsg = new Message(
                UUID.randomUUID(),
                conversationId,
                "SYSTEM",
                handoverNotice,
                TokenEstimator.estimateTokens(handoverNotice),
                "system",
                0L,
                Instant.now()
        );
        messageRepository.save(noticeMsg);

        log.info("Conversation {} successfully transferred to human agent: {}", conversationId, assignedAgent);
        return getSummaryDto(conv, conversationId);
    }

    @Transactional
    public MonitoringConversationDto handbackConversation(UUID conversationId) {
        String tenantId = requireTenant();
        Conversation conv = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        conv.setMode("AI");
        conv.setAssignedAgent(null);
        conv.setEscalationStatus("RESOLVED");
        conv.setUpdatedAt(Instant.now());
        conversationRepository.save(conv);

        String notice = "Live agent has ended the handover. AI Assistant is now active.";
        Message noticeMsg = new Message(
                UUID.randomUUID(),
                conversationId,
                "SYSTEM",
                notice,
                TokenEstimator.estimateTokens(notice),
                "system",
                0L,
                Instant.now()
        );
        messageRepository.save(noticeMsg);

        log.info("Conversation {} successfully returned to AI mode", conversationId);
        return getSummaryDto(conv, conversationId);
    }

    @Transactional
    public MessageDto sendAgentMessage(UUID conversationId, String agentName, String messageText) {
        String tenantId = requireTenant();
        Conversation conv = conversationRepository.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        UUID msgId = UUID.randomUUID();
        String sender = (agentName != null && !agentName.isBlank()) ? agentName : "Live Agent";
        int tokens = TokenEstimator.estimateTokens(messageText);

        Message agentMsg = new Message(
                msgId,
                conversationId,
                "AGENT",
                messageText,
                tokens,
                "human:" + sender,
                0L,
                Instant.now()
        );
        messageRepository.save(agentMsg);

        conv.setUpdatedAt(Instant.now());
        conversationRepository.save(conv);

        return new MessageDto(
                msgId,
                "AGENT",
                messageText,
                List.of(),
                Instant.now()
        );
    }

    public MonitoringStatsDto getMonitoringStats() {
        String tenantId = requireTenant();
        List<Conversation> conversations = conversationRepository.findAllByTenantId(tenantId);
        int total = conversations.size();
        int aiCount = conversationRepository.countByTenantIdAndMode(tenantId, "AI");
        int humanCount = conversationRepository.countByTenantIdAndMode(tenantId, "HUMAN");
        int escalationRec = conversationRepository.countByTenantIdAndEscalationStatus(tenantId, "RECOMMENDED");
        int escalated = conversationRepository.countByTenantIdAndEscalationStatus(tenantId, "ESCALATED");
        double avgFrustration = conversationRepository.getAverageFrustrationScore(tenantId);

        return new MonitoringStatsDto(
                total,
                aiCount,
                humanCount,
                escalationRec,
                escalated,
                avgFrustration
        );
    }

    private MonitoringConversationDto getSummaryDto(Conversation conv, UUID conversationId) {
        List<Message> messages = messageRepository.findAllByConversationId(conversationId);
        int msgCount = messages.size();
        String lastSnippet = msgCount > 0 ? truncate(messages.get(msgCount - 1).getContent(), 80) : "";

        return new MonitoringConversationDto(
                conv.getId(),
                conv.getTitle(),
                conv.getMode(),
                conv.getAssignedAgent(),
                conv.getEscalationStatus(),
                conv.getEscalationReason(),
                conv.getLastFrustrationScore(),
                conv.getLastFrustrationLevel(),
                conv.getLastSentiment(),
                conv.getLastEmotion(),
                conv.getLastIntent(),
                conv.getLastDomain(),
                conv.getCallStatus(),
                msgCount,
                lastSnippet,
                conv.getCreatedAt(),
                conv.getUpdatedAt()
        );
    }

    private String requireTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null || tenantId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant ID context missing");
        }
        return tenantId;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }
}
