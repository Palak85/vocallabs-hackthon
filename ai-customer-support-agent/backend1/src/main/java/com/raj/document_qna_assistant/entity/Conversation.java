package com.raj.document_qna_assistant.entity;

import java.time.Instant;
import java.util.UUID;

public class Conversation {
    private UUID id;
    private String tenantId;
    private String title;
    private String mode = "AI"; // "AI" or "HUMAN"
    private String assignedAgent;
    private String escalationStatus = "NONE"; // "NONE", "RECOMMENDED", "ESCALATED", "RESOLVED"
    private String escalationReason;
    private Integer lastFrustrationScore = 0;
    private String lastFrustrationLevel = "low";
    private String lastSentiment = "neutral";
    private String lastEmotion = "neutral";
    private String lastIntent;
    private String lastDomain;
    private String callStatus = "ACTIVE"; // "ACTIVE", "IDLE", "ENDED"
    private Instant createdAt;
    private Instant updatedAt;

    public Conversation() {}

    public Conversation(UUID id, String tenantId, String title, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.mode = "AI";
        this.escalationStatus = "NONE";
        this.callStatus = "ACTIVE";
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

    public String getMode() {
        return mode != null ? mode : "AI";
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(String assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public String getEscalationStatus() {
        return escalationStatus != null ? escalationStatus : "NONE";
    }

    public void setEscalationStatus(String escalationStatus) {
        this.escalationStatus = escalationStatus;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public Integer getLastFrustrationScore() {
        return lastFrustrationScore != null ? lastFrustrationScore : 0;
    }

    public void setLastFrustrationScore(Integer lastFrustrationScore) {
        this.lastFrustrationScore = lastFrustrationScore;
    }

    public String getLastFrustrationLevel() {
        return lastFrustrationLevel != null ? lastFrustrationLevel : "low";
    }

    public void setLastFrustrationLevel(String lastFrustrationLevel) {
        this.lastFrustrationLevel = lastFrustrationLevel;
    }

    public String getLastSentiment() {
        return lastSentiment != null ? lastSentiment : "neutral";
    }

    public void setLastSentiment(String lastSentiment) {
        this.lastSentiment = lastSentiment;
    }

    public String getLastEmotion() {
        return lastEmotion != null ? lastEmotion : "neutral";
    }

    public void setLastEmotion(String lastEmotion) {
        this.lastEmotion = lastEmotion;
    }

    public String getLastIntent() {
        return lastIntent;
    }

    public void setLastIntent(String lastIntent) {
        this.lastIntent = lastIntent;
    }

    public String getLastDomain() {
        return lastDomain;
    }

    public void setLastDomain(String lastDomain) {
        this.lastDomain = lastDomain;
    }

    public String getCallStatus() {
        return callStatus != null ? callStatus : "ACTIVE";
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
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
