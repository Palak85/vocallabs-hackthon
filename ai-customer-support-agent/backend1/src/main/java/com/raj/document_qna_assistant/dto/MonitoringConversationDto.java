package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonitoringConversationDto(
        UUID id,
        String title,
        String mode,                  // "AI" or "HUMAN"
        String assignedAgent,
        String escalationStatus,      // "NONE", "RECOMMENDED", "ESCALATED", "RESOLVED"
        String escalationReason,
        Integer frustrationScore,
        String frustrationLevel,
        String sentiment,
        String emotion,
        String intent,
        String domain,
        String callStatus,            // "ACTIVE", "IDLE", "ENDED"
        int messageCount,
        String lastMessageSnippet,
        Instant createdAt,
        Instant updatedAt
) {}
