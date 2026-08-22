package com.raj.document_qna_assistant.event;

import java.util.UUID;

public record MonitoringUpdateEvent(
    UUID conversationId,
    String eventType,
    Object payload
) {}
