package com.hackathon.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpAnalysisResponse {

    private boolean success;

    @JsonProperty("conversation_id")
    private String conversationId;

    @JsonProperty("message_id")
    private String messageId;

    private NlpData nlp;

    @JsonProperty("conversation_analysis")
    private ConversationAnalysis conversationAnalysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NlpData {
        private LabelConfidence language;
        private LabelConfidence domain;
        private LabelConfidence intent;
        private LabelConfidence sentiment;
        private LabelConfidence emotion;
        private Frustration frustration;
        private Urgency urgency;
        private Map<String, Object> entities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabelConfidence {
        private String label;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Frustration {
        private Integer score;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Urgency {
        private String level;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationAnalysis {
        @JsonProperty("frustration_trend")
        private String frustrationTrend;
    }
}
