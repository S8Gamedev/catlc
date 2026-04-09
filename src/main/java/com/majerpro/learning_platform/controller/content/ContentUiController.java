package com.majerpro.learning_platform.controller.content;

import com.majerpro.learning_platform.dto.content.ContentSkillListItemDto;
import com.majerpro.learning_platform.dto.content.CreateSkillFormDto;
import com.majerpro.learning_platform.dto.content.SkillContentDto;
import com.majerpro.learning_platform.dto.content.SkillNotesFormDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.content.SkillContent;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.content.SkillContentRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final UserRepository userRepository;

    public ContentUiController(SkillRepository skillRepository,
                               RevisionTaskRepository revisionTaskRepository,
                               SkillContentRepository skillContentRepository,
                               UserRepository userRepository) {
        this.skillRepository = skillRepository;
        this.revisionTaskRepository = revisionTaskRepository;
        this.skillContentRepository = skillContentRepository;
        this.userRepository = userRepository;
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
                    dto.setContentAvailable(
                            skillContentRepository.findBySkillIdAndCreatedById(skill.getId(), userId).isPresent()
                    );
                    return dto;
                })
                .sorted(Comparator.comparing(ContentSkillListItemDto::getSkillName))
                .toList();

        model.addAttribute("skills", skills);
        model.addAttribute("allSkills", skillRepository.findAll().stream()
                .sorted(Comparator.comparing(Skill::getName))
                .toList());
        model.addAttribute("userId", userId);

        CreateSkillFormDto createSkillForm = new CreateSkillFormDto();
        createSkillForm.setUserId(userId);
        model.addAttribute("createSkillForm", createSkillForm);

        SkillNotesFormDto notesForm = new SkillNotesFormDto();
        notesForm.setUserId(userId);
        model.addAttribute("form", notesForm);

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

        SkillContent savedContent = skillContentRepository
                .findBySkillIdAndCreatedById(skillId, userId)
                .orElse(null);

        if (savedContent != null) {
            dto.setTitle(savedContent.getTitle());
            dto.setSummary(savedContent.getSummary());
            dto.setContent(savedContent.getContent());
            dto.setContentAvailable(true);

            notesForm.setSkillId(skill.getId());
            notesForm.setSkillName(skill.getName());
            notesForm.setTitle(savedContent.getTitle());
            notesForm.setSummary(savedContent.getSummary());
            notesForm.setContent(savedContent.getContent());
            notesForm.setUserId(userId);
        } else {
            dto.setTitle(skill.getName() + " Review Notes");
            dto.setSummary("No stored content available for this skill yet.");
            dto.setContent("""
                    No saved notes/content found for this skill.

                    Create notes for this skill to make this page show real learning content.
                    """);
            dto.setContentAvailable(false);

            notesForm.setSkillId(skill.getId());
            notesForm.setSkillName(skill.getName());
            notesForm.setTitle(skill.getName() + " Notes");
            notesForm.setSummary(skill.getDescription());
            notesForm.setContent("");
            notesForm.setUserId(userId);
        }

        model.addAttribute("selectedSkill", dto);
        model.addAttribute("form", notesForm);
        return "content/overview";
    }

    @GetMapping("/new-skill")
    public String newSkillPage(@RequestParam(defaultValue = "1") Long userId, Model model) {
        CreateSkillFormDto form = new CreateSkillFormDto();
        form.setUserId(userId);
        model.addAttribute("form", form);
        return "content/new-skill";
    }

    @PostMapping("/save-skill")
    public String saveSkill(@ModelAttribute("form") CreateSkillFormDto form) {
        User user = userRepository.findById(form.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String requestedName = form.getName() == null ? "" : form.getName().trim();
        if (requestedName.isBlank()) {
            throw new RuntimeException("Skill name is required");
        }

        Skill skill = skillRepository.findByNameIgnoreCase(requestedName)
                .orElseGet(() -> {
                    Skill s = new Skill();
                    s.setName(requestedName);
                    s.setDescription(form.getDescription());
                    s.setCreatedBy(user);
                    return skillRepository.save(s);
                });

        return "redirect:/content/add-notes?skillId=" + skill.getId() + "&userId=" + user.getId();
    }

    @PostMapping("/learn-existing")
    public String learnExistingSkill(@RequestParam Long skillId,
                                     @RequestParam(defaultValue = "1") Long userId) {
        return "redirect:/content?skillId=" + skillId + "&userId=" + userId;
    }

    @GetMapping("/add-notes")
    public String addNotesPage(@RequestParam Long skillId,
                               @RequestParam(defaultValue = "1") Long userId,
                               Model model) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        SkillNotesFormDto form = new SkillNotesFormDto();
        form.setSkillId(skill.getId());
        form.setSkillName(skill.getName());
        form.setUserId(userId);

        skillContentRepository.findBySkillIdAndCreatedById(skillId, userId).ifPresentOrElse(saved -> {
            form.setTitle(saved.getTitle());
            form.setSummary(saved.getSummary());
            form.setContent(saved.getContent());
        }, () -> {
            form.setTitle(skill.getName() + " Notes");
            form.setSummary(skill.getDescription());
            form.setContent("");
        });

        model.addAttribute("form", form);
        model.addAttribute("userId", userId);
        return "content/add-notes";
    }

    @PostMapping("/save-notes")
    public String saveNotes(@ModelAttribute("form") SkillNotesFormDto form) throws IOException {
        User user = userRepository.findById(form.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(form.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        SkillContent skillContent = skillContentRepository
                .findBySkillIdAndCreatedById(skill.getId(), user.getId())
                .orElseGet(SkillContent::new);

        skillContent.setSkill(skill);
        skillContent.setCreatedBy(user);

        String finalContent = normalizeContent(form.getContent(), form.getTextFile());

        skillContent.setTitle(defaultIfBlank(form.getTitle(), skill.getName() + " Notes"));
        skillContent.setSummary(defaultIfBlank(form.getSummary(), "Notes for " + skill.getName()));
        skillContent.setContent(defaultIfBlank(finalContent, "No content provided."));

        MultipartFile file = form.getTextFile();
        if (file != null && !file.isEmpty()) {
            skillContent.setSourceFileName(file.getOriginalFilename());
        }

        skillContentRepository.save(skillContent);

        return "redirect:/content?skillId=" + skill.getId() + "&userId=" + user.getId();
    }

    private String normalizeContent(String typedContent, MultipartFile textFile) throws IOException {
        boolean hasTyped = typedContent != null && !typedContent.isBlank();
        boolean hasFile = textFile != null && !textFile.isEmpty();

        if (hasFile) {
            validateTxtFile(textFile);
            String fileText = new String(textFile.getBytes(), StandardCharsets.UTF_8);

            if (hasTyped) {
                return typedContent.trim() + "\n\n" + fileText.trim();
            }
            return fileText.trim();
        }

        return hasTyped ? typedContent.trim() : "";
    }

    private void validateTxtFile(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (!StringUtils.hasText(name) || !name.toLowerCase().endsWith(".txt")) {
            throw new RuntimeException("Only .txt files are supported");
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }
}