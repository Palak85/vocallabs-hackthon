package com.raj.document_qna_assistant.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConversationDto(
    UUID id,
    String title,
    List<MessageDto> messages,
    Instant createdAt
) {}
