package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.DocumentUploadDto;
import com.majerpro.learning_platform.dto.DocumentResponseDto;
import com.majerpro.learning_platform.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private IngestionService ingestionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@Valid @RequestBody DocumentUploadDto dto) {
        try {
            DocumentResponseDto response = ingestionService.uploadDocument(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DocumentResponseDto>> getUserDocuments(@PathVariable Long userId) {
        return ResponseEntity.ok(ingestionService.getUserDocuments(userId));
    }

    @GetMapping("/{documentId}/chunks")
    public ResponseEntity<List<String>> getDocumentChunks(@PathVariable Long documentId) {
        try {
            return ResponseEntity.ok(ingestionService.getDocumentChunks(documentId));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
