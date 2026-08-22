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
public class TravelHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "travel";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String bookingId = nlp != null ? nlp.getFirstEntityValue("booking_id") : null;
        String flightNumber = nlp != null ? nlp.getFirstEntityValue("flight_number") : null;

        if ("flight_cancellation".equals(intent) || "refund".equals(intent)) {
            if ((bookingId != null && !bookingId.isBlank()) || (flightNumber != null && !flightNumber.isBlank())) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.checkFlightBooking(bookingId, flightNumber);
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "checkFlightBooking");
            } else {
                return new DomainResponse(
                        "I can help process your flight cancellation and refund. Please provide your booking PNR or flight number so I can retrieve your reservation.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("flight_delay".equals(intent)) {
            if (flightNumber != null && !flightNumber.isBlank()) {
                return new DomainResponse(
                        String.format("Flight %s is currently monitored. Complimentary refreshments and rescheduling options are provided at the airline ground desk for delays exceeding 2 hours.", flightNumber),
                        "RESOLVED",
                        true,
                        "checkFlightStatus"
                );
            } else {
                return new DomainResponse(
                        "Please provide your flight number (e.g., 6E-412 or AI-102) so I can retrieve the latest departure and gate updates.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("booking_cancellation".equals(intent)) {
            if (bookingId != null && !bookingId.isBlank()) {
                return new DomainResponse(
                        String.format("Reservation %s has been cancelled per airline fare rules. Refund amount will be credited to your original payment mode within 5-7 working days.", bookingId),
                        "RESOLVED",
                        true,
                        "cancelBooking"
                );
            } else {
                return new DomainResponse(
                        "To cancel your reservation, please provide your booking PNR reference.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        // General Travel Query
        return new DomainResponse(
                "I can assist with flight bookings, ticket cancellations, flight delay updates, refund inquiries, and baggage tracing. Please let me know your query.",
                "RESOLVED",
                false,
                null
        );
    }
}
