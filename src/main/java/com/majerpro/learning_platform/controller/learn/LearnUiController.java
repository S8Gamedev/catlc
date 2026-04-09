package com.majerpro.learning_platform.controller.learn;

import com.majerpro.learning_platform.dto.learn.LearnSkillListItemDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserSkillProgressRepository;
import com.majerpro.learning_platform.service.progress.ProgressTrackingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/learn")
public class LearnUiController {

    private final SkillRepository skillRepository;
    private final UserSkillProgressRepository progressRepository;
    private final ProgressTrackingService progressTrackingService;

    public LearnUiController(SkillRepository skillRepository,
                             UserSkillProgressRepository progressRepository,
                             ProgressTrackingService progressTrackingService) {
        this.skillRepository = skillRepository;
        this.progressRepository = progressRepository;
        this.progressTrackingService = progressTrackingService;
    }

    @GetMapping({"", "/"})
    public String learnPage(@RequestParam(defaultValue = "1") Long userId, Model model) {

        Set<Long> addedSkillIds = progressRepository.findByUserId(userId).stream()
                .map(p -> p.getSkill().getId())
                .collect(Collectors.toSet());

        List<LearnSkillListItemDto> skills = skillRepository.findAll().stream()
                .map(skill -> {
                    LearnSkillListItemDto dto = new LearnSkillListItemDto();
                    dto.setSkillId(skill.getId());
                    dto.setSkillName(skill.getName());
                    dto.setDescription(skill.getDescription());
                    dto.setAlreadyAdded(addedSkillIds.contains(skill.getId()));
                    return dto;
                })
                .sorted((a, b) -> a.getSkillName().compareToIgnoreCase(b.getSkillName()))
                .toList();

        model.addAttribute("skills", skills);
        model.addAttribute("userId", userId);

        return "learn/overview";
    }

    @PostMapping("/add")
    public String addSkill(@RequestParam Long skillId,
                           @RequestParam(defaultValue = "1") Long userId) {

        progressTrackingService.ensureSkillTracked(userId, skillId);

        return "redirect:/learn?userId=" + userId;
    }
}