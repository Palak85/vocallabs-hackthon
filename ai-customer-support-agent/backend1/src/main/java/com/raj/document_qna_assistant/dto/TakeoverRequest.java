package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record TakeoverRequest(
        @JsonProperty("agentName")
        @JsonAlias({"agentName", "agent_name", "adminId", "admin_id"})
        String agentName,

        @JsonProperty("notes")
        String notes
) {}
