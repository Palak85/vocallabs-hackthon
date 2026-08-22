package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MonitoringStatsDto(
        int totalConversations,
        int activeAiConversations,
        int activeHumanConversations,
        int escalationRecommendedCount,
        int escalatedCount,
        double averageFrustrationScore
) {}
