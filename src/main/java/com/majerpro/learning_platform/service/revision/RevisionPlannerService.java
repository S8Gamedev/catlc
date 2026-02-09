package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.model.KnowledgeState;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.*;
import com.majerpro.learning_platform.repository.KnowledgeStateRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.practice.CodeProblemRepository;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import com.majerpro.learning_platform.repository.revision.RevisionPlanRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RevisionPlannerService {

    private final RevisionPlanRepository revisionPlanRepository;
    private final RevisionTaskRepository revisionTaskRepository;
    private final RevisionEventRepository revisionEventRepository;

    private final KnowledgeStateRepository knowledgeStateRepository;
    private final UserRepository userRepository;

    private final SpacedRepetitionEngineService engine;

    private final CodeProblemRepository codeProblemRepository; // for CODE task availability

    // Thresholds (tune later)
    private final double R_WARN = 0.75;
    private final double R_MIN = 0.60;

    public RevisionPlannerService(
            RevisionPlanRepository revisionPlanRepository,
            RevisionTaskRepository revisionTaskRepository,
            RevisionEventRepository revisionEventRepository,
            KnowledgeStateRepository knowledgeStateRepository,
            UserRepository userRepository,
            SpacedRepetitionEngineService engine,
            CodeProblemRepository codeProblemRepository
    ) {
        this.revisionPlanRepository = revisionPlanRepository;
        this.revisionTaskRepository = revisionTaskRepository;
        this.revisionEventRepository = revisionEventRepository;
        this.knowledgeStateRepository = knowledgeStateRepository;
        this.userRepository = userRepository;
        this.engine = engine;
        this.codeProblemRepository = codeProblemRepository;
    }

    @Transactional
    public void rebuildForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<KnowledgeState> states = knowledgeStateRepository.findByUser(user);

        for (KnowledgeState ks : states) {
            Skill skill = ks.getSkill();

            RevisionPlan plan = revisionPlanRepository.findByUserAndSkill(user, skill)
                    .orElseGet(() -> {
                        RevisionPlan p = new RevisionPlan();
                        p.setUser(user);
                        p.setSkill(skill);
                        p.setStatus(RevisionPlanStatus.ACTIVE);

                        // Initialize using current KnowledgeState
                        // If lastPracticed exists, treat that as lastReviewedAt to bootstrap schedule.
                        p.setLastReviewedAt(ks.getLastPracticed());
                        p.setStabilityDays(3.0);
                        p.setDifficulty(0.5);
                        p.setReviewCount(0);
                        p.setLapseCount(0);
                        return p;
                    });

            // If plan has never been reviewed, bootstrap from knowledge state
            if (plan.getLastReviewedAt() == null) {
                plan.setLastReviewedAt(ks.getLastPracticed());
            }
            if (plan.getLastReviewedAt() == null) {
                plan.setLastReviewedAt(LocalDateTime.now().minusDays(1)); // bootstrap
            }

            // Schedule next review
            plan.setNextReviewAt(engine.computeNextReviewAt(plan, 0.85));
            RevisionPlan savedPlan = revisionPlanRepository.save(plan);

            // Create/refresh tasks based on decay thresholds
            generateTaskIfNeeded(savedPlan);
        }
    }

    @Transactional
    public void generateTaskIfNeeded(RevisionPlan plan) {
        if (plan.getStatus() != RevisionPlanStatus.ACTIVE) return;

        LocalDateTime now = LocalDateTime.now();
        double r = engine.computeRecallProbability(plan, now);

        LocalDateTime dueAt;
        String reason;

        if (r <= R_MIN) {
            dueAt = now;
            reason = "RECALL_BELOW_MIN";
        } else if (r <= R_WARN) {
            dueAt = now.plusDays(1);
            reason = "RECALL_BELOW_WARN";
        } else {
            return; // no task needed
        }

        // Decide task type: CODE if code problems exist for this skill, else QUIZ
        RevisionTaskType type = RevisionTaskType.QUIZ;
        if (plan.getSkill() != null) {
            long count = codeProblemRepository.countBySkill(plan.getSkill());
            if (count > 0) type = RevisionTaskType.CODE;
        }

        // Prevent duplicates for same day/skill
        boolean exists = revisionTaskRepository.existsByUserAndSkillAndStatusAndDueAt(
                plan.getUser(),
                plan.getSkill(),
                RevisionTaskStatus.PENDING,
                dueAt.withHour(0).withMinute(0).withSecond(0).withNano(0)
        );

        RevisionTask task = new RevisionTask();
        task.setUser(plan.getUser());
        task.setSkill(plan.getSkill());
        task.setTaskType(type);
        task.setStatus(RevisionTaskStatus.PENDING);
        task.setDueAt(dueAt.withHour(0).withMinute(0).withSecond(0).withNano(0));
        task.setRecommendedQuizSize(5);
        task.setRecommendedDifficulty("AUTO");

        if (!exists) {
            revisionTaskRepository.save(task);

            RevisionEvent ev = new RevisionEvent();
            ev.setPlan(plan);
            ev.setEventType(RevisionEventType.TASK_CREATED);
            ev.setSource("SCHEDULER");
            ev.setReason(reason);
            ev.setComputedRecallProbability(r);
            ev.setOldNextReviewAt(plan.getNextReviewAt());
            ev.setNewNextReviewAt(plan.getNextReviewAt());
            ev.setOldStabilityDays(plan.getStabilityDays());
            ev.setNewStabilityDays(plan.getStabilityDays());
            ev.setOldDifficulty(plan.getDifficulty());
            ev.setNewDifficulty(plan.getDifficulty());
            revisionEventRepository.save(ev);
        }
    }
}
