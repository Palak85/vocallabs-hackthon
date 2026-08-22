package com.raj.document_qna_assistant.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentDetailDto(
    UUID id,
    String tenantId,
    String title,
    String category,
    String filename,
    String contentType,
    long sizeBytes,
    String status,
    String errorMessage,
    Instant createdAt,
    Instant updatedAt
) {}
