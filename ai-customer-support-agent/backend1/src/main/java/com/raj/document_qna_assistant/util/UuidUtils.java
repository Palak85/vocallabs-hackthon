package com.raj.document_qna_assistant.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class UuidUtils {

    private UuidUtils() {}

    /**
     * Parses a UUID string safely. If the string is a standard RFC 4122 UUID, parses it directly.
     * If the string is a legacy prefixed ID (e.g., "doc_0538b4c08050", "conv_001"), derives a deterministic
     * Type-3 UUID from its UTF-8 bytes to ensure zero exceptions.
     */
    public static UUID parseSafely(String str) {
        if (str == null || str.isBlank()) {
            return UUID.randomUUID();
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(str.getBytes(StandardCharsets.UTF_8));
        }
    }
}
