// src/main/java/com/majerpro/learning_platform/service/revision/RevisionService.java
package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.dto.revision.CompleteRevisionTaskRequestDto;
import com.majerpro.learning_platform.dto.revision.RevisionExplainDto;
import com.majerpro.learning_platform.dto.revision.RevisionEventDto;
import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.dto.revision.TodayTasksResponseDto;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.model.revision.RevisionPlanStatus;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import com.majerpro.learning_platform.repository.revision.RevisionPlanRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RevisionService {

    private final RevisionTaskRepository revisionTaskRepository;
    private final RevisionPlanRepository revisionPlanRepository;
    private final RevisionEventRepository revisionEventRepository;

    private final RevisionTaskQueryService revisionTaskQueryService;
    private final SpacedRepetitionEngineService engine;

    public RevisionService(
            RevisionTaskRepository revisionTaskRepository,
            RevisionPlanRepository revisionPlanRepository,
            RevisionEventRepository revisionEventRepository,
            RevisionTaskQueryService revisionTaskQueryService,
            SpacedRepetitionEngineService engine
    ) {
        this.revisionTaskRepository = revisionTaskRepository;
        this.revisionPlanRepository = revisionPlanRepository;
        this.revisionEventRepository = revisionEventRepository;
        this.revisionTaskQueryService = revisionTaskQueryService;
        this.engine = engine;
    }

    // 1) Today’s tasks -------------------------------------------------

    @Transactional
    public TodayTasksResponseDto getTodayTasks(Long userId) {
        List<RevisionTaskDto> list = revisionTaskQueryService.getTodayTasks(userId); // uses your existing query service
        return new TodayTasksResponseDto(list, list.size());
    }

    // 2) Complete a task ----------------------------------------------

    @Transactional
    public void completeTask(Long taskId, CompleteRevisionTaskRequestDto body) {
        RevisionTask task = revisionTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("RevisionTask not found"));

        if (task.getStatus() != RevisionTaskStatus.PENDING) {
            throw new RuntimeException("Task is not in PENDING state");
        }

        // Find or create plan for this (user, skill)
        RevisionPlan plan = revisionPlanRepository
                .findByUserAndSkill(task.getUser(), task.getSkill())
                .orElseThrow(() -> new RuntimeException("RevisionPlan not found for task"));

        // Update task status
        task.setStatus(RevisionTaskStatus.DONE);
        task.setUpdatedAt(LocalDateTime.now());
        revisionTaskRepository.save(task);

        // Apply outcome + reschedule (logs RevisionEvent internally)
        double recallScore = body.getRecallScore();
        String source = switch (task.getTaskType()) {
            case CODE -> "CODE";
            case QUIZ -> "QUIZ";
            case READ_REVIEW -> "READ_REVIEW";
        };
        engine.applyOutcomeAndReschedule(plan, recallScore, source, "USER_COMPLETED");
        // plan + event saved inside engine
    }

    // 3) Skip a task ---------------------------------------------------

    @Transactional
    public void skipTask(Long taskId, String reason) {
        RevisionTask task = revisionTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("RevisionTask not found"));

        if (task.getStatus() != RevisionTaskStatus.PENDING) {
            throw new RuntimeException("Task is not in PENDING state");
        }

        RevisionPlan plan = revisionPlanRepository
                .findByUserAndSkill(task.getUser(), task.getSkill())
                .orElseThrow(() -> new RuntimeException("RevisionPlan not found for task"));

        task.setStatus(RevisionTaskStatus.SKIPPED);
        task.setUpdatedAt(LocalDateTime.now());
        revisionTaskRepository.save(task);

        // For skip: do NOT change stability/difficulty, just push next review a bit
        LocalDateTime oldNext = plan.getNextReviewAt();
        plan.setNextReviewAt(
                (oldNext != null ? oldNext : LocalDateTime.now()).plusDays(1)
        );
        revisionPlanRepository.save(plan);

        // Log as RESCHEDULED / SKIPPED without changing stability
        var ev = new com.majerpro.learning_platform.model.revision.RevisionEvent();
        ev.setPlan(plan);
        ev.setEventType(com.majerpro.learning_platform.model.revision.RevisionEventType.SKIPPED);
        ev.setSource("USER");
        ev.setReason(reason != null ? reason : "USER_SKIPPED");
        ev.setOldNextReviewAt(oldNext);
        ev.setNewNextReviewAt(plan.getNextReviewAt());
        ev.setOldStabilityDays(plan.getStabilityDays());
        ev.setNewStabilityDays(plan.getStabilityDays());
        ev.setOldDifficulty(plan.getDifficulty());
        ev.setNewDifficulty(plan.getDifficulty());
        revisionEventRepository.save(ev);
    }

    // 4) Explain a plan (optional, for later controller) --------------

    @Transactional
    public RevisionExplainDto explainPlan(Long userId, Long skillId) {
        RevisionPlan plan = revisionPlanRepository.findByUserAndSkill(userId, skillId)
                .orElseThrow(() -> new RuntimeException("RevisionPlan not found"));

        RevisionExplainDto dto = new RevisionExplainDto();
        dto.setUserId(plan.getUser().getId());
        dto.setSkillId(plan.getSkill().getId());
        dto.setSkillName(plan.getSkill().getName());
        dto.setStabilityDays(plan.getStabilityDays());
        dto.setDifficulty(plan.getDifficulty());
        dto.setLastReviewedAt(plan.getLastReviewedAt());
        dto.setNextReviewAt(plan.getNextReviewAt());
        dto.setCurrentRecallProbability(engine.computeRecallProbability(plan, LocalDateTime.now()));

        var events = revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan);
        List<RevisionEventDto> evDtos = events.stream().map(ev -> {
            RevisionEventDto e = new RevisionEventDto();
            e.setEventType(ev.getEventType().name());
            e.setSource(ev.getSource());
            e.setReason(ev.getReason());
            e.setComputedRecallProbability(ev.getComputedRecallProbability());
            e.setRecallScore(ev.getRecallScore());
            e.setOldStabilityDays(ev.getOldStabilityDays());
            e.setNewStabilityDays(ev.getNewStabilityDays());
            e.setOldDifficulty(ev.getOldDifficulty());
            e.setNewDifficulty(ev.getNewDifficulty());
            e.setOldNextReviewAt(ev.getOldNextReviewAt());
            e.setNewNextReviewAt(ev.getNewNextReviewAt());
            e.setCreatedAt(ev.getCreatedAt());
            return e;
        }).toList();

        dto.setRecentEvents(evDtos);
        return dto;
    }
}
