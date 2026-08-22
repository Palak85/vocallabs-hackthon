package com.raj.document_qna_assistant.entity;

import java.util.UUID;

public class MessageSource {
    private UUID id;
    private UUID messageId;
    private UUID chunkId;
    private double similarityScore;

    public MessageSource() {}

    public MessageSource(UUID id, UUID messageId, UUID chunkId, double similarityScore) {
        this.id = id;
        this.messageId = messageId;
        this.chunkId = chunkId;
        this.similarityScore = similarityScore;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getChunkId() {
        return chunkId;
    }

    public void setChunkId(UUID chunkId) {
        this.chunkId = chunkId;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
