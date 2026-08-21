package com.hackathon.backend.service;

import com.hackathon.backend.dto.DocumentDto;
import com.hackathon.backend.dto.DocumentStatusResponse;
import com.hackathon.backend.dto.DocumentUploadResponse;
import com.hackathon.backend.entity.Document;
import com.hackathon.backend.entity.IngestionJob;
import com.hackathon.backend.repository.DocumentRepository;
import com.hackathon.backend.repository.IngestionJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentRepository documentRepository;
    private final IngestionJobRepository ingestionJobRepository;
    private final VectorStore vectorStore;

    @Value("${storage.upload-dir:uploads}")
    private String uploadDir = "uploads";

    public DocumentUploadResponse uploadDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Upload file must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "unnamed_document";
        }

        String documentId = "doc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String jobId = "job_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        byte[] fileBytes;
        String savedStoragePath = null;
        try {
            fileBytes = file.getBytes();
            // Save file to server disk
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path targetPath = uploadPath.resolve(documentId + "_" + originalFilename);
            Files.write(targetPath, fileBytes);
            savedStoragePath = targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Failed to store uploaded file: {}", originalFilename, e);
            throw new RuntimeException("File storage failed", e);
        }

        Document doc = Document.builder()
                .id(documentId)
                .filename(originalFilename)
                .contentType(file.getContentType())
                .size(file.getSize())
                .status("PENDING")
                .storagePath(savedStoragePath)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        documentRepository.save(doc);

        IngestionJob job = IngestionJob.builder()
                .id(jobId)
                .documentId(documentId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        ingestionJobRepository.save(job);

        // Trigger Async processing
        processDocumentAsync(documentId, jobId, originalFilename, file.getContentType(), fileBytes);

        return DocumentUploadResponse.builder()
                .documentId(documentId)
                .filename(originalFilename)
                .status("PENDING")
                .message("Document upload accepted. Processing started in background.")
                .build();
    }

    @Async
    public void processDocumentAsync(String documentId, String jobId, String filename, String contentType, byte[] bytes) {
        log.info("Starting async ingestion for documentId: {}, filename: {}", documentId, filename);
        try {
            // Update status to PROCESSING
            updateStatus(documentId, jobId, "PROCESSING", null, null);

            // 1. Extract text
            String extractedText = extractText(filename, contentType, bytes);
            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalStateException("No text could be extracted from document: " + filename);
            }

            // 2. Chunk text
            List<String> textChunks = chunkText(extractedText, 800, 150);
            log.info("Extracted {} chunks from documentId: {}", textChunks.size(), documentId);

            // 3. Create Spring AI Documents
            List<org.springframework.ai.document.Document> aiDocs = new ArrayList<>();
            for (int i = 0; i < textChunks.size(); i++) {
                String chunk = textChunks.get(i);
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("documentId", documentId);
                metadata.put("filename", filename);
                metadata.put("chunkIndex", i);
                metadata.put("totalChunks", textChunks.size());

                String chunkId = documentId + "_chk_" + i;
                org.springframework.ai.document.Document aiDoc = new org.springframework.ai.document.Document(chunkId, chunk, metadata);
                aiDocs.add(aiDoc);
            }

            // 4. Store embeddings in PGVector
            log.info("Generating embeddings and writing {} chunks to vector store...", aiDocs.size());
            vectorStore.accept(aiDocs);

            // 5. Update status to COMPLETED
            updateStatus(documentId, jobId, "COMPLETED", null, aiDocs.size());
            log.info("Successfully ingested documentId: {}", documentId);

        } catch (Exception e) {
            log.error("Async ingestion failed for documentId: {}", documentId, e);
            markFailed(documentId, jobId, e.getMessage());
        }
    }

    private String extractText(String filename, String contentType, byte[] bytes) throws IOException {
        boolean isPdf = (contentType != null && contentType.toLowerCase().contains("pdf"))
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));

        if (isPdf) {
            try (PDDocument pdfDocument = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(pdfDocument);
            }
        } else {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        int start = 0;
        int length = normalized.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            
            // Try to break on a whitespace or newline if possible
            if (end < length) {
                int lastSpace = normalized.lastIndexOf(' ', end);
                int lastNewline = normalized.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastSpace, lastNewline);
                if (breakPoint > start + (chunkSize / 2)) {
                    end = breakPoint;
                }
            }

            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end >= length) {
                break;
            }

            start = Math.max(start + 1, end - overlap);
        }

        return chunks;
    }

    @Transactional
    public void updateStatus(String documentId, String jobId, String status, String errorMessage, Integer chunkCount) {
        documentRepository.findById(documentId).ifPresent(doc -> {
            doc.setStatus(status);
            doc.setErrorMessage(errorMessage);
            if (chunkCount != null) {
                doc.setChunkCount(chunkCount);
            }
            doc.setUpdatedAt(LocalDateTime.now());
            documentRepository.save(doc);
        });

        ingestionJobRepository.findById(jobId).ifPresent(job -> {
            job.setStatus(status);
            job.setErrorMessage(errorMessage);
            job.setUpdatedAt(LocalDateTime.now());
            ingestionJobRepository.save(job);
        });
    }

    @Transactional
    public void markFailed(String documentId, String jobId, String errorMessage) {
        updateStatus(documentId, jobId, "FAILED", errorMessage, null);
    }

    public DocumentStatusResponse getDocumentStatus(String documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID: " + documentId));

        return DocumentStatusResponse.builder()
                .documentId(doc.getId())
                .filename(doc.getFilename())
                .status(doc.getStatus())
                .errorMessage(doc.getErrorMessage())
                .chunkCount(doc.getChunkCount())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }

    public List<DocumentDto> getAllDocuments() {
        return documentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(doc -> DocumentDto.builder()
                        .id(doc.getId())
                        .filename(doc.getFilename())
                        .contentType(doc.getContentType())
                        .size(doc.getSize())
                        .status(doc.getStatus())
                        .chunkCount(doc.getChunkCount())
                        .createdAt(doc.getCreatedAt())
                        .build())
                .toList();
    }

    public DocumentDto getDocumentById(String documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID: " + documentId));

        return DocumentDto.builder()
                .id(doc.getId())
                .filename(doc.getFilename())
                .contentType(doc.getContentType())
                .size(doc.getSize())
                .status(doc.getStatus())
                .chunkCount(doc.getChunkCount())
                .createdAt(doc.getCreatedAt())
                .build();
    }

    public Resource loadDocumentResource(String documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException("Document not found with ID: " + documentId));

        if (doc.getStoragePath() == null || doc.getStoragePath().isBlank()) {
            throw new NoSuchElementException("File content not found for document: " + documentId);
        }

        Path filePath = Paths.get(doc.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new NoSuchElementException("Physical file not found on disk at: " + doc.getStoragePath());
        }

        return new FileSystemResource(filePath);
    }
}
