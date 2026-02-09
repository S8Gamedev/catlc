package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.KnowledgeState;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface KnowledgeStateRepository extends JpaRepository<KnowledgeState, Long> {

    Optional<KnowledgeState> findByUserAndSkill(User user, Skill skill);

    List<KnowledgeState> findByUser(User user);

    List<KnowledgeState> findByUserOrderByMasteryScoreAsc(User user);

    @Query("SELECT ks FROM KnowledgeState ks WHERE ks.user = :user AND ks.masteryScore < :threshold")
    List<KnowledgeState> findWeakSkills(User user, Double threshold);
    List<KnowledgeState> findByUserAndMasteryScoreLessThan(User user, double masteryScore);
}
