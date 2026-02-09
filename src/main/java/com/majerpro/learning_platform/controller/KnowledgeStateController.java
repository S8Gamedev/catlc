package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.KnowledgeStateDto;
import com.majerpro.learning_platform.dto.UpdateMasteryDto;
import com.majerpro.learning_platform.service.KnowledgeTracingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeStateController {

    @Autowired
    private KnowledgeTracingService knowledgeTracingService;

    @PostMapping("/update-mastery")
    public ResponseEntity<?> updateMastery(@Valid @RequestBody UpdateMasteryDto dto) {
        try {
            KnowledgeStateDto result = knowledgeTracingService.updateMastery(dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<KnowledgeStateDto>> getUserKnowledge(@PathVariable Long userId) {
        return ResponseEntity.ok(knowledgeTracingService.getUserKnowledgeStates(userId));
    }

    @GetMapping("/user/{userId}/weak-skills")
    public ResponseEntity<List<KnowledgeStateDto>> getWeakSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(knowledgeTracingService.getWeakSkills(userId));
    }
}
