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
public class EcommerceHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "ecommerce";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String orderId = nlp != null ? nlp.getFirstEntityValue("order_id") : null;

        if ("delivery_delay".equals(intent) || "order_tracking".equals(intent)) {
            if (orderId != null && !orderId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.trackOrder(orderId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "trackOrder");
            } else {
                return new DomainResponse(
                        "Sure, I can help track your order. Please provide your order ID.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("order_cancellation".equals(intent)) {
            if (orderId != null && !orderId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.cancelOrder(orderId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "cancelOrder");
            } else {
                return new DomainResponse(
                        "To cancel your shipment, please provide your order ID (e.g., ORD12345).",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("refund_request".equals(intent)) {
            if (orderId != null && !orderId.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.processRefund(orderId);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "processRefund");
            } else {
                return new DomainResponse(
                        "To check or initiate your order refund, please provide your order ID.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("damaged_product".equals(intent) || "wrong_product".equals(intent) || "return_request".equals(intent) || "replacement_request".equals(intent)) {
            if (orderId != null && !orderId.isBlank()) {
                return new DomainResponse(
                        String.format("A replacement pickup request has been initiated for order %s. Our courier partner will collect the item within 48 hours.", orderId),
                        "RESOLVED",
                        true,
                        "processReplacement"
                );
            } else {
                return new DomainResponse(
                        "I can help arrange a return or replacement. Please provide your order ID.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        // General Ecommerce Query
        return new DomainResponse(
                "I can assist with order tracking, shipment delivery updates, returns, cancellations, and payment refunds. Please share your order details.",
                "RESOLVED",
                false,
                null
        );
    }
}
