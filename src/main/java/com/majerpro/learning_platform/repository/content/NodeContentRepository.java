package com.majerpro.learning_platform.repository.content;

import com.majerpro.learning_platform.model.content.NodeContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeContentRepository extends JpaRepository<NodeContent, Long> {
    Optional<NodeContent> findBySkillNodeId(Long skillNodeId);
}