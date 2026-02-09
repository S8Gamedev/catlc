package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.ChatRequestDto;
import com.majerpro.learning_platform.dto.ChatResponseDto;
import com.majerpro.learning_platform.model.Conversation;
import com.majerpro.learning_platform.model.Message;
import com.majerpro.learning_platform.service.AIService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequestDto request) {
        try {
            ChatResponseDto response = aiService.chat(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(aiService.getUserConversations(userId));
    }

    @GetMapping("/conversation/{conversationId}/history")
    public ResponseEntity<List<Message>> getConversationHistory(@PathVariable Long conversationId) {
        try {
            return ResponseEntity.ok(aiService.getConversationHistory(conversationId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
