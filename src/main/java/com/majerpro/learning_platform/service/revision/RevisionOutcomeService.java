package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.model.revision.RevisionPlanStatus;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import com.majerpro.learning_platform.repository.revision.RevisionPlanRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevisionOutcomeService {

    private final RevisionPlanRepository revisionPlanRepository;
    private final RevisionTaskRepository revisionTaskRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    private final SpacedRepetitionEngineService engine;
    private final RevisionPlannerService planner;

    public RevisionOutcomeService(
            RevisionPlanRepository revisionPlanRepository,
            RevisionTaskRepository revisionTaskRepository,
            UserRepository userRepository,
            SkillRepository skillRepository,
            SpacedRepetitionEngineService engine,
            RevisionPlannerService planner
    ) {
        this.revisionPlanRepository = revisionPlanRepository;
        this.revisionTaskRepository = revisionTaskRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.engine = engine;
        this.planner = planner;
    }

    @Transactional
    public void applyOutcome(Long userId, Long skillId, double recallScore, String source, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        RevisionPlan plan = revisionPlanRepository.findByUserAndSkill(user, skill)
                .orElseGet(() -> {
                    RevisionPlan p = new RevisionPlan();
                    p.setUser(user);
                    p.setSkill(skill);
                    p.setStatus(RevisionPlanStatus.ACTIVE);
                    return p;
                });

        plan = revisionPlanRepository.save(plan);

        engine.applyOutcomeAndReschedule(plan, recallScore, source, reason);
        revisionPlanRepository.save(plan);

        // After reschedule, generate new tasks if decay already low (edge cases)
        planner.generateTaskIfNeeded(plan);
    }

    @Transactional
    public void completeTask(Long taskId, double recallScore) {
        RevisionTask task = revisionTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (task.getStatus() != RevisionTaskStatus.PENDING) {
            throw new RuntimeException("Task already completed/skipped");
        }

        task.setStatus(RevisionTaskStatus.DONE);
        revisionTaskRepository.save(task);

        applyOutcome(
                task.getUser().getId(),
                task.getSkill().getId(),
                recallScore,
                "MANUAL_TASK",
                "TASK_COMPLETED"
        );
    }
}
