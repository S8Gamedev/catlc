package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByName(String name);

    List<Skill> findByCategory(String category);

    List<Skill> findByDifficultyLevel(String difficultyLevel);

    boolean existsByName(String name);
}
