package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NlpAnalysisResponse(
        @JsonProperty("success") boolean success,
        @JsonProperty("conversation_id") String conversationId,
        @JsonProperty("message_id") String messageId,
        @JsonProperty("nlp") NlpDetails nlp,
        @JsonProperty("conversation_analysis") ConversationAnalysis conversationAnalysis
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NlpDetails(
            @JsonProperty("language") LabelConfidence language,
            @JsonProperty("domain") LabelConfidence domain,
            @JsonProperty("intent") LabelConfidence intent,
            @JsonProperty("sentiment") LabelConfidence sentiment,
            @JsonProperty("emotion") LabelConfidence emotion,
            @JsonProperty("frustration") Frustration frustration,
            @JsonProperty("urgency") Urgency urgency,
            @JsonProperty("entities") List<Entity> entities
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LabelConfidence(
            @JsonProperty("label") String label,
            @JsonProperty("confidence") Double confidence
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Frustration(
            @JsonProperty("score") Integer score,
            @JsonProperty("level") String level
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Urgency(
            @JsonProperty("level") String level,
            @JsonProperty("confidence") Double confidence
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entity(
            @JsonProperty("type") String type,
            @JsonProperty("value") String value,
            @JsonProperty("confidence") Double confidence
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConversationAnalysis(
            @JsonProperty("frustration_trend") String frustrationTrend
    ) {}
}
