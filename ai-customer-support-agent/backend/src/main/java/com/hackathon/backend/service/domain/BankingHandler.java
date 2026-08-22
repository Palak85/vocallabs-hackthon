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
public class BankingHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "banking";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String transactionId = nlp != null ? nlp.getFirstEntityValue("transaction_id") : null;
        String accountNumber = nlp != null ? nlp.getFirstEntityValue("account_number") : null;

        if ("transaction_failed".equals(intent) || "upi_problem".equals(intent) || "duplicate_transaction".equals(intent)) {
            if (transactionId != null && !transactionId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkTransactionStatus(transactionId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkTransactionStatus");
            } else {
                return new DomainResponse(
                        "Your UPI or banking transaction appears to have encountered an issue. Please provide your transaction ID (e.g., TXN12345 or UPI reference) so I can verify its status.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("transaction_pending".equals(intent)) {
            if (transactionId != null && !transactionId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkTransactionStatus(transactionId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkTransactionStatus");
            } else {
                return new DomainResponse(
                        "I understand your transaction is pending. Please share your transaction reference ID so I can track the clearing status with our switch.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("account_statement".equals(intent)) {
            if (accountNumber != null && !accountNumber.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.getAccountStatement(accountNumber);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "getAccountStatement");
            } else {
                return new DomainResponse(
                        "I can help generate your bank account statement. Please provide your account number so I can send the statement to your registered email.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("emi_payment".equals(intent) || (rawMessage != null && rawMessage.toLowerCase().contains("emi"))) {
            return new DomainResponse(
                    "To pay your loan EMI this month, you can set up auto-debit from your savings account or make an instant payment via net banking / mobile app under the Loans & EMI section. Please provide your loan account number if you need specific schedule details.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        if ("card_problem".equals(intent)) {
            return new DomainResponse(
                    "For card security and unblocking assistance, please confirm the last 4 digits of your debit/credit card, or use our mobile banking app's 'Manage Cards' section for instant unblocking.",
                    "IN_PROGRESS",
                    false,
                    null
            );
        }

        if ("refund_request".equals(intent)) {
            if (transactionId != null && !transactionId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.processRefund(transactionId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "processRefund");
            } else {
                return new DomainResponse(
                        "To initiate or check the status of a transaction refund, please provide your original transaction reference ID.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        // General Banking Query
        return new DomainResponse(
                "I can assist with banking services including UPI transactions, account statements, loan EMIs, card services, and failed transfer reversals. Please let me know your specific banking inquiry.",
                "RESOLVED",
                false,
                null
        );
    }
}
