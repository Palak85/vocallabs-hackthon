package com.hackathon.backend.service.domain;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.decision.DecisionEngine;
import com.hackathon.backend.service.tools.BusinessToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "insurance";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String policyNumber = nlp != null ? nlp.getFirstEntityValue("policy_number") : null;
        String claimNumber = nlp != null ? nlp.getFirstEntityValue("claim_number") : null;

        if ("policy_expiry".equals(intent) || "policy_renewal".equals(intent) || "policy_document".equals(intent) || "coverage_information".equals(intent)) {
            if (policyNumber != null && !policyNumber.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.getPolicyDetails(policyNumber);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "getPolicyDetails");
            } else {
                return new DomainResponse(
                        "I can help with your policy expiry date and renewal details. Please provide your policy number so I can look up the policy details.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("claim_status".equals(intent) || "claim_delay".equals(intent) || "claim_rejection".equals(intent)) {
            if (claimNumber != null && !claimNumber.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkClaimStatus(claimNumber);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkClaimStatus");
            } else {
                return new DomainResponse(
                        "I can help check your insurance claim status. Please provide your claim number (e.g., CLM-12345) so I can retrieve the latest update.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("premium_payment".equals(intent)) {
            return new DomainResponse(
                    "You can pay your insurance premium online using Debit Card, Net Banking, or UPI via our customer portal. A 30-day grace period is available for premium renewals.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        // General Insurance Query
        return new DomainResponse(
                "I can assist with insurance claims, cashless network hospital locators, policy renewals, coverage benefits, and premium receipts. Please share your specific insurance query.",
                "RESOLVED",
                false,
                null
        );
    }
}
