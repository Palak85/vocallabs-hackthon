package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatResponse(
        @JsonProperty("answer") String answer,
        @JsonProperty("conversationId") UUID conversationId,
        @JsonProperty("sources") List<SourceDto> sources,
        @JsonProperty("nlp") NlpAnalysisResponse nlp
) {
    public ChatResponse(String answer, UUID conversationId, List<SourceDto> sources) {
        this(answer, conversationId, sources, null);
    }
}
