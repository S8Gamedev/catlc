package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.*;
import com.majerpro.learning_platform.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired private QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<?> generate(@Valid @RequestBody GenerateQuizRequestDto req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(quizService.generateQuiz(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{quizId}/start")
    public ResponseEntity<?> start(@PathVariable Long quizId, @Valid @RequestBody StartAttemptRequestDto req) {
        try {
            Long attemptId = quizService.startAttempt(quizId, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(attemptId);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<?> submit(@PathVariable Long attemptId, @Valid @RequestBody SubmitAttemptDto req) {
        try {
            return ResponseEntity.ok(quizService.submitAttempt(attemptId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<?> attempt(@PathVariable Long attemptId) {
        try {
            return ResponseEntity.ok(quizService.getAttemptResult(attemptId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
