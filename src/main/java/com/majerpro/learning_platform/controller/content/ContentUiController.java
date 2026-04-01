package com.majerpro.learning_platform.controller.content;

import com.majerpro.learning_platform.dto.content.ContentSkillListItemDto;
import com.majerpro.learning_platform.dto.content.SkillContentDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.content.SkillContent;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.content.SkillContentRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/content")
public class ContentUiController {

    private final SkillRepository skillRepository;
    private final RevisionTaskRepository revisionTaskRepository;
    private final SkillContentRepository skillContentRepository;

    public ContentUiController(SkillRepository skillRepository,
                               RevisionTaskRepository revisionTaskRepository,
                               SkillContentRepository skillContentRepository) {
        this.skillRepository = skillRepository;
        this.revisionTaskRepository = revisionTaskRepository;
        this.skillContentRepository = skillContentRepository;
    }

    @GetMapping({"", "/"})
    public String contentOverview(@RequestParam(required = false) Long skillId,
                                  @RequestParam(defaultValue = "1") Long userId,
                                  Model model) {

        List<RevisionTask> userTasks = revisionTaskRepository.findByUserId(userId);

        List<ContentSkillListItemDto> skills = userTasks.stream()
                .filter(t -> t.getSkill() != null)
                .map(RevisionTask::getSkill)
                .filter(Objects::nonNull)
                .distinct()
                .map(skill -> {
                    List<RevisionTask> skillTasks = userTasks.stream()
                            .filter(t -> t.getSkill() != null && t.getSkill().getId().equals(skill.getId()))
                            .toList();

                    int pendingTasks = (int) skillTasks.stream()
                            .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                            .count();

                    LocalDateTime nextDueAt = skillTasks.stream()
                            .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                            .map(RevisionTask::getDueAt)
                            .filter(Objects::nonNull)
                            .sorted()
                            .findFirst()
                            .orElse(null);

                    ContentSkillListItemDto dto = new ContentSkillListItemDto();
                    dto.setSkillId(skill.getId());
                    dto.setSkillName(skill.getName());

                    try {
                        dto.setDescription(skill.getDescription());
                    } catch (Exception e) {
                        dto.setDescription("No description available.");
                    }

                    dto.setPendingTasks(pendingTasks);
                    dto.setNextDueAt(nextDueAt);
                    return dto;
                })
                .sorted(Comparator.comparing(ContentSkillListItemDto::getSkillName))
                .toList();

        model.addAttribute("skills", skills);
        model.addAttribute("userId", userId);

        if (skillId == null) {
            model.addAttribute("selectedSkill", null);
            return "content/overview";
        }

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        SkillContentDto dto = new SkillContentDto();
        dto.setSkillId(skill.getId());
        dto.setSkillName(skill.getName());

        String recommendedDifficulty = revisionTaskRepository
                .findByUserIdAndSkillIdOrderByDueAtAsc(userId, skillId)
                .stream()
                .map(RevisionTask::getRecommendedDifficulty)
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .orElse("MEDIUM");

        dto.setRecommendedDifficulty(recommendedDifficulty);

        SkillContent savedContent = skillContentRepository.findBySkillId(skillId).orElse(null);

        if (savedContent != null) {
            dto.setTitle(savedContent.getTitle());
            dto.setSummary(savedContent.getSummary());
            dto.setContent(savedContent.getContent());
            dto.setContentAvailable(true);
        } else {
            dto.setTitle(skill.getName() + " Review Notes");
            dto.setSummary("No stored content available for this skill yet.");
            dto.setContent("""
                    No saved notes/content found for this skill.

                    Create a SkillContent record in the database for this skill to make this page show real learning notes.
                    """);
            dto.setContentAvailable(false);
        }

        model.addAttribute("selectedSkill", dto);
        return "content/overview";
    }
}