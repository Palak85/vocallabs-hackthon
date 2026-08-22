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
public class TelecomHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "telecom";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String phoneNumber = nlp != null ? nlp.getFirstEntityValue("phone_number") : null;

        if ("recharge_not_updated".equals(intent) || "recharge_problem".equals(intent)) {
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkRechargeStatus(phoneNumber);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkRechargeStatus");
            } else {
                return new DomainResponse(
                        "I can help check why your recharge is not updated. Please provide your 10-digit mobile number.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("data_problem".equals(intent) || "network_problem".equals(intent)) {
            return new DomainResponse(
                    "If you are experiencing mobile data or network signal issues, please restart your phone or toggle Airplane mode. If the issue persists, please provide your mobile number so we can run a remote line diagnostic.",
                    "IN_PROGRESS",
                    false,
                    null
            );
        }

        if ("plan_information".equals(intent) || "plan_details".equals(intent) || "plan_change".equals(intent)) {
            BusinessToolService.ToolExecutionResult toolResult = businessToolService.getPlanDetails();
            return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "getPlanDetails");
        }

        // General Telecom Query
        return new DomainResponse(
                "I can assist with mobile recharges, high-speed 5G data packs, SIM porting (MNP), eSIM conversion, and network trouble tickets. Please tell me how I can help.",
                "RESOLVED",
                false,
                null
        );
    }
}
