package com.raj.document_qna_assistant.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChatRequest(
        @JsonProperty("conversationId")
        @JsonAlias({"conversation_id", "conversationId"})
        UUID conversationId,

        @JsonProperty("question")
        @JsonAlias({"question", "text", "message"})
        String question,

        @JsonProperty("customerId")
        @JsonAlias({"customer_id", "customerId"})
        String customerId,

        @JsonProperty("category")
        String category
) {

    public ChatRequest(UUID conversationId, String question, String category) {
        this(conversationId, question, null, category);
    }

    @AssertTrue(message = "Question or text cannot be blank")
    public boolean isValidQuestion() {
        return question != null && !question.trim().isEmpty();
    }
}
