package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonitoringDetailDto(
        UUID id,
        String title,
        String mode,
        String assignedAgent,
        String escalationStatus,
        String escalationReason,
        Integer frustrationScore,
        String frustrationLevel,
        String sentiment,
        String emotion,
        String intent,
        String domain,
        String callStatus,
        Instant createdAt,
        Instant updatedAt,
        List<MessageDto> messages
) {}
