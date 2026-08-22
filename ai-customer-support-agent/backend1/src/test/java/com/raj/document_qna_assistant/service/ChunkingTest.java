package com.raj.document_qna_assistant.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ChunkingTest {

    private final IngestionPipeline ingestionPipeline = new IngestionPipeline(null, null, null, null);

    @Test
    void testEmptyText() {
        List<String> chunks = ingestionPipeline.splitIntoChunks("", 1000, 200);
        assertTrue(chunks.isEmpty());

        chunks = ingestionPipeline.splitIntoChunks(null, 1000, 200);
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSingleWordText() {
        List<String> chunks = ingestionPipeline.splitIntoChunks("hello", 1000, 200);
        assertEquals(1, chunks.size());
        assertEquals("hello", chunks.get(0));
    }

    @Test
    void testTextLargerThanOneChunk() {
        // Chunk size 10, overlap 2. Text of length 15.
        // First chunk: 0 to 10 (length 10)
        // Next start: 10 - 2 = 8.
        // Second chunk: 8 to 15 (length 7)
        String text = "abcdefghijklmno"; // 15 characters
        List<String> chunks = ingestionPipeline.splitIntoChunks(text, 10, 2);
        
        assertEquals(2, chunks.size());
        assertEquals("abcdefghij", chunks.get(0));
        assertEquals("ijklmno", chunks.get(1));
    }

    @Test
    void testTextExactlyEqualToChunkSize() {
        String text = "abcdefghij"; // 10 characters
        List<String> chunks = ingestionPipeline.splitIntoChunks(text, 10, 2);
        assertEquals(1, chunks.size());
        assertEquals("abcdefghij", chunks.get(0));
    }
}
