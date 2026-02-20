package com.majerpro.learning_platform.repository.revision;

import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.model.revision.RevisionPlanStatus;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RevisionPlanRepository extends JpaRepository<RevisionPlan, Long> {

    @Query("SELECT r FROM RevisionPlan r WHERE r.user.id = :userId AND r.status = 'ACTIVE' AND r.nextReviewAt <= :now")
    List<RevisionPlan> findDuePlans(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM RevisionPlan p WHERE p.user.id = :userId")
    List<RevisionPlan> findByUserId(@Param("userId") Long userId);


    @Query("SELECT r FROM RevisionPlan r WHERE r.user.id = :userId AND r.skill.id = :skillId")
    Optional<RevisionPlan> findByUserAndSkill(@Param("userId") Long userId, @Param("skillId") Long skillId);

    Optional<RevisionPlan> findByUserAndSkill(User user, Skill skill);

    List<RevisionPlan> findByUser(User user);


    List<RevisionPlan> findByUserAndStatus(User user, RevisionPlanStatus status);

    List<RevisionPlan> findByUserAndStatusAndNextReviewAtBefore(
            User user,
            RevisionPlanStatus status,
            LocalDateTime cutoff
    );
}
