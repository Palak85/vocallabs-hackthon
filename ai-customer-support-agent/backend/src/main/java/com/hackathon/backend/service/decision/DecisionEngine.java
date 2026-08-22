package com.hackathon.backend.service.decision;

import com.hackathon.backend.dto.NlpAnalysisResponse;
import com.hackathon.backend.service.monitor.ContinuousMonitor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DecisionEngine {

    public enum DecisionType {
        HUMAN_ESCALATION,
        AI_TOOL_ACTION,
        RAG_RESPONSE,
        AI_CONTINUE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionResult {
        private DecisionType decisionType;
        private boolean escalated;
        private String escalationReason;
        private String customerEscalationResponse;
        private String recommendedTool;
    }

    public DecisionResult evaluate(ContinuousMonitor.HealthAssessment assessment, NlpAnalysisResponse nlpAnalysis, String rawMessage) {
        // 1. Check Explicit Human Support Request
        if (assessment.isHumanSupportRequested()) {
            log.warn("Escalation triggered: Customer explicitly requested human assistance.");
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("HUMAN_SUPPORT_REQUEST")
                    .customerEscalationResponse("I understand. I am connecting you with a human support specialist right away. Please hold on while an agent joins this session.")
                    .build();
        }

        // 2. Critical Frustration / Urgency
        if ("critical".equalsIgnoreCase(assessment.getFrustrationLevel()) || assessment.getFrustrationScore() >= 85) {
            log.warn("Escalation triggered: Critical customer frustration (score={})", assessment.getFrustrationScore());
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("CRITICAL_FRUSTRATION")
                    .customerEscalationResponse("I sincerely apologize for the inconvenience and frustration this has caused. I have prioritized your case and am escalating this conversation to a senior support specialist immediately.")
                    .build();
        }

        if ("critical".equalsIgnoreCase(assessment.getUrgency())) {
            log.warn("Escalation triggered: Critical urgency level detected.");
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("CRITICAL_URGENCY")
                    .customerEscalationResponse("This matter has been flagged as critical urgency. I am immediately routing you to an on-duty specialist for expedited handling.")
                    .build();
        }

        // 3. High Frustration + Escalating Trend
        if (assessment.getFrustrationScore() >= 80 &&
                ("increasing".equalsIgnoreCase(assessment.getFrustrationTrend()) || "rapidly_increasing".equalsIgnoreCase(assessment.getFrustrationTrend()))) {
            log.warn("Escalation triggered: High frustration ({}) with accelerating trend ({})",
                    assessment.getFrustrationScore(), assessment.getFrustrationTrend());
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("ESCALATING_FRUSTRATION_TREND")
                    .customerEscalationResponse("I see that this issue remains unresolved and is causing continued trouble. To ensure this is resolved properly, I am transferring you directly to a human agent.")
                    .build();
        }

        // 4. Angry Emotion + High Frustration
        if ("angry".equalsIgnoreCase(assessment.getEmotion()) && assessment.getFrustrationScore() >= 70) {
            log.warn("Escalation triggered: Angry emotion with high frustration (score={})", assessment.getFrustrationScore());
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("ANGRY_EMOTION_SPIKE")
                    .customerEscalationResponse("I apologize for the trouble. I want to make sure this gets resolved correctly without further delay, so I am connecting you to a human agent now.")
                    .build();
        }

        // 5. Repeated Unresolved Turns with Negative Sentiment / Frustration
        if (assessment.getTurnCount() >= 4 && assessment.getFrustrationScore() >= 60 && "negative".equalsIgnoreCase(assessment.getSentiment())) {
            log.warn("Escalation triggered: Repeated unresolved turns (turns={}, score={})", assessment.getTurnCount(), assessment.getFrustrationScore());
            return DecisionResult.builder()
                    .decisionType(DecisionType.HUMAN_ESCALATION)
                    .escalated(true)
                    .escalationReason("REPEATED_UNRESOLVED_TURNS")
                    .customerEscalationResponse("Thank you for your patience. Since we haven't been able to fully resolve this yet, I am handing this over to a support representative to assist you directly.")
                    .build();
        }

        // 6. Check if a Business Tool can handle the request
        String toolName = determineTool(assessment.getDomain(), assessment.getIntent());
        if (toolName != null) {
            return DecisionResult.builder()
                    .decisionType(DecisionType.AI_TOOL_ACTION)
                    .escalated(false)
                    .recommendedTool(toolName)
                    .build();
        }

        // 7. Standard AI / RAG Path
        return DecisionResult.builder()
                .decisionType(DecisionType.RAG_RESPONSE)
                .escalated(false)
                .build();
    }

    private String determineTool(String domain, String intent) {
        if (domain == null || intent == null) return null;

        String d = domain.toLowerCase();
        String i = intent.toLowerCase();

        if (d.equals("ecommerce")) {
            if (i.equals("order_tracking") || i.equals("delivery_delay")) return "trackOrder";
            if (i.equals("order_cancellation")) return "cancelOrder";
            if (i.equals("refund_request") || i.equals("payment_failure")) return "processRefund";
        } else if (d.equals("banking")) {
            if (i.equals("transaction_failed") || i.equals("transaction_pending") || i.equals("upi_problem") || i.equals("duplicate_transaction")) {
                return "checkTransaction";
            }
            if (i.equals("account_statement") || i.equals("account_problem")) {
                return "getAccountStatement";
            }
        } else if (d.equals("education")) {
            if (i.equals("fee_payment") || i.equals("fee_payment_not_updated") || i.equals("fee_receipt") || i.equals("fee_refund")) {
                return "checkFeePayment";
            }
            if (i.equals("admission") || i.equals("scholarship") || i.equals("course_information")) {
                return "getAdmissionInfo";
            }
        } else if (d.equals("insurance")) {
            if (i.equals("claim_status") || i.equals("claim_delay") || i.equals("claim_rejection")) {
                return "checkClaimStatus";
            }
            if (i.equals("premium_payment") || i.equals("policy_renewal") || i.equals("policy_document") || i.equals("coverage_information")) {
                return "getPolicyDetails";
            }
        } else if (d.equals("telecom")) {
            if (i.equals("recharge_problem") || i.equals("recharge_not_updated")) {
                return "checkRechargeStatus";
            }
            if (i.equals("plan_information") || i.equals("plan_change") || i.equals("data_problem") || i.equals("network_problem")) {
                return "getPlanDetails";
            }
        } else if (d.equals("travel")) {
            if (i.equals("flight_delay") || i.equals("flight_cancellation") || i.equals("booking") || i.equals("booking_cancellation") || i.equals("refund")) {
                return "checkFlightBooking";
            }
        } else if (d.equals("healthcare")) {
            if (i.equals("appointment") || i.equals("appointment_cancellation")) {
                return "manageAppointment";
            }
        }

        return null;
    }
}
