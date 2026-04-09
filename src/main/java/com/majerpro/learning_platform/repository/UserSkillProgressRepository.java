package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.UserSkillProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSkillProgressRepository extends JpaRepository<UserSkillProgress, Long> {

    Optional<UserSkillProgress> findByUserIdAndSkillId(Long userId, Long skillId);

    List<UserSkillProgress> findByUserId(Long userId);

    List<UserSkillProgress> findByUserIdOrderByMasteryDesc(Long userId);
}