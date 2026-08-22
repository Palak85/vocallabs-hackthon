package com.hackathon.backend.service.domain;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.decision.DecisionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralQueryHandler implements DomainHandler {

    @Override
    public String getDomain() {
        return "unknown";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        String msg = rawMessage != null ? rawMessage.trim().toLowerCase() : "";

        if (msg.matches("(?i)^(hi|hello|hey|good\\s*(morning|afternoon|evening)|namaste|kaise\\s*ho)[!.]*$")) {
            return new DomainResponse(
                    "Hello! How can I help you today?",
                    "RESOLVED",
                    false,
                    null
            );
        }

        if (msg.contains("what can you help") || msg.contains("what can you do") || msg.contains("help me with") || msg.contains("supported domains")) {
            return new DomainResponse(
                    "I can help with ecommerce (orders & returns), education (fees & admissions), insurance (claims & policies), banking (UPI & accounts), telecom (recharges & plans), travel (flights & tickets), and healthcare (appointments) support. Please tell me what you need help with.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        return new DomainResponse(
                "I can help with ecommerce, education, insurance, banking, telecom, travel, and healthcare support. Please tell me what you need help with.",
                "RESOLVED",
                false,
                null
        );
    }
}
