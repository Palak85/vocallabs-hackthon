package com.raj.document_qna_assistant.dto;

public record SourceDto(
    String title,
    Integer pageNumber,
    Double similarityScore,
    String snippet
) {}
