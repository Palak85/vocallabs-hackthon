package com.hackathon.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.controller.ChatController;
import com.hackathon.backend.controller.DocumentController;
import com.hackathon.backend.dto.ChatRequest;
import com.hackathon.backend.dto.ChatResponse;
import com.hackathon.backend.dto.DocumentDto;
import com.hackathon.backend.dto.DocumentStatusResponse;
import com.hackathon.backend.dto.DocumentUploadResponse;
import com.hackathon.backend.exception.GlobalExceptionHandler;
import com.hackathon.backend.service.ChatService;
import com.hackathon.backend.service.IngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

    private MockMvc chatMockMvc;
    private MockMvc documentMockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChatService chatService;

    @Mock
    private IngestionService ingestionService;

    @InjectMocks
    private ChatController chatController;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatMockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        documentMockMvc = MockMvcBuilders.standaloneSetup(documentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testChatEndpointSuccess() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("How can I track my order?")
                .build();

        ChatResponse response = ChatResponse.builder()
                .messageId("msg_111")
                .conversationId("conv_222")
                .answer("You can track your order using the link in your email.")
                .build();

        when(chatService.processChat(any(ChatRequest.class))).thenReturn(response);

        chatMockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("msg_111"))
                .andExpect(jsonPath("$.conversationId").value("conv_222"))
                .andExpect(jsonPath("$.answer").value("You can track your order using the link in your email."));
    }

    @Test
    void testChatEndpointBlankMessageFailsValidation() throws Exception {
        ChatRequest request = ChatRequest.builder()
                .message("   ")
                .build();

        chatMockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.validationErrors.message").exists());
    }

    @Test
    void testDocumentUploadReturnsAccepted() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "faq.txt", "text/plain", "FAQ content".getBytes());

        DocumentUploadResponse uploadResponse = DocumentUploadResponse.builder()
                .documentId("doc_101")
                .filename("faq.txt")
                .status("PENDING")
                .message("Document upload accepted. Processing started in background.")
                .build();

        when(ingestionService.uploadDocument(any())).thenReturn(uploadResponse);

        documentMockMvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.documentId").value("doc_101"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetDocumentStatusEndpoint() throws Exception {
        DocumentStatusResponse statusResponse = DocumentStatusResponse.builder()
                .documentId("doc_101")
                .filename("faq.txt")
                .status("COMPLETED")
                .chunkCount(3)
                .build();

        when(ingestionService.getDocumentStatus("doc_101")).thenReturn(statusResponse);

        documentMockMvc.perform(get("/api/documents/doc_101/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("doc_101"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.chunkCount").value(3));
    }

    @Test
    void testGetDocumentFileEndpoint() throws Exception {
        DocumentDto docDto = DocumentDto.builder()
                .id("doc_101")
                .filename("manual.pdf")
                .contentType("application/pdf")
                .build();

        ByteArrayResource resource = new ByteArrayResource("PDF-DATA".getBytes());

        when(ingestionService.getDocumentById("doc_101")).thenReturn(docDto);
        when(ingestionService.loadDocumentResource("doc_101")).thenReturn(resource);

        documentMockMvc.perform(get("/api/documents/doc_101/file"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"manual.pdf\""))
                .andExpect(content().bytes("PDF-DATA".getBytes()));
    }
}
