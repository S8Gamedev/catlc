package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.QuestionCreateDto;
import com.majerpro.learning_platform.dto.QuestionResponseDto;
import com.majerpro.learning_platform.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired private QuestionService questionService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody QuestionCreateDto dto) {
        try {
            QuestionResponseDto saved = questionService.createQuestion(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<?> bySkill(@PathVariable Long skillId) {
        try {
            List<QuestionResponseDto> list = questionService.getQuestionsBySkill(skillId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
