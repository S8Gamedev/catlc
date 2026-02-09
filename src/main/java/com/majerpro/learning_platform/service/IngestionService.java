package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.DocumentUploadDto;
import com.majerpro.learning_platform.dto.DocumentResponseDto;
import com.majerpro.learning_platform.model.Document;
import com.majerpro.learning_platform.model.Chunk;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.repository.DocumentRepository;
import com.majerpro.learning_platform.repository.ChunkRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private UserRepository userRepository;

    // Chunk size: approximately 500 words
    private static final int CHUNK_SIZE = 500;

    /**
     * Upload and process a document
     */
    @Transactional
    public DocumentResponseDto uploadDocument(DocumentUploadDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create document
        Document document = new Document();
        document.setUser(user);
        document.setTitle(dto.getTitle());
        document.setFileName(dto.getFileName());
        document.setFileType(dto.getFileType());
        document.setFileSize((long) dto.getContent().length());
        document.setDescription(dto.getDescription());
        document.setCategory(dto.getCategory());
        document.setSourceUrl(dto.getSourceUrl());
        document.setIsProcessed(false);

        Document savedDocument = documentRepository.save(document);

        // Chunk the content
        List<Chunk> chunks = chunkContent(savedDocument, dto.getContent());
        chunkRepository.saveAll(chunks);

        // Mark as processed
        savedDocument.setIsProcessed(true);
        documentRepository.save(savedDocument);

        return convertToDto(savedDocument, chunks.size());
    }

    /**
     * Chunk content into smaller pieces
     */
    private List<Chunk> chunkContent(Document document, String content) {
        List<Chunk> chunks = new ArrayList<>();

        // Split by words
        String[] words = content.split("\\s+");

        int chunkIndex = 0;
        StringBuilder currentChunk = new StringBuilder();
        int wordCount = 0;

        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;

            if (wordCount >= CHUNK_SIZE) {
                // Create chunk
                Chunk chunk = new Chunk();
                chunk.setDocument(document);
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContent(currentChunk.toString().trim());
                chunk.setTokenCount(wordCount);
                chunks.add(chunk);

                // Reset for next chunk
                currentChunk = new StringBuilder();
                wordCount = 0;
            }
        }

        // Add remaining content
        if (currentChunk.length() > 0) {
            Chunk chunk = new Chunk();
            chunk.setDocument(document);
            chunk.setChunkIndex(chunkIndex);
            chunk.setContent(currentChunk.toString().trim());
            chunk.setTokenCount(wordCount);
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * Get all documents for a user
     */
    public List<DocumentResponseDto> getUserDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Document> documents = documentRepository.findByUser(user);
        List<DocumentResponseDto> dtos = new ArrayList<>();

        for (Document doc : documents) {
            int chunkCount = chunkRepository.findByDocument(doc).size();
            dtos.add(convertToDto(doc, chunkCount));
        }

        return dtos;
    }

    /**
     * Get chunks for a document
     */
    public List<String> getDocumentChunks(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        List<Chunk> chunks = chunkRepository.findByDocumentOrderByChunkIndexAsc(document);
        List<String> chunkContents = new ArrayList<>();

        for (Chunk chunk : chunks) {
            chunkContents.add(chunk.getContent());
        }

        return chunkContents;
    }

    /**
     * Convert entity to DTO
     */
    private DocumentResponseDto convertToDto(Document document, int chunkCount) {
        DocumentResponseDto dto = new DocumentResponseDto();
        dto.setId(document.getId());
        dto.setUserId(document.getUser().getId());
        dto.setTitle(document.getTitle());
        dto.setFileName(document.getFileName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setDescription(document.getDescription());
        dto.setCategory(document.getCategory());
        dto.setSourceUrl(document.getSourceUrl());
        dto.setUploadDate(document.getUploadDate());
        dto.setIsProcessed(document.getIsProcessed());
        dto.setChunkCount(chunkCount);
        return dto;
    }
}
