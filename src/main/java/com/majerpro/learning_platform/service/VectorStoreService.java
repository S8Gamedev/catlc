package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.SearchQueryDto;
import com.majerpro.learning_platform.dto.SearchResultDto;
import com.majerpro.learning_platform.model.*;
import com.majerpro.learning_platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorStoreService {

    @Autowired
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmbeddingService embeddingService;

    /**
     * Generate and store embeddings for all chunks of a document
     */
    @Transactional
    public void generateEmbeddingsForDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        List<Chunk> chunks = chunkRepository.findByDocument(document);

        for (Chunk chunk : chunks) {
            // Check if embedding already exists
            if (vectorEmbeddingRepository.findByChunk(chunk).isPresent()) {
                continue; // Skip if already embedded
            }

            // Generate embedding
            float[] embedding = embeddingService.generateEmbedding(chunk.getContent());

            // Store embedding
            VectorEmbedding vectorEmbedding = new VectorEmbedding();
            vectorEmbedding.setChunk(chunk);
            vectorEmbedding.setVectorData(embeddingService.vectorToString(embedding));
            vectorEmbedding.setEmbeddingModel("all-MiniLM-L6-v2");
            vectorEmbedding.setVectorDimension(embedding.length);

            vectorEmbeddingRepository.save(vectorEmbedding);
        }
    }

    /**
     * Semantic search: Find most similar chunks to query
     */
    public List<SearchResultDto> semanticSearch(SearchQueryDto searchQuery) {
        User user = userRepository.findById(searchQuery.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate embedding for search query
        float[] queryEmbedding = embeddingService.generateEmbedding(searchQuery.getQuery());

        // Get all user's documents
        List<Document> documents = documentRepository.findByUser(user);

        // Filter by category if specified
        if (searchQuery.getCategory() != null && !searchQuery.getCategory().isEmpty()) {
            documents = documents.stream()
                    .filter(doc -> searchQuery.getCategory().equals(doc.getCategory()))
                    .collect(Collectors.toList());
        }

        // Calculate similarity for all chunks
        List<SearchResultDto> results = new ArrayList<>();

        for (Document doc : documents) {
            List<Chunk> chunks = chunkRepository.findByDocument(doc);

            for (Chunk chunk : chunks) {
                vectorEmbeddingRepository.findByChunk(chunk).ifPresent(vectorEmbedding -> {
                    float[] chunkEmbedding = embeddingService.stringToVector(
                            vectorEmbedding.getVectorData()
                    );

                    double similarity = embeddingService.cosineSimilarity(
                            queryEmbedding,
                            chunkEmbedding
                    );

                    SearchResultDto result = new SearchResultDto();
                    result.setChunkId(chunk.getId());
                    result.setDocumentId(doc.getId());
                    result.setDocumentTitle(doc.getTitle());
                    result.setChunkContent(chunk.getContent());
                    result.setSimilarityScore(similarity);
                    result.setChunkIndex(chunk.getChunkIndex());

                    results.add(result);
                });
            }
        }

        // Sort by similarity (highest first) and return top K
        return results.stream()
                .sorted(Comparator.comparing(SearchResultDto::getSimilarityScore).reversed())
                .limit(searchQuery.getTopK())
                .collect(Collectors.toList());
    }

    /**
     * Get total number of embeddings stored
     */
    public long getEmbeddingCount() {
        return vectorEmbeddingRepository.count();
    }
}
