package com.majerpro.learning_platform.service.content;

import com.majerpro.learning_platform.model.Chunk;
import com.majerpro.learning_platform.model.Document;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.content.IngestionRun;
import com.majerpro.learning_platform.model.content.SkillNode;
import com.majerpro.learning_platform.repository.ChunkRepository;
import com.majerpro.learning_platform.repository.DocumentRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.service.VectorStoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillRagBridgeService {

    private static final int CHUNK_SIZE = 350;

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final UserRepository userRepository;
    private final VectorStoreService vectorStoreService;

    public SkillRagBridgeService(DocumentRepository documentRepository,
                                 ChunkRepository chunkRepository,
                                 UserRepository userRepository,
                                 VectorStoreService vectorStoreService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.userRepository = userRepository;
        this.vectorStoreService = vectorStoreService;
    }

    @Transactional
    public Long storeSkillNodeInRag(Long userId,
                                    Skill skill,
                                    SkillNode node,
                                    IngestionRun run,
                                    String summary,
                                    List<String> keyPoints,
                                    String example,
                                    String sourceUrl) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StringBuilder content = new StringBuilder();
        content.append("Skill: ").append(skill.getName()).append("\n");
        content.append("Topic: ").append(node.getTitle()).append("\n");

        if (node.getObjective() != null && !node.getObjective().isBlank()) {
            content.append("Objective: ").append(node.getObjective()).append("\n\n");
        }

        if (summary != null && !summary.isBlank()) {
            content.append("Summary:\n").append(summary).append("\n\n");
        }

        if (keyPoints != null && !keyPoints.isEmpty()) {
            content.append("Key Points:\n");
            for (String kp : keyPoints) {
                content.append("- ").append(kp).append("\n");
            }
            content.append("\n");
        }

        if (example != null && !example.isBlank()) {
            content.append("Example:\n").append(example).append("\n");
        }

        Document document = new Document();
        document.setUser(user);
        document.setTitle(skill.getName() + " - " + node.getTitle());
        document.setFileName("ingested-skill-" + skill.getId() + "-node-" + node.getId() + ".txt");
        document.setFileType("AI_INGESTED");
        document.setFileSize((long) content.length());
        document.setDescription("Auto-ingested study content for skill node");
        document.setCategory(skill.getName());
        document.setSourceUrl(sourceUrl != null ? sourceUrl : "INGESTION_RUN_" + run.getId());
        document.setIsProcessed(false);

        Document savedDocument = documentRepository.save(document);

        List<Chunk> chunks = chunkContent(savedDocument, content.toString());
        chunkRepository.saveAll(chunks);

        savedDocument.setIsProcessed(true);
        documentRepository.save(savedDocument);

        vectorStoreService.generateEmbeddingsForDocument(savedDocument.getId());

        return savedDocument.getId();
    }

    private List<Chunk> chunkContent(Document document, String content) {
        List<Chunk> chunks = new ArrayList<>();
        String[] words = content.split("\\s+");

        int chunkIndex = 0;
        int wordCount = 0;
        StringBuilder currentChunk = new StringBuilder();

        for (String word : words) {
            currentChunk.append(word).append(" ");
            wordCount++;

            if (wordCount >= CHUNK_SIZE) {
                Chunk chunk = new Chunk();
                chunk.setDocument(document);
                chunk.setChunkIndex(chunkIndex++);
                chunk.setContent(currentChunk.toString().trim());
                chunk.setTokenCount(wordCount);
                chunks.add(chunk);

                currentChunk = new StringBuilder();
                wordCount = 0;
            }
        }

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
}