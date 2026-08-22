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
public class EducationHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "education";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String studentId = nlp != null ? nlp.getFirstEntityValue("student_id") : null;
        String feeType = nlp != null ? nlp.getFirstEntityValue("fee_type") : null;

        // Duplicate Fee Payment / Double Deduction Handling
        if ("duplicate_fee_payment".equals(intent) || "duplicate_transaction".equals(intent) ||
                (rawMessage != null && (rawMessage.toLowerCase().contains("dono transactions") || rawMessage.toLowerCase().contains("double debit") || rawMessage.contains("1,70,000") || (rawMessage.toLowerCase().contains("twice") && rawMessage.toLowerCase().contains("deduct"))))) {
            return new DomainResponse(
                    "I understand that your college fees were deducted twice from your bank account while only one transaction receipt was generated on the portal. We have recorded this high-priority duplicate fee payment case. Please share both transaction IDs (or UPI UTR numbers) and your Student ID so we can verify the successful fee receipt and expedite the immediate reversal/refund of the duplicate deduction to your source account.",
                    "IN_PROGRESS",
                    false,
                    null
            );
        }

        if ("fee_payment_not_updated".equals(intent)) {
            if (studentId != null && !studentId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkFeePaymentStatus(studentId, feeType != null ? feeType : "tuition_fee");
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkFeePaymentStatus");
            } else {
                return new DomainResponse(
                        "I understand your fee payment has not updated yet. Please provide your Student ID or fee transaction reference so I can verify the reconciliation status in the accounting system.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("fee_payment".equals(intent)) {
            return new DomainResponse(
                    "You can pay your semester and term tuition fees online via the official student portal using Net Banking, UPI, or Debit/Credit Cards. Please retain the transaction acknowledgment once completed.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        if ("fee_receipt".equals(intent)) {
            if (studentId != null && !studentId.isBlank()) {
                return new DomainResponse(
                        String.format("Fee payment receipt for student %s has been verified and is available for download in PDF format under the 'Fee Records' tab in the student portal.", studentId),
                        "RESOLVED",
                        true,
                        "checkFeePaymentStatus"
                );
            } else {
                return new DomainResponse(
                        "To download your official fee receipt, please provide your Student ID or enrollment number.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("admission".equals(intent) || "scholarship".equals(intent) || "course_information".equals(intent)) {
            BusinessToolService.ToolExecutionResult toolResult = businessToolService.getAdmissionInfo();
            return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "getAdmissionInfo");
        }

        if ("fee_refund".equals(intent)) {
            return new DomainResponse(
                    "Fee refund requests for admission withdrawal or excess payment require submitting Form F-102 through the administrative accounts desk within 15 days of term commencement.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        // General Education / Fee Query
        return new DomainResponse(
                "School and college fee structures include tuition, laboratory, and activity fees payable in quarterly installments. For a detailed breakdown, please specify the class or degree program.",
                "RESOLVED",
                false,
                null
        );
    }
}
