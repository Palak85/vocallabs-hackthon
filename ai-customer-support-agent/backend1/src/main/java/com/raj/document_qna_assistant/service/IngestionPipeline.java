package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.entity.DocumentStatus;
import com.raj.document_qna_assistant.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IngestionPipeline {

    private static final Logger log = LoggerFactory.getLogger(IngestionPipeline.class);

    private final TextExtractor textExtractor;
    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;
    private final TransactionTemplate transactionTemplate;

    public IngestionPipeline(TextExtractor textExtractor, 
                             VectorStore vectorStore, 
                             DocumentRepository documentRepository, 
                             TransactionTemplate transactionTemplate) {
        this.textExtractor = textExtractor;
        this.vectorStore = vectorStore;
        this.documentRepository = documentRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Async
    public void ingestAsync(UUID docId, String tenantId, String title, String category, byte[] bytes, String contentType, String filename) {
        try {
            // 1. Extract text page-by-page
            List<TextExtractor.ExtractedPage> pages = textExtractor.extractText(bytes, contentType, filename);
            
            // 2. Chunk text
            List<Document> aiDocuments = new ArrayList<>();
            int chunkIndex = 0;
            for (TextExtractor.ExtractedPage page : pages) {
                List<String> chunks = splitIntoChunks(page.content(), 1000, 200);
                for (String textChunk : chunks) {
                    if (textChunk.isBlank()) {
                        continue;
                    }
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("document_id", docId.toString());
                    metadata.put("tenant_id", tenantId);
                    metadata.put("title", title);
                    if (category != null) {
                        metadata.put("category", category);
                    }
                    metadata.put("chunk_index", chunkIndex++);
                    metadata.put("page_number", page.pageNumber());

                    Document aiDoc = new Document(textChunk, metadata);
                    aiDocuments.add(aiDoc);
                }
            }

            // 3. Write chunks + embeddings to pgvector in a single transaction per document
            if (!aiDocuments.isEmpty()) {
                transactionTemplate.executeWithoutResult(status -> {
                    vectorStore.add(aiDocuments);
                });
            }

            // 4. Update status to READY
            documentRepository.updateStatus(docId, DocumentStatus.READY, null);

        } catch (Exception e) {
            log.error("Failed to ingest document {}", docId, e);
            documentRepository.updateStatus(docId, DocumentStatus.FAILED, e.getMessage());
        }
    }

    public List<String> splitIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        if (text.length() <= chunkSize) {
            chunks.add(text);
            return chunks;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += (chunkSize - overlap);
            if (start >= text.length()) {
                break;
            }
        }
        return chunks;
    }
}
