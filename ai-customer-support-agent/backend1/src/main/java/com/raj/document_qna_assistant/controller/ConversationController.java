package com.raj.document_qna_assistant.controller;

import com.raj.document_qna_assistant.dto.ConversationDto;
import com.raj.document_qna_assistant.entity.Conversation;
import com.raj.document_qna_assistant.service.ConversationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<Conversation> createConversation(@RequestBody(required = false) Map<String, String> body) {
        String title = "New Conversation";
        if (body != null && body.containsKey("title") && body.get("title") != null && !body.get("title").isBlank()) {
            title = body.get("title");
        }
        Conversation conversation = conversationService.createConversation(title);
        return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
    }

    @GetMapping
    public ResponseEntity<List<Conversation>> listConversations() {
        List<Conversation> conversations = conversationService.listConversations();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDto> getConversation(@PathVariable("id") UUID id) {
        ConversationDto history = conversationService.getConversationHistory(id);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable("id") UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
