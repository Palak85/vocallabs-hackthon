package com.hackathon.backend.service;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RefusalService {

    private static final List<String> PROHIBITED_KEYWORDS = List.of(
            "drop table",
            "select * from",
            "<script>",
            "exploit",
            "jailbreak",
            "override system prompt",
            "ignore previous instructions",
            "bypass security"
    );

    private static final List<String> BLOCKED_INTENTS = List.of(
            "malicious_exploit",
            "system_hack",
            "prompt_injection",
            "illegal_activity"
    );

    public RefusalResult evaluate(String message, NlpAnalysisResponse nlpAnalysis) {
        if (message == null || message.trim().isEmpty()) {
            return RefusalResult.refuse("EMPTY_MESSAGE", "Message cannot be empty.");
        }

        String lowerMessage = message.toLowerCase();

        // 1. Keyword check for prompt injection or exploits
        for (String prohibited : PROHIBITED_KEYWORDS) {
            if (lowerMessage.contains(prohibited)) {
                log.warn("Refusal triggered by prohibited keyword: '{}'", prohibited);
                return RefusalResult.refuse(
                        "PROHIBITED_KEYWORD",
                        "I cannot fulfill this request as it violates our security and safety policies."
                );
            }
        }

        // 2. NLP Intent-based policy check
        if (nlpAnalysis != null && nlpAnalysis.getNlp() != null && nlpAnalysis.getNlp().getIntent() != null) {
            String intent = nlpAnalysis.getNlp().getIntent().getLabel();
            if (intent != null && BLOCKED_INTENTS.contains(intent.toLowerCase())) {
                log.warn("Refusal triggered by blocked intent: '{}'", intent);
                return RefusalResult.refuse(
                        "BLOCKED_INTENT",
                        "I cannot assist with requests classified as unauthorized or malicious operations."
                );
            }
        }

        return RefusalResult.allow();
    }
}
