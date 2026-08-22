package com.raj.document_qna_assistant.entity;

import java.time.Instant;
import java.util.UUID;

public class Message {
    private UUID id;
    private UUID conversationId;
    private String role; // USER, ASSISTANT
    private String content;
    private int tokenCount;
    private String model;
    private Long latencyMs;
    private Instant createdAt;

    public Message() {}

    public Message(UUID id, UUID conversationId, String role, String content, int tokenCount, String model, Long latencyMs, Instant createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.tokenCount = tokenCount;
        this.model = model;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
