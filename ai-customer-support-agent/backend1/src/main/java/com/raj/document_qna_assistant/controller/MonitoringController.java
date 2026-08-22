package com.raj.document_qna_assistant.controller;

import com.raj.document_qna_assistant.dto.AgentMessageRequest;
import com.raj.document_qna_assistant.dto.MessageDto;
import com.raj.document_qna_assistant.dto.MonitoringConversationDto;
import com.raj.document_qna_assistant.dto.MonitoringDetailDto;
import com.raj.document_qna_assistant.dto.MonitoringStatsDto;
import com.raj.document_qna_assistant.dto.TakeoverRequest;
import com.raj.document_qna_assistant.service.MonitoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping({"/api/v1/monitoring", "/api/monitoring"})
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<MonitoringConversationDto>> listMonitoredConversations() {
        return ResponseEntity.ok(monitoringService.listMonitoredConversations());
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<MonitoringDetailDto> getConversationDetail(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(monitoringService.getConversationDetail(id));
    }

    @PostMapping("/conversations/{id}/takeover")
    public ResponseEntity<MonitoringConversationDto> takeoverConversation(
            @PathVariable("id") UUID id,
            @RequestBody(required = false) TakeoverRequest request) {
        String agentName = (request != null && request.agentName() != null) ? request.agentName() : "Support Specialist";
        MonitoringConversationDto result = monitoringService.takeoverConversation(id, agentName);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/conversations/{id}/handback")
    public ResponseEntity<MonitoringConversationDto> handbackConversation(@PathVariable("id") UUID id) {
        MonitoringConversationDto result = monitoringService.handbackConversation(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/conversations/{id}/message")
    public ResponseEntity<MessageDto> sendAgentMessage(
            @PathVariable("id") UUID id,
            @Valid @RequestBody AgentMessageRequest request) {
        MessageDto result = monitoringService.sendAgentMessage(id, request.agentName(), request.message());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<MonitoringStatsDto> getMonitoringStats() {
        return ResponseEntity.ok(monitoringService.getMonitoringStats());
    }

    @GetMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public reactor.core.publisher.Flux<org.springframework.http.codec.ServerSentEvent<String>> streamMonitoringEvents() {
        return monitoringService.streamMonitoringEvents();
    }
}
