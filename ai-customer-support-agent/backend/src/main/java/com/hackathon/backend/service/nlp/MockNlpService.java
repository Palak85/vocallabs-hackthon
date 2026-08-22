package com.hackathon.backend.service.nlp;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.NlpService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("mockNlpService")
@ConditionalOnProperty(name = "nlp.mock.enabled", havingValue = "true", matchIfMissing = true)
public class MockNlpService implements NlpService {

    private static final Pattern CLAIM_PATTERN = Pattern.compile("(?i)(CLM-[0-9A-Z]+)");
    private static final Pattern ORDER_PATTERN = Pattern.compile("(?i)(ORD-[0-9A-Z]+)");

    @Override
    public NlpAnalysisResponse analyze(String text) {
        return analyze(text, null, null);
    }

    @Override
    public NlpAnalysisResponse analyze(String text, String conversationId, String messageId) {
        String lower = text != null ? text.toLowerCase() : "";

        String domain = "general";
        double domainConfidence = 0.90;

        String intent = "general_inquiry";
        double intentConfidence = 0.85;

        String sentiment = "neutral";
        double sentimentConfidence = 0.80;

        String emotion = "neutral";
        double emotionConfidence = 0.80;

        int frustrationScore = 20;
        String frustrationLevel = "low";

        String urgencyLevel = "low";
        double urgencyConfidence = 0.75;

        Map<String, Object> entities = new HashMap<>();

        if (lower.contains("refund") || lower.contains("money back") || lower.contains("billing") || lower.contains("charge") || lower.contains("subscription")) {
            domain = "billing";
            domainConfidence = 0.96;
            intent = "refund_request";
            intentConfidence = 0.94;
            sentiment = "negative";
            sentimentConfidence = 0.92;
            emotion = "frustrated";
            emotionConfidence = 0.89;
            frustrationScore = 75;
            frustrationLevel = "medium";
            urgencyLevel = "high";
            urgencyConfidence = 0.88;
            entities.put("topic", "subscription");
        } else if (lower.contains("claim") || lower.contains("delay") || lower.contains("insurance") || lower.contains("policy")) {
            domain = "insurance";
            domainConfidence = 0.95;
            intent = "claim_delay";
            intentConfidence = 0.93;
            sentiment = "negative";
            sentimentConfidence = 0.96;
            emotion = "frustrated";
            emotionConfidence = 0.91;
            frustrationScore = 87;
            frustrationLevel = "high";
            urgencyLevel = "high";
            urgencyConfidence = 0.90;
            entities.put("claim_type", "auto_or_health");
        } else if (lower.contains("cancel") || lower.contains("stop service") || lower.contains("terminate")) {
            domain = "account";
            domainConfidence = 0.92;
            intent = "cancellation";
            intentConfidence = 0.91;
            sentiment = "negative";
            sentimentConfidence = 0.85;
            emotion = "disappointed";
            emotionConfidence = 0.82;
            frustrationScore = 60;
            frustrationLevel = "medium";
            urgencyLevel = "medium";
            urgencyConfidence = 0.80;
        } else if (lower.contains("angry") || lower.contains("terrible") || lower.contains("awful") || lower.contains("horrible") || lower.contains("scam")) {
            sentiment = "negative";
            sentimentConfidence = 0.98;
            emotion = "angry";
            emotionConfidence = 0.95;
            frustrationScore = 95;
            frustrationLevel = "high";
            urgencyLevel = "high";
            urgencyConfidence = 0.95;
        }

        // Check regex for entities
        Matcher claimMatcher = CLAIM_PATTERN.matcher(text != null ? text : "");
        if (claimMatcher.find()) {
            entities.put("claim_number", claimMatcher.group(1));
        }

        Matcher orderMatcher = ORDER_PATTERN.matcher(text != null ? text : "");
        if (orderMatcher.find()) {
            entities.put("order_id", orderMatcher.group(1));
        }

        if (entities.isEmpty() && domain.equals("insurance")) {
            entities.put("claim_number", "CLM-12345");
        }

        NlpAnalysisResponse.NlpData nlpData = NlpAnalysisResponse.NlpData.builder()
                .language(new NlpAnalysisResponse.LabelConfidence("en", 0.99))
                .domain(new NlpAnalysisResponse.LabelConfidence(domain, domainConfidence))
                .intent(new NlpAnalysisResponse.LabelConfidence(intent, intentConfidence))
                .sentiment(new NlpAnalysisResponse.LabelConfidence(sentiment, sentimentConfidence))
                .emotion(new NlpAnalysisResponse.LabelConfidence(emotion, emotionConfidence))
                .frustration(new NlpAnalysisResponse.Frustration(frustrationScore, frustrationLevel))
                .urgency(new NlpAnalysisResponse.Urgency(urgencyLevel, urgencyConfidence))
                .entities(entities)
                .build();

        return NlpAnalysisResponse.builder()
                .success(true)
                .conversationId("conv_" + UUID.randomUUID().toString().substring(0, 8))
                .messageId("msg_" + UUID.randomUUID().toString().substring(0, 8))
                .nlp(nlpData)
                .conversationAnalysis(new NlpAnalysisResponse.ConversationAnalysis(
                        frustrationScore > 50 ? "increasing" : "stable"
                ))
                .build();
    }
}
