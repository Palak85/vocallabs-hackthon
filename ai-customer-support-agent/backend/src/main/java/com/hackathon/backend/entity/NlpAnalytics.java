package com.hackathon.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nlp_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpAnalytics {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "message_id", nullable = false, length = 64)
    private String messageId;

    private String language;
    private Double languageConfidence;

    private String domain;
    private Double domainConfidence;

    private String intent;
    private Double intentConfidence;

    private String sentiment;
    private Double sentimentConfidence;

    private String emotion;
    private Double emotionConfidence;

    private Integer frustrationScore;
    private String frustrationLevel;

    private String urgencyLevel;
    private Double urgencyConfidence;

    @Column(columnDefinition = "TEXT")
    private String entities;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
