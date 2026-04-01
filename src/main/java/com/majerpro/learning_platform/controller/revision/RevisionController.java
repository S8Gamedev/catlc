package com.majerpro.learning_platform.controller.revision;

import com.majerpro.learning_platform.dto.revision.CompleteRevisionTaskRequestDto;
import com.majerpro.learning_platform.dto.revision.RevisionExplainDto;
import com.majerpro.learning_platform.dto.revision.RevisionEventDto;
import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import com.majerpro.learning_platform.repository.revision.RevisionPlanRepository;
import com.majerpro.learning_platform.service.revision.SpacedRepetitionEngineService;
import com.majerpro.learning_platform.service.revision.RevisionOutcomeService;
import com.majerpro.learning_platform.service.revision.RevisionPlannerService;
import com.majerpro.learning_platform.service.revision.RevisionTaskQueryService;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/api/revision")
public class RevisionController {

    private final RevisionPlannerService plannerService;
    private final RevisionTaskQueryService queryService;
    private final RevisionOutcomeService outcomeService;

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final RevisionPlanRepository revisionPlanRepository;
    private final RevisionEventRepository revisionEventRepository;
    private final SpacedRepetitionEngineService engine;

    public RevisionController(
            RevisionPlannerService plannerService,
            RevisionTaskQueryService queryService,
            RevisionOutcomeService outcomeService,
            UserRepository userRepository,
            SkillRepository skillRepository,
            RevisionPlanRepository revisionPlanRepository,
            RevisionEventRepository revisionEventRepository,
            SpacedRepetitionEngineService engine
    ) {
        this.plannerService = plannerService;
        this.queryService = queryService;
        this.outcomeService = outcomeService;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.revisionPlanRepository = revisionPlanRepository;
        this.revisionEventRepository = revisionEventRepository;
        this.engine = engine;
    }

    @PostMapping("/rebuild")
    public ResponseEntity<?> rebuild(@RequestParam Long userId) {
        plannerService.rebuildForUser(userId);
        return ResponseEntity.ok("REBUILT");
    }

    @GetMapping("/today")
    public ResponseEntity<List<RevisionTaskDto>> today(@RequestParam Long userId) {
        return ResponseEntity.ok(queryService.getTodayTasks(userId));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ResponseEntity<?> completeTask(@PathVariable Long taskId, @Valid @RequestBody CompleteRevisionTaskRequestDto req) {
        outcomeService.completeTask(taskId, req.getRecallScore());
        return ResponseEntity.ok("DONE");
    }

    @GetMapping("/explain")
    public ResponseEntity<RevisionExplainDto> explain(@RequestParam Long userId, @RequestParam Long skillId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Skill skill = skillRepository.findById(skillId).orElseThrow(() -> new RuntimeException("Skill not found"));

        RevisionPlan plan = revisionPlanRepository.findByUserAndSkill(user, skill)
                .orElseThrow(() -> new RuntimeException("Revision plan not found. Call /api/revision/rebuild first."));

        double r = engine.computeRecallProbability(plan, LocalDateTime.now());

        RevisionExplainDto dto = new RevisionExplainDto();
        dto.setUserId(userId);
        dto.setSkillId(skillId);
        dto.setSkillName(skill.getName());
        dto.setStabilityDays(plan.getStabilityDays());
        dto.setDifficulty(plan.getDifficulty());
        dto.setLastReviewedAt(plan.getLastReviewedAt());
        dto.setNextReviewAt(plan.getNextReviewAt());
        dto.setCurrentRecallProbability(r);

        dto.setRecentEvents(
                revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan)
                        .stream()
                        .map(e -> {
                            RevisionEventDto ed = new RevisionEventDto();
                            ed.setEventType(e.getEventType().name());
                            ed.setSource(e.getSource());
                            ed.setReason(e.getReason());
                            ed.setComputedRecallProbability(e.getComputedRecallProbability());
                            ed.setRecallScore(e.getRecallScore());
                            ed.setOldStabilityDays(e.getOldStabilityDays());
                            ed.setNewStabilityDays(e.getNewStabilityDays());
                            ed.setOldDifficulty(e.getOldDifficulty());
                            ed.setNewDifficulty(e.getNewDifficulty());
                            ed.setOldNextReviewAt(e.getOldNextReviewAt());
                            ed.setNewNextReviewAt(e.getNewNextReviewAt());
                            ed.setCreatedAt(e.getCreatedAt());
                            return ed;
                        })
                        .toList()
        );




        return ResponseEntity.ok(dto);
    }
    @GetMapping("/")  // /revisions → UI page
    public String todayTasksUi(@RequestParam(defaultValue = "1") Long userId, Model model) {
        List<RevisionTaskDto> tasks = queryService.getTodayTasks(userId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("userId", userId);
        model.addAttribute("taskCount", tasks.size());
        return "revisions/today";  // templates/revisions/today.html
    }
}
