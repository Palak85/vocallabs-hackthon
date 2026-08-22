package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.ConversationDto;
import com.raj.document_qna_assistant.dto.MessageDto;
import com.raj.document_qna_assistant.dto.SourceDto;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.Message;
import com.raj.document_qna_assistant.repository.ConversationRepository;
import com.raj.document_qna_assistant.repository.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Conversation createConversation(String title) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant ID context missing");
        }
        UUID id = UUID.randomUUID();
        Conversation conv = new Conversation(id, tenantId, title, Instant.now(), Instant.now());
        conversationRepository.save(conv);
        return conv;
    }

    public List<Conversation> listConversations() {
        String tenantId = TenantContext.getCurrentTenant();
        return conversationRepository.findAllByTenantId(tenantId);
    }

    public ConversationDto getConversationHistory(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        Conversation conv = conversationRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found"));

        List<Message> messages = messageRepository.findAllByConversationId(id);
        List<MessageDto> messageDtos = messages.stream().map(msg -> {
            List<SourceDto> sources = List.of();
            if ("ASSISTANT".equalsIgnoreCase(msg.getRole())) {
                sources = messageRepository.findSourcesForMessage(msg.getId());
            }
            return new MessageDto(msg.getId(), msg.getRole(), msg.getContent(), sources, msg.getCreatedAt());
        }).toList();

        return new ConversationDto(conv.getId(), conv.getTitle(), messageDtos, conv.getCreatedAt());
    }

    @Transactional
    public void deleteConversation(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        boolean deleted = conversationRepository.deleteByIdAndTenantId(id, tenantId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
    }
}
