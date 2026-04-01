package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.SkillProgressDto;
import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.service.revision.RevisionTaskQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/skills")
public class SkillUiController {

    private final SkillRepository skillRepository;
    private final RevisionTaskQueryService revisionTaskQueryService;

    public SkillUiController(SkillRepository skillRepository,
                             RevisionTaskQueryService revisionTaskQueryService) {
        this.skillRepository = skillRepository;
        this.revisionTaskQueryService = revisionTaskQueryService;
    }

    @GetMapping("/{skillId}")
    public String skillDetail(@PathVariable Long skillId,
                              @RequestParam(defaultValue = "1") Long userId,
                              Model model) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        List<RevisionTaskDto> allTodayTasks = revisionTaskQueryService.getTodayTasks(userId);

        List<RevisionTaskDto> skillTasks = allTodayTasks.stream()
                .filter(t -> t.getSkillId() != null && t.getSkillId().equals(skillId))
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        int pendingTasks = skillTasks.size();
        int overdueTasks = (int) skillTasks.stream()
                .filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(now))
                .count();

        LocalDateTime nextDueAt = skillTasks.stream()
                .map(RevisionTaskDto::getDueAt)
                .filter(d -> d != null)
                .sorted()
                .findFirst()
                .orElse(null);

        SkillProgressDto progress = new SkillProgressDto();
        progress.setSkillId(skill.getId());
        progress.setSkillName(skill.getName());
        progress.setDescription(skill.getDescription());

        progress.setTotalTasks(pendingTasks);
        progress.setPendingTasks(pendingTasks);
        progress.setCompletedTasks(0);
        progress.setOverdueTasks(overdueTasks);

        double completionPercent = pendingTasks == 0 ? 100.0 : 0.0;
        progress.setCompletionPercent(completionPercent);
        progress.setNextDueAt(nextDueAt);

        if (!skillTasks.isEmpty()) {
            RevisionTaskDto firstTask = skillTasks.get(0);
            progress.setRecommendedQuizSize(firstTask.getRecommendedQuizSize());
            progress.setRecommendedDifficulty(
                    firstTask.getRecommendedDifficulty() != null
                            ? firstTask.getRecommendedDifficulty().toString()
                            : "MEDIUM"
            );
        } else {
            progress.setRecommendedQuizSize(5);
            progress.setRecommendedDifficulty("MEDIUM");
        }

        model.addAttribute("skill", skill);
        model.addAttribute("progress", progress);
        model.addAttribute("tasks", skillTasks);
        model.addAttribute("userId", userId);

        return "skills/detail";
    }
}