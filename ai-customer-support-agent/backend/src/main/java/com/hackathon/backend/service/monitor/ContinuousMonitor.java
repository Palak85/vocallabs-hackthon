package com.hackathon.backend.service.monitor;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContinuousMonitor {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthAssessment {
        private int frustrationScore;
        private String frustrationLevel;
        private String frustrationTrend;
        private String sentiment;
        private String emotion;
        private String urgency;
        private String domain;
        private String intent;
        private int turnCount;
        private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL
        private boolean isHumanSupportRequested;
        private boolean isHighFrustrationSpike;
        private boolean isRapidlyEscalating;
    }

    public HealthAssessment assess(NlpAnalysisResponse nlpAnalysis, int turnCount, String userMessage) {
        if (nlpAnalysis == null || nlpAnalysis.getNlp() == null) {
            return HealthAssessment.builder()
                    .frustrationScore(0)
                    .frustrationLevel("low")
                    .frustrationTrend("stable")
                    .sentiment("neutral")
                    .emotion("neutral")
                    .urgency("low")
                    .domain("unknown")
                    .intent("general_query")
                    .turnCount(turnCount)
                    .riskLevel("LOW")
                    .build();
        }

        var nlp = nlpAnalysis.getNlp();
        int frustrationScore = nlp.getFrustration() != null && nlp.getFrustration().getScore() != null ? nlp.getFrustration().getScore() : 0;
        String frustrationLevel = nlp.getFrustration() != null && nlp.getFrustration().getLevel() != null ? nlp.getFrustration().getLevel().toLowerCase() : "low";
        String sentiment = nlp.getSentiment() != null && nlp.getSentiment().getLabel() != null ? nlp.getSentiment().getLabel().toLowerCase() : "neutral";
        String emotion = nlp.getEmotion() != null && nlp.getEmotion().getLabel() != null ? nlp.getEmotion().getLabel().toLowerCase() : "neutral";
        String urgency = nlp.getUrgency() != null && nlp.getUrgency().getLevel() != null ? nlp.getUrgency().getLevel().toLowerCase() : "low";
        String domain = nlp.getDomain() != null && nlp.getDomain().getLabel() != null ? nlp.getDomain().getLabel().toLowerCase() : "unknown";
        String intent = nlp.getIntent() != null && nlp.getIntent().getLabel() != null ? nlp.getIntent().getLabel().toLowerCase() : "general_query";

        String trend = "stable";
        if (nlpAnalysis.getConversationAnalysis() != null && nlpAnalysis.getConversationAnalysis().getFrustrationTrend() != null) {
            trend = nlpAnalysis.getConversationAnalysis().getFrustrationTrend().toLowerCase();
        }

        boolean isHumanRequested = "human_support_request".equalsIgnoreCase(intent)
                || (userMessage != null && (
                userMessage.toLowerCase().contains("talk to a human")
                        || userMessage.toLowerCase().contains("connect me to a human")
                        || userMessage.toLowerCase().contains("human agent")
                        || userMessage.toLowerCase().contains("speak with an agent")
                        || userMessage.toLowerCase().contains("customer executive")
        ));

        boolean isHighFrustration = frustrationScore >= 75 || "critical".equals(frustrationLevel) || "high".equals(frustrationLevel);
        boolean isRapidlyEscalating = "rapidly_increasing".equals(trend) || ("increasing".equals(trend) && frustrationScore >= 70);

        // Determine Risk Level
        String riskLevel = "LOW";
        if (isHumanRequested || "critical".equals(frustrationLevel) || frustrationScore >= 85 || "critical".equals(urgency)) {
            riskLevel = "CRITICAL";
        } else if (isHighFrustration || isRapidlyEscalating || "angry".equals(emotion) || "high".equals(urgency)) {
            riskLevel = "HIGH";
        } else if (frustrationScore >= 40 || "concerned".equals(emotion) || "medium".equals(urgency) || "negative".equals(sentiment)) {
            riskLevel = "MEDIUM";
        }

        HealthAssessment assessment = HealthAssessment.builder()
                .frustrationScore(frustrationScore)
                .frustrationLevel(frustrationLevel)
                .frustrationTrend(trend)
                .sentiment(sentiment)
                .emotion(emotion)
                .urgency(urgency)
                .domain(domain)
                .intent(intent)
                .turnCount(turnCount)
                .riskLevel(riskLevel)
                .isHumanSupportRequested(isHumanRequested)
                .isHighFrustrationSpike(isHighFrustration)
                .isRapidlyEscalating(isRapidlyEscalating)
                .build();

        log.info("ContinuousMonitor Assessment: risk={}, frustration={}/{}, trend={}, intent={}, turnCount={}",
                riskLevel, frustrationScore, frustrationLevel, trend, intent, turnCount);

        return assessment;
    }
}
