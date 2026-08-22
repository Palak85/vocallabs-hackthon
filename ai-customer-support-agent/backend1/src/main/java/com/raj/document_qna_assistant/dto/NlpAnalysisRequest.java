package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record NlpAnalysisRequest(
        @JsonProperty("conversation_id") String conversationId,
        @JsonProperty("message_id") String messageId,
        @JsonProperty("customer_id") String customerId,
        @JsonProperty("text") String text
) {}
