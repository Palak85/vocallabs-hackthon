package com.hackathon.backend;

import com.hackathon.backend.dto.DocumentStatusResponse;
import com.hackathon.backend.dto.DocumentUploadResponse;
import com.hackathon.backend.entity.Document;
import com.hackathon.backend.entity.IngestionJob;
import com.hackathon.backend.repository.DocumentRepository;
import com.hackathon.backend.repository.IngestionJobRepository;
import com.hackathon.backend.service.IngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private IngestionJobRepository ingestionJobRepository;

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private IngestionService ingestionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ingestionService, "uploadDir", "target/test-uploads");
    }

    @Test
    void testUploadDocumentEmptyThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> ingestionService.uploadDocument(emptyFile));
    }

    @Test
    void testUploadDocumentSuccess() {
        MockMultipartFile validFile = new MockMultipartFile(
                "file",
                "policy.txt",
                "text/plain",
                "This is a refund policy. Customers can request refunds within 30 days of purchase.".getBytes(StandardCharsets.UTF_8)
        );

        DocumentUploadResponse response = ingestionService.uploadDocument(validFile);

        assertNotNull(response);
        assertNotNull(response.getDocumentId());
        assertEquals("policy.txt", response.getFilename());
        assertEquals("PENDING", response.getStatus());

        verify(documentRepository, times(1)).save(any(Document.class));
        verify(ingestionJobRepository, times(1)).save(any(IngestionJob.class));
    }

    @Test
    void testProcessDocumentAsyncSuccess() {
        String docId = "doc_123";
        String jobId = "job_123";
        String content = "Welcome to VocalLabs. We build high accuracy Conversational AI systems.";
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        Document doc = Document.builder().id(docId).filename("info.txt").status("PENDING").build();
        IngestionJob job = IngestionJob.builder().id(jobId).documentId(docId).status("PENDING").build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(ingestionJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        ingestionService.processDocumentAsync(docId, jobId, "info.txt", "text/plain", bytes);

        verify(vectorStore, times(1)).accept(anyList());
        assertEquals("COMPLETED", doc.getStatus());
        assertEquals("COMPLETED", job.getStatus());
    }

    @Test
    void testProcessDocumentAsyncFailureHandlesError() {
        String docId = "doc_456";
        String jobId = "job_456";
        byte[] bytes = "Some sample content".getBytes(StandardCharsets.UTF_8);

        Document doc = Document.builder().id(docId).filename("error.txt").status("PENDING").build();
        IngestionJob job = IngestionJob.builder().id(jobId).documentId(docId).status("PENDING").build();

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(ingestionJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        doThrow(new RuntimeException("PGVector database unavailable")).when(vectorStore).accept(anyList());

        ingestionService.processDocumentAsync(docId, jobId, "error.txt", "text/plain", bytes);

        assertEquals("FAILED", doc.getStatus());
        assertEquals("FAILED", job.getStatus());
        assertNotNull(doc.getErrorMessage());
    }

    @Test
    void testGetDocumentStatus() {
        Document doc = Document.builder()
                .id("doc_789")
                .filename("guide.pdf")
                .status("COMPLETED")
                .chunkCount(5)
                .build();

        when(documentRepository.findById("doc_789")).thenReturn(Optional.of(doc));

        DocumentStatusResponse status = ingestionService.getDocumentStatus("doc_789");

        assertNotNull(status);
        assertEquals("doc_789", status.getDocumentId());
        assertEquals("COMPLETED", status.getStatus());
        assertEquals(5, status.getChunkCount());
    }
}
