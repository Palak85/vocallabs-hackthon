package com.hackathon.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    @Builder.Default
    private boolean success = true;

    private String messageId;
    private String conversationId;

    private String response;
    private String answer;

    private String status; // RESOLVED, IN_PROGRESS, ESCALATED
    private boolean escalated;

    private NlpTelemetryDto nlp;

    @JsonProperty("message_id")
    public String getMessage_id() {
        return messageId;
    }

    @JsonProperty("conversation_id")
    public String getConversation_id() {
        return conversationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NlpTelemetryDto {
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

        @JsonProperty("frustration_score")
        private Integer frustrationScore;

        @JsonProperty("frustration_level")
        private String frustrationLevel;

        private String urgency;
        private Double urgencyConfidence;

        @JsonProperty("frustration_trend")
        private String frustrationTrend;

        private List<NlpAnalysisResponse.EntityItem> entities;
    }
}
