package com.majerpro.learning_platform.controller.practice;

import com.majerpro.learning_platform.dto.practice.CodeSubmissionResultDto;
import com.majerpro.learning_platform.dto.practice.SubmitCodeDto;
import com.majerpro.learning_platform.dto.practice.CreateCodeProblemDto;
import com.majerpro.learning_platform.model.practice.CodeProblem;
import com.majerpro.learning_platform.service.practice.CodePracticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List; // NEW

@RestController
@RequestMapping("/api/practice/code")
public class CodePracticeController {

    private final CodePracticeService codePracticeService;

    public CodePracticeController(CodePracticeService codePracticeService) {
        this.codePracticeService = codePracticeService;
    }

    @PostMapping("/problems")
    public ResponseEntity<?> createProblem(@RequestBody CreateCodeProblemDto dto) {
        try {
            CodeProblem p = codePracticeService.createProblem(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(p);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody SubmitCodeDto dto) {
        try {
            CodeSubmissionResultDto res = codePracticeService.submit(dto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/evaluate/{submissionId}")
    public ResponseEntity<?> evaluate(@PathVariable Long submissionId) {
        try {
            CodeSubmissionResultDto res = codePracticeService.evaluate(submissionId);
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NEW: Submission history for frontend
    @GetMapping("/submissions/user/{userId}")
    public ResponseEntity<?> userSubmissions(@PathVariable Long userId) {
        try {
            List<CodeSubmissionResultDto> res = codePracticeService.getSubmissionsForUser(userId); // NEW
            return ResponseEntity.ok(res);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // NEW
    @GetMapping("/submissions/user/{userId}/problem/{problemId}")
    public ResponseEntity<?> userProblemSubmissions(
            @PathVariable Long userId,
            @PathVariable Long problemId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        try {
            return ResponseEntity.ok(codePracticeService.getSubmissionsForUserAndProblem(userId, problemId, limit));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

