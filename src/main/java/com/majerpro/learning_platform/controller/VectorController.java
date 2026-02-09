package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.SearchQueryDto;
import com.majerpro.learning_platform.dto.SearchResultDto;
import com.majerpro.learning_platform.service.VectorStoreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vector")
public class VectorController {

    @Autowired
    private VectorStoreService vectorStoreService;

    /**
     * Existing endpoint (kept for backward compatibility)
     */
    @PostMapping("/generate-embeddings/{documentId}")
    public ResponseEntity<?> generateEmbeddings(@PathVariable Long documentId) {
        try {
            vectorStoreService.generateEmbeddingsForDocument(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "status", "EMBEDDINGS_GENERATED"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * NEW: Index endpoint (Option B naming)
     * Same behavior as generate-embeddings but more "RAG friendly" naming.
     */
    @PostMapping("/index/{documentId}")
    public ResponseEntity<?> indexDocument(@PathVariable Long documentId) {
        try {
            vectorStoreService.generateEmbeddingsForDocument(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "status", "INDEXED"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * NEW: Reindex endpoint (handy for testing after deleting rows, changing chunking, etc.)
     * Note: With your current service, "reindex" won't regenerate embeddings if they already exist
     * because generateEmbeddingsForDocument() skips chunks that already have embeddings. [file:9]
     */
    @PostMapping("/reindex/{documentId}")
    public ResponseEntity<?> reindexDocument(@PathVariable Long documentId) {
        try {
            vectorStoreService.generateEmbeddingsForDocument(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "status", "REINDEX_REQUESTED"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Existing semantic search
     */
    @PostMapping("/search")
    public ResponseEntity<List<SearchResultDto>> semanticSearch(
            @Valid @RequestBody SearchQueryDto searchQuery) {
        List<SearchResultDto> results = vectorStoreService.semanticSearch(searchQuery);
        return ResponseEntity.ok(results);
    }

    /**
     * Existing embedding count
     */
    @GetMapping("/count")
    public ResponseEntity<?> getEmbeddingCount() {
        return ResponseEntity.ok(Map.of(
                "count", vectorStoreService.getEmbeddingCount()
        ));
    }

    /**
     * NEW: quick health check for vector module
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "embeddingCount", vectorStoreService.getEmbeddingCount()
        ));
    }
}
