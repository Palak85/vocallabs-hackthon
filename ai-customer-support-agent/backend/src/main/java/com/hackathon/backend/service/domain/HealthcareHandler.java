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
public class HealthcareHandler implements DomainHandler {

    private final BusinessToolService businessToolService;

    @Override
    public String getDomain() {
        return "healthcare";
    }

    @Override
    public DomainResponse handle(String rawMessage, NlpAnalysisResponse nlpAnalysis, DecisionEngine.DecisionResult decision, int turnCount) {
        var nlp = (nlpAnalysis != null) ? nlpAnalysis.getNlp() : null;
        String intent = (nlp != null && nlp.getIntent() != null && nlp.getIntent().getLabel() != null)
                ? nlp.getIntent().getLabel().toLowerCase()
                : "general_query";

        String doctorName = nlp != null ? nlp.getFirstEntityValue("doctor_name") : null;
        String date = nlp != null ? nlp.getFirstEntityValue("date") : null;

        if ("appointment_cancellation".equals(intent)) {
            if (doctorName != null && !doctorName.isBlank()) {
                BusinessToolService.ToolExecutionResult toolResult = businessToolService.manageAppointment(doctorName, date != null ? date : "scheduled date");
                return new DomainResponse(toolResult.getResponseText(), toolResult.getResolutionStatus(), true, "cancelAppointment");
            } else {
                return new DomainResponse(
                        "Please provide your doctor's name and scheduled appointment date so I can process the cancellation.",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("appointment".equals(intent)) {
            if (doctorName != null && !doctorName.isBlank()) {
                return new DomainResponse(
                        String.format("Appointment slots with %s are available tomorrow from 10:00 AM to 1:00 PM and 5:00 PM to 8:00 PM. Please confirm your preferred slot.", doctorName),
                        "RESOLVED",
                        false,
                        null
                );
            } else {
                return new DomainResponse(
                        "I can help schedule your doctor consultation. Please specify the doctor's name or medical department (e.g., Cardiology, Dermatology, Pediatrics).",
                        "IN_PROGRESS",
                        false,
                        null
                );
            }
        }

        if ("doctor_information".equals(intent)) {
            return new DomainResponse(
                    "Our specialist OPD operates Monday through Saturday, 9:00 AM to 7:00 PM. Video tele-consultation is also available through the clinic portal.",
                    "RESOLVED",
                    false,
                    null
            );
        }

        if ("report_query".equals(intent)) {
            return new DomainResponse(
                    "Diagnostic and blood test reports are uploaded to the patient portal within 24 hours of sample collection. Please provide your lab test order ID to check report readiness.",
                    "IN_PROGRESS",
                    false,
                    null
            );
        }

        // General Healthcare Query
        return new DomainResponse(
                "I can assist with clinic appointments, doctor schedules, diagnostic lab test reports, and emergency hospital contact information. Please let me know your healthcare inquiry.",
                "RESOLVED",
                false,
                null
        );
    }
}
