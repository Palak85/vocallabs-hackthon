package com.raj.document_qna_assistant.controller;

import com.raj.document_qna_assistant.dto.DocumentDetailDto;
import com.raj.document_qna_assistant.dto.UploadResponse;
import com.raj.document_qna_assistant.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<UploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category", required = false) String category) {
        UploadResponse response = documentService.uploadDocument(file, title, category);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DocumentDetailDto>> listDocuments(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        List<DocumentDetailDto> documents = documentService.listDocuments(page, size);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailDto> getDocument(@PathVariable("id") UUID id) {
        DocumentDetailDto doc = documentService.getDocument(id);
        return ResponseEntity.ok(doc);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable("id") UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
