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
@Table(name = "retrieval_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievalAnalytics {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "message_id", nullable = false, length = 64)
    private String messageId;

    @Column(columnDefinition = "TEXT")
    private String query;

    @Column(name = "document_id", length = 64)
    private String documentId;

    @Column(name = "chunk_id", length = 64)
    private String chunkId;

    @Column(name = "chunk_rank")
    private Integer rank;

    @Column(name = "similarity_score")
    private Double similarityScore;

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
