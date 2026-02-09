package com.majerpro.learning_platform.repository.revision;

import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.model.revision.RevisionPlanStatus;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RevisionPlanRepository extends JpaRepository<RevisionPlan, Long> {

    Optional<RevisionPlan> findByUserAndSkill(User user, Skill skill);

    List<RevisionPlan> findByUserAndStatus(User user, RevisionPlanStatus status);

    List<RevisionPlan> findByUserAndStatusAndNextReviewAtBefore(User user, RevisionPlanStatus status, LocalDateTime cutoff);
}
