package com.raj.document_qna_assistant.util;

public final class TokenEstimator {
    private TokenEstimator() {}

    public static int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        // Standard rule of thumb: 1 token is roughly 4 characters
        return (int) Math.ceil(text.length() / 4.0);
    }
}
