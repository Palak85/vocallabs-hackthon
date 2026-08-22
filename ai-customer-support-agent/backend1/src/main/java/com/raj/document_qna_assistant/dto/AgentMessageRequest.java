package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AgentMessageRequest(
        @NotBlank(message = "Message text cannot be blank")
        @JsonProperty("message")
        @JsonAlias({"message", "text", "content"})
        String message,

        @JsonProperty("agentName")
        @JsonAlias({"agentName", "agent_name", "sender"})
        String agentName
) {}
