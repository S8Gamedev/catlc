package com.majerpro.learning_platform.repository.revision;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RevisionTaskRepository extends JpaRepository<RevisionTask, Long> {

    // ADD THIS NEW METHOD at the end (before closing brace)
    @Query("SELECT t FROM RevisionTask t WHERE t.user.id = :userId")
    List<RevisionTask> findByUserId(@Param("userId") Long userId);



    // existing: fetch by User entity
    List<RevisionTask> findByUserAndStatusAndDueAtBefore(
            User user,
            RevisionTaskStatus status,
            LocalDateTime cutoff
    );


    boolean existsByUserAndSkillAndStatusAndDueAt(
            User user,
            Skill skill,
            RevisionTaskStatus status,
            LocalDateTime dueAt
    );
    // NEW - Stage 9 Analytics
    List<RevisionTask> findByUser(User user);

    // ✅ NEW: fetch today's tasks by userId directly (for /api/revision/today?userId=1)
    List<RevisionTask> findByUserIdAndStatusAndDueAtBeforeOrderByDueAtAsc(
            Long userId,
            RevisionTaskStatus status,
            LocalDateTime cutoff
    );

    List<RevisionTask> findByUserIdAndSkillIdOrderByDueAtAsc(Long userId, Long skillId);

    long countByUserIdAndSkillId(Long userId, Long skillId);

    long countByUserIdAndSkillIdAndStatus(Long userId, Long skillId, RevisionTaskStatus status);
}
