package com.raj.document_qna_assistant;

import com.raj.document_qna_assistant.config.TenantContext;
import com.raj.document_qna_assistant.dto.ChatRequest;
import com.raj.document_qna_assistant.dto.ChatResponse;
import com.raj.document_qna_assistant.dto.DocumentDetailDto;
import com.raj.document_qna_assistant.dto.UploadResponse;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.entity.DocumentStatus;
import com.raj.document_qna_assistant.repository.TenantRepository;
import com.raj.document_qna_assistant.service.ChatService;
import com.raj.document_qna_assistant.service.ConversationService;
import com.raj.document_qna_assistant.service.DocumentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestModelsConfig.class)
class DocumentQnaIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16");

    @LocalServerPort
    private int port;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String TENANT_A = "tenant-a";
    private static final String TENANT_B = "tenant-b";

    @BeforeEach
    void setUp() {
        tenantRepository.save(TENANT_A, "Tenant A");
        tenantRepository.save(TENANT_B, "Tenant B");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE TABLE message_sources CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE messages CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE conversations CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE document_chunks CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE documents CASCADE");
        TenantContext.clear();
    }

    @Test
    void testFullDocumentIngestionPipeline() throws Exception {
        TenantContext.setCurrentTenant(TENANT_A);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.txt",
                "text/plain",
                "This is the core content of the transport policy for school buses.".getBytes()
        );

        UploadResponse uploadResponse = documentService.uploadDocument(file, "Transport Policy", "TRANSPORT");
        assertNotNull(uploadResponse.id());
        assertEquals("PROCESSING", uploadResponse.status());

        // Wait for async ingestion to complete using Awaitility
        await().pollInSameThread().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            DocumentDetailDto detail = documentService.getDocument(uploadResponse.id());
            assertEquals("READY", detail.status());
        });

        // Verify embedding persistence
        Integer chunkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM document_chunks WHERE tenant_id = ?",
                Integer.class,
                TENANT_A
        );
        assertNotNull(chunkCount);
        assertTrue(chunkCount > 0);
    }

    @Test
    void testTenantIsolationAndCategoryFiltering() {
        // Upload for Tenant A
        TenantContext.setCurrentTenant(TENANT_A);
        MockMultipartFile fileA = new MockMultipartFile(
                "file",
                "fees-policy.txt",
                "text/plain",
                "The late payment fee for term 2 is 50 dollars.".getBytes()
        );
        UploadResponse uploadA = documentService.uploadDocument(fileA, "Fees Policy", "FEES");

        // Upload for Tenant B
        TenantContext.setCurrentTenant(TENANT_B);
        MockMultipartFile fileB = new MockMultipartFile(
                "file",
                "leave-policy.txt",
                "text/plain",
                "Sick leave must be approved by the principal.".getBytes()
        );
        UploadResponse uploadB = documentService.uploadDocument(fileB, "HR Policy", "HR");

        // Wait for both to be READY
        await().pollInSameThread().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            TenantContext.setCurrentTenant(TENANT_A);
            assertEquals("READY", documentService.getDocument(uploadA.id()).status());
            TenantContext.setCurrentTenant(TENANT_B);
            assertEquals("READY", documentService.getDocument(uploadB.id()).status());
        });

        // Query as Tenant A with category FEES
        TenantContext.setCurrentTenant(TENANT_A);
        ChatRequest chatReq = new ChatRequest(null, "payment fee", "FEES");
        ChatResponse chatRes = chatService.chat(chatReq);

        // Should return answer since A's document contains "payment fee" in category FEES
        assertNotEquals("not found in the available documents", chatRes.answer());
        assertEquals(1, chatRes.sources().size());
        assertEquals("Fees Policy", chatRes.sources().get(0).title());

        // Query as Tenant A with category HR (no docs exist in A under category HR)
        ChatRequest chatReqHr = new ChatRequest(null, "sick leave", "HR");
        ChatResponse chatResHr = chatService.chat(chatReqHr);
        assertEquals("not found in the available documents", chatResHr.answer());

        // Query as Tenant B for "payment fee" (A's doc belongs to Tenant A)
        TenantContext.setCurrentTenant(TENANT_B);
        ChatRequest chatReqB = new ChatRequest(null, "payment fee", "FEES");
        ChatResponse chatResB = chatService.chat(chatReqB);
        // Tenant B has no such document under category FEES, must refuse
        assertEquals("not found in the available documents", chatResB.answer());
    }

    @Test
    void testSimilarityThresholdRefusal() {
        TenantContext.setCurrentTenant(TENANT_A);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "transport.txt",
                "text/plain",
                "Buses leave at 3 PM daily.".getBytes()
        );
        UploadResponse upload = documentService.uploadDocument(file, "Transport", "TRANSPORT");

        await().pollInSameThread().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals("READY", documentService.getDocument(upload.id()).status());
        });

        // Query with text completely unrelated to mock embedding vector definition
        // (Note: our MockEmbeddingModel sin wave generator creates vectors based on text hash,
        // so querying with a totally different string will return low similarity)
        ChatRequest chatReq = new ChatRequest(null, "Unrelated query text that will not match", "TRANSPORT");
        ChatResponse chatRes = chatService.chat(chatReq);

        // Verification: threshold is set to 0.62 in configuration/service, unrelated text fails threshold
        // and triggers the refusal path directly without LLM invocation.
        assertEquals("not found in the available documents", chatRes.answer());
        assertTrue(chatRes.sources().isEmpty());
    }

    @Test
    void testDocumentDeletionCascades() {
        TenantContext.setCurrentTenant(TENANT_A);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.txt",
                "text/plain",
                "Important guidelines for teachers.".getBytes()
        );
        UploadResponse upload = documentService.uploadDocument(file, "Guidelines", "HR");

        await().pollInSameThread().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals("READY", documentService.getDocument(upload.id()).status());
        });

        // Verify chunks exist
        Integer chunkCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM document_chunks WHERE tenant_id = ?",
                Integer.class,
                TENANT_A
        );
        assertTrue(chunkCount > 0);

        // Delete document
        documentService.deleteDocument(upload.id());

        // Verify document is deleted
        assertThrows(Exception.class, () -> documentService.getDocument(upload.id()));

        // Verify chunks are deleted via cascade delete
        Integer chunkCountAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM document_chunks WHERE tenant_id = ?",
                Integer.class,
                TENANT_A
        );
        assertEquals(0, chunkCountAfter);
    }

    @Test
    void testConversationPersistenceAndFollowUpQuestions() {
        TenantContext.setCurrentTenant(TENANT_A);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "school.txt",
                "text/plain",
                "School principal is Mr. Smith.".getBytes()
        );
        UploadResponse upload = documentService.uploadDocument(file, "School", "HR");

        await().pollInSameThread().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals("READY", documentService.getDocument(upload.id()).status());
        });

        // 1. Initial Chat (absent conversation ID)
        ChatRequest chatReq1 = new ChatRequest(null, "Who is the principal?", "HR");
        ChatResponse chatRes1 = chatService.chat(chatReq1);
        UUID convId = chatRes1.conversationId();
        assertNotNull(convId);

        // Verify conversation & message saved in DB
        Integer messageCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE conversation_id = ?",
                Integer.class,
                convId
        );
        // Includes 1 user message + 1 assistant message = 2 messages
        assertEquals(2, messageCount);

        // Verify message sources / citations saved in DB
        Integer sourceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM message_sources",
                Integer.class
        );
        assertTrue(sourceCount > 0);

        // 2. Follow-up Chat (include conversation ID)
        ChatRequest chatReq2 = new ChatRequest(convId, "What about fees?", "FEES");
        ChatResponse chatRes2 = chatService.chat(chatReq2);
        assertEquals(convId, chatRes2.conversationId());

        // Verify total messages are now 4
        Integer totalMsgCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM messages WHERE conversation_id = ?",
                Integer.class,
                convId
        );
        assertEquals(4, totalMsgCount);
    }
}
