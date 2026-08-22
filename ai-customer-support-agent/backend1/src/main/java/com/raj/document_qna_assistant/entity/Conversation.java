package com.raj.document_qna_assistant.entity;

import java.time.Instant;
import java.util.UUID;

public class Conversation {
    private UUID id;
    private String tenantId;
    private String title;
    private Instant createdAt;
    private Instant updatedAt;

    public Conversation() {}

    public Conversation(UUID id, String tenantId, String title, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
