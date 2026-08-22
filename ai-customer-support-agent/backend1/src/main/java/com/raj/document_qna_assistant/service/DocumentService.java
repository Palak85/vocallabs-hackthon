package com.raj.document_qna_assistant.service;

import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.DocumentDetailDto;
import com.raj.document_qna_assistant.dto.UploadResponse;
import com.raj.document_qna_assistant.entity.Document;
import com.raj.document_qna_assistant.entity.DocumentStatus;
import com.raj.document_qna_assistant.repository.DocumentRepository;
import com.raj.document_qna_assistant.util.HashUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final IngestionPipeline ingestionPipeline;

    public DocumentService(DocumentRepository documentRepository, IngestionPipeline ingestionPipeline) {
        this.documentRepository = documentRepository;
        this.ingestionPipeline = ingestionPipeline;
    }

    @Transactional
    public UploadResponse uploadDocument(MultipartFile file, String title, String category) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant ID context missing");
        }

        // Validate content type & format
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidFormat(filename)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported file format. Supported formats: PDF, DOCX, TXT, MD");
        }

        // Validate size (20MB limit)
        long maxSizeBytes = 20L * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds limit of 20MB");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file bytes");
        }

        String contentHash = HashUtils.sha256(bytes);
        String finalTitle = (title != null && !title.isBlank()) ? title : getBaseName(filename);

        Optional<Document> existing = documentRepository.findByTenantIdAndContentHash(tenantId, contentHash);
        if (existing.isPresent()) {
            Document doc = existing.get();
            if (doc.getStatus() == DocumentStatus.FAILED) {
                // If it previously failed, delete it to allow retry
                documentRepository.deleteByIdAndTenantId(doc.getId(), tenantId);
            } else {
                // Return existing document details
                return new UploadResponse(doc.getId(), doc.getStatus().name());
            }
        }

        UUID docId = UUID.randomUUID();
        Document doc = new Document(
                docId,
                tenantId,
                finalTitle,
                category,
                filename,
                file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                file.getSize(),
                DocumentStatus.PROCESSING,
                null,
                contentHash,
                Instant.now(),
                Instant.now()
        );

        documentRepository.save(doc);

        // Async ingestion
        ingestionPipeline.ingestAsync(docId, tenantId, doc.getTitle(), category, bytes, doc.getContentType(), filename);

        return new UploadResponse(docId, DocumentStatus.PROCESSING.name());
    }

    public List<DocumentDetailDto> listDocuments(int page, int size) {
        String tenantId = TenantContext.getCurrentTenant();
        int offset = page * size;
        return documentRepository.findAllByTenantId(tenantId, size, offset).stream()
                .map(this::mapToDto)
                .toList();
    }

    public DocumentDetailDto getDocument(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        return documentRepository.findByIdAndTenantId(id, tenantId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
    }

    @Transactional
    public void deleteDocument(UUID id) {
        String tenantId = TenantContext.getCurrentTenant();
        documentRepository.deleteChunksByDocumentId(id);
        boolean deleted = documentRepository.deleteByIdAndTenantId(id, tenantId);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
        }
    }

    private DocumentDetailDto mapToDto(Document doc) {
        return new DocumentDetailDto(
                doc.getId(),
                doc.getTenantId(),
                doc.getTitle(),
                doc.getCategory(),
                doc.getFilename(),
                doc.getContentType(),
                doc.getSizeBytes(),
                doc.getStatus().name(),
                doc.getErrorMessage(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }

    private boolean isValidFormat(String filename) {
        if (!filename.contains(".")) {
            return false;
        }
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return List.of("pdf", "docx", "txt", "md").contains(ext);
    }

    private String getBaseName(String filename) {
        if (filename == null || !filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf('.'));
    }
}
