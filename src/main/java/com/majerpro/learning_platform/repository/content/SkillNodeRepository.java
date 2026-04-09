package com.majerpro.learning_platform.repository.content;

import com.majerpro.learning_platform.model.content.SkillNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillNodeRepository extends JpaRepository<SkillNode, Long> {
    List<SkillNode> findBySkillIdOrderByDepthAscNodeOrderAsc(Long skillId);
    void deleteBySkillId(Long skillId);
}