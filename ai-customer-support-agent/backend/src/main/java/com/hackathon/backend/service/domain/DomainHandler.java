package com.hackathon.backend.service.domain;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.decision.DecisionEngine;

public interface DomainHandler {
    String getDomain();
    DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount);

    record DomainResponse(
            String responseText,
            String resolutionStatus, // RESOLVED, IN_PROGRESS
            boolean toolExecuted,
            String toolName
    ) {}
}
