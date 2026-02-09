package com.majerpro.learning_platform.repository.revision;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RevisionTaskRepository extends JpaRepository<RevisionTask, Long> {

    List<RevisionTask> findByUserAndStatusAndDueAtBefore(User user, RevisionTaskStatus status, LocalDateTime cutoff);

    boolean existsByUserAndSkillAndStatusAndDueAt(User user, Skill skill, RevisionTaskStatus status, LocalDateTime dueAt);
}
