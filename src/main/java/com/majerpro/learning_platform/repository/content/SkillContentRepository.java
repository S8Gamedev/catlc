package com.majerpro.learning_platform.repository.content;

import com.majerpro.learning_platform.model.content.SkillContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillContentRepository extends JpaRepository<SkillContent, Long> {
    Optional<SkillContent> findBySkillId(Long skillId);
}