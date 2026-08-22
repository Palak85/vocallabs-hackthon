package com.raj.document_qna_assistant.service.nlp;

import com.raj.document_qna_assistant.dto.NlpAnalysisRequest;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.ConversationAnalysis;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.Entity;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.Frustration;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.LabelConfidence;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.NlpDetails;
import com.raj.document_qna_assistant.dto.NlpAnalysisResponse.Urgency;
import com.raj.document_qna_assistant.service.NlpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("mockNlpService")
public class MockNlpService implements NlpService {

    private static final Logger log = LoggerFactory.getLogger(MockNlpService.class);

    private static final Pattern TXN_PATTERN = Pattern.compile("(?i)(TXN[A-Z0-9]+|\\b[0-9]{12}\\b|[0-9a-zA-Z.\\-_]+@[a-zA-Z]+)");
    private static final Pattern ORDER_PATTERN = Pattern.compile("(?i)(ORD[0-9]+|ORDER[0-9]+|#[0-9]+)");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(?i)(\\$\\d+(?:\\.\\d+)?|rs\\.?\\s*\\d+(?:\\.\\d+)?|inr\\s*\\d+(?:\\.\\d+)?|\\d+\\s*rupees?)");

    @Override
    public NlpAnalysisResponse analyze(NlpAnalysisRequest request) {
        String text = request != null && request.text() != null ? request.text().toLowerCase() : "";
        String convId = request != null && request.conversationId() != null ? request.conversationId() : "conv_default";
        String msgId = request != null && request.messageId() != null ? request.messageId() : "msg_default";

        log.info("MockNlpService analyzing message: '{}' (convId: {}, msgId: {})", text, convId, msgId);

        String domainLabel;
        double domainConf;
        String intentLabel;
        double intentConf;
        String sentimentLabel;
        double sentimentConf;
        String emotionLabel;
        double emotionConf;
        int frustrationScore;
        String frustrationLevel;
        String urgencyLevel;
        double urgencyConf;
        String trend = "stable";

        List<Entity> entities = extractEntities(request != null ? request.text() : "");

        if (text.contains("upi") || text.contains("transaction") || text.contains("deducted") || text.contains("bank") || text.contains("payment")) {
            domainLabel = "banking";
            domainConf = 0.96;
            if (text.contains("fail") || text.contains("deducted") || text.contains("stuck") || text.contains("error")) {
                intentLabel = "transaction_failed";
                intentConf = 0.94;
                sentimentLabel = "negative";
                sentimentConf = 0.91;
                emotionLabel = "frustrated";
                emotionConf = 0.88;
                frustrationScore = 72;
                frustrationLevel = "high";
                urgencyLevel = "medium";
                urgencyConf = 0.82;
                trend = "increasing";
            } else {
                intentLabel = "payment_inquiry";
                intentConf = 0.89;
                sentimentLabel = "neutral";
                sentimentConf = 0.80;
                emotionLabel = "neutral";
                emotionConf = 0.85;
                frustrationScore = 25;
                frustrationLevel = "low";
                urgencyLevel = "low";
                urgencyConf = 0.70;
            }
        } else if (text.contains("refund") || text.contains("return") || text.contains("money back") || text.contains("exchange")) {
            domainLabel = "e-commerce";
            domainConf = 0.95;
            intentLabel = "refund_request";
            intentConf = 0.93;
            sentimentLabel = "negative";
            sentimentConf = 0.85;
            emotionLabel = "frustrated";
            emotionConf = 0.82;
            frustrationScore = 65;
            frustrationLevel = "medium";
            urgencyLevel = "high";
            urgencyConf = 0.88;
            trend = "increasing";
        } else if (text.contains("login") || text.contains("password") || text.contains("reset") || text.contains("otp") || text.contains("account")) {
            domainLabel = "technical_support";
            domainConf = 0.92;
            intentLabel = "account_access_issue";
            intentConf = 0.90;
            sentimentLabel = "negative";
            sentimentConf = 0.80;
            emotionLabel = "annoyed";
            emotionConf = 0.78;
            frustrationScore = 55;
            frustrationLevel = "medium";
            urgencyLevel = "high";
            urgencyConf = 0.85;
        } else if (text.contains("cancel") || text.contains("terminate") || text.contains("unsubscribe")) {
            domainLabel = "subscription";
            domainConf = 0.94;
            intentLabel = "cancellation_request";
            intentConf = 0.92;
            sentimentLabel = "negative";
            sentimentConf = 0.82;
            emotionLabel = "neutral";
            emotionConf = 0.75;
            frustrationScore = 40;
            frustrationLevel = "low";
            urgencyLevel = "medium";
            urgencyConf = 0.75;
        } else if (text.contains("urgent") || text.contains("asap") || text.contains("immediately") || text.contains("help")) {
            domainLabel = "customer_service";
            domainConf = 0.90;
            intentLabel = "urgent_assistance";
            intentConf = 0.91;
            sentimentLabel = "negative";
            sentimentConf = 0.88;
            emotionLabel = "anxious";
            emotionConf = 0.84;
            frustrationScore = 70;
            frustrationLevel = "high";
            urgencyLevel = "high";
            urgencyConf = 0.95;
            trend = "increasing";
        } else {
            domainLabel = "general_inquiry";
            domainConf = 0.85;
            intentLabel = "information_lookup";
            intentConf = 0.82;
            sentimentLabel = "neutral";
            sentimentConf = 0.85;
            emotionLabel = "neutral";
            emotionConf = 0.88;
            frustrationScore = 15;
            frustrationLevel = "low";
            urgencyLevel = "low";
            urgencyConf = 0.60;
        }

        NlpDetails details = new NlpDetails(
                new LabelConfidence(detectLanguage(text), 0.99),
                new LabelConfidence(domainLabel, domainConf),
                new LabelConfidence(intentLabel, intentConf),
                new LabelConfidence(sentimentLabel, sentimentConf),
                new LabelConfidence(emotionLabel, emotionConf),
                new Frustration(frustrationScore, frustrationLevel),
                new Urgency(urgencyLevel, urgencyConf),
                entities
        );

        return new NlpAnalysisResponse(
                true,
                convId,
                msgId,
                details,
                new ConversationAnalysis(trend)
        );
    }

    private String detectLanguage(String text) {
        if (text == null || text.isBlank()) {
            return "en";
        }
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.DEVANAGARI) {
                return "hi";
            }
        }
        return "en";
    }

    private List<Entity> extractEntities(String rawText) {
        List<Entity> list = new ArrayList<>();
        if (rawText == null || rawText.isBlank()) {
            return list;
        }

        Matcher txnMatcher = TXN_PATTERN.matcher(rawText);
        if (txnMatcher.find()) {
            list.add(new Entity("transaction_id", txnMatcher.group(1), 0.95));
        }

        Matcher ordMatcher = ORDER_PATTERN.matcher(rawText);
        if (ordMatcher.find()) {
            list.add(new Entity("order_id", ordMatcher.group(1), 0.93));
        }

        Matcher amtMatcher = AMOUNT_PATTERN.matcher(rawText);
        if (amtMatcher.find()) {
            list.add(new Entity("amount", amtMatcher.group(1), 0.90));
        }

        return list;
    }
}
