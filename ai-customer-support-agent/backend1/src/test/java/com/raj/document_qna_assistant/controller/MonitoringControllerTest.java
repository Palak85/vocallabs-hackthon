package com.raj.document_qna_assistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raj.document_qna_assistant.dto.AgentMessageRequest;
import com.raj.document_qna_assistant.dto.MessageDto;
import com.raj.document_qna_assistant.dto.MonitoringConversationDto;
import com.raj.document_qna_assistant.dto.MonitoringDetailDto;
import com.raj.document_qna_assistant.dto.MonitoringStatsDto;
import com.raj.document_qna_assistant.dto.TakeoverRequest;
import com.raj.document_qna_assistant.service.MonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonitoringControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MonitoringService monitoringService;

    @InjectMocks
    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(monitoringController).build();
    }

    @Test
    void testListMonitoredConversations() throws Exception {
        UUID id = UUID.randomUUID();
        MonitoringConversationDto dto = new MonitoringConversationDto(
                id, "UPI Issue", "AI", null, "RECOMMENDED", "High frustration", 72, "high",
                "negative", "frustrated", "transaction_failed", "banking", "ACTIVE", 2, "My money was deducted",
                Instant.now(), Instant.now()
        );

        when(monitoringService.listMonitoredConversations()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/monitoring/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].escalationStatus").value("RECOMMENDED"))
                .andExpect(jsonPath("$[0].frustrationScore").value(72));
    }

    @Test
    void testGetConversationDetail() throws Exception {
        UUID id = UUID.randomUUID();
        MonitoringDetailDto detailDto = new MonitoringDetailDto(
                id, "UPI Issue", "HUMAN", "Agent Sarah", "ESCALATED", null, 72, "high",
                "negative", "frustrated", "transaction_failed", "banking", "ACTIVE",
                Instant.now(), Instant.now(), List.of()
        );

        when(monitoringService.getConversationDetail(id)).thenReturn(detailDto);

        mockMvc.perform(get("/api/v1/monitoring/conversations/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.mode").value("HUMAN"))
                .andExpect(jsonPath("$.assignedAgent").value("Agent Sarah"));
    }

    @Test
    void testTakeoverEndpoint() throws Exception {
        UUID id = UUID.randomUUID();
        TakeoverRequest req = new TakeoverRequest("Agent Sarah", "Taking over due to high frustration");
        MonitoringConversationDto dto = new MonitoringConversationDto(
                id, "UPI Issue", "HUMAN", "Agent Sarah", "ESCALATED", null, 72, "high",
                "negative", "frustrated", "transaction_failed", "banking", "ACTIVE", 2, "My money was deducted",
                Instant.now(), Instant.now()
        );

        when(monitoringService.takeoverConversation(eq(id), eq("Agent Sarah"))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/monitoring/conversations/" + id + "/takeover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("HUMAN"))
                .andExpect(jsonPath("$.assignedAgent").value("Agent Sarah"));
    }

    @Test
    void testHandbackEndpoint() throws Exception {
        UUID id = UUID.randomUUID();
        MonitoringConversationDto dto = new MonitoringConversationDto(
                id, "UPI Issue", "AI", null, "RESOLVED", null, 20, "low",
                "neutral", "neutral", "transaction_failed", "banking", "ACTIVE", 4, "Thank you",
                Instant.now(), Instant.now()
        );

        when(monitoringService.handbackConversation(id)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/monitoring/conversations/" + id + "/handback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("AI"))
                .andExpect(jsonPath("$.escalationStatus").value("RESOLVED"));
    }

    @Test
    void testSendAgentMessageEndpoint() throws Exception {
        UUID id = UUID.randomUUID();
        AgentMessageRequest req = new AgentMessageRequest("Hello, I am handling your case now.", "Agent Sarah");
        MessageDto msgDto = new MessageDto(
                UUID.randomUUID(), "AGENT", "Hello, I am handling your case now.", List.of(), Instant.now()
        );

        when(monitoringService.sendAgentMessage(eq(id), eq("Agent Sarah"), eq("Hello, I am handling your case now.")))
                .thenReturn(msgDto);

        mockMvc.perform(post("/api/v1/monitoring/conversations/" + id + "/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("AGENT"))
                .andExpect(jsonPath("$.content").value("Hello, I am handling your case now."));
    }

    @Test
    void testGetMonitoringStatsEndpoint() throws Exception {
        MonitoringStatsDto stats = new MonitoringStatsDto(10, 8, 2, 3, 1, 55.4);
        when(monitoringService.getMonitoringStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/monitoring/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalConversations").value(10))
                .andExpect(jsonPath("$.activeAiConversations").value(8))
                .andExpect(jsonPath("$.activeHumanConversations").value(2))
                .andExpect(jsonPath("$.averageFrustrationScore").value(55.4));
    }
}
