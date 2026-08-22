package com.hackathon.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NlpData {
        private LabelConfidence language;
        private LabelConfidence domain;
        private LabelConfidence intent;
        private LabelConfidence sentiment;
        private LabelConfidence emotion;
        private Frustration frustration;
        private Urgency urgency;
        private Object entities;

        @SuppressWarnings("unchecked")
        public List<EntityItem> getEntityList() {
            List<EntityItem> result = new ArrayList<>();
            if (entities instanceof List) {
                for (Object item : (List<?>) entities) {
                    if (item instanceof Map) {
                        Map<String, Object> map = (Map<String, Object>) item;
                        String type = map.get("type") != null ? map.get("type").toString() : "";
                        String value = map.get("value") != null ? map.get("value").toString() : "";
                        Double conf = 1.0;
                        if (map.get("confidence") instanceof Number) {
                            conf = ((Number) map.get("confidence")).doubleValue();
                        }
                        result.add(new EntityItem(type, value, conf));
                    } else if (item instanceof EntityItem) {
                        result.add((EntityItem) item);
                    }
                }
            } else if (entities instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) entities;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getValue() != null) {
                        result.add(new EntityItem(entry.getKey(), entry.getValue().toString(), 1.0));
                    }
                }
            }
            return result;
        }

        public String getFirstEntityValue(String type) {
            for (EntityItem item : getEntityList()) {
                if (item.getType() != null && item.getType().equalsIgnoreCase(type)) {
                    return item.getValue();
                }
            }
            return null;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EntityItem {
        private String type;
        private String value;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LabelConfidence {
        private String label;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Frustration {
        private Integer score;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Urgency {
        private String level;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConversationAnalysis {
        @JsonProperty("frustration_trend")
        private String frustrationTrend;

        @JsonProperty("current_frustration_score")
        private Integer currentFrustrationScore;

        @JsonProperty("previous_frustration_score")
        private Integer previousFrustrationScore;

        public ConversationAnalysis(String frustrationTrend) {
            this.frustrationTrend = frustrationTrend;
        }
    }
}
