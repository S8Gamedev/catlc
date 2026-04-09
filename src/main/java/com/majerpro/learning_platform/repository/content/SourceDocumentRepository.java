package com.majerpro.learning_platform.repository.content;

import com.majerpro.learning_platform.model.content.SourceDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SourceDocumentRepository extends JpaRepository<SourceDocument, Long> {
    List<SourceDocument> findBySkillNodeIdOrderByRankingScoreDesc(Long skillNodeId);
}