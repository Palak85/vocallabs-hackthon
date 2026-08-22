package com.raj.document_qna_assistant.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageDto(
    UUID id,
    String role,
    String content,
    List<SourceDto> sources,
    Instant createdAt
) {}
