package com.majerpro.learning_platform.controller.content;

import com.majerpro.learning_platform.dto.content.ContentSkillCardDto;
import com.majerpro.learning_platform.dto.content.SkillNotesFormDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.content.SkillContent;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.content.SkillContentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/content")
public class ContentAdminController {

    private final SkillRepository skillRepository;
    private final SkillContentRepository skillContentRepository;

    public ContentAdminController(SkillRepository skillRepository,
                                  SkillContentRepository skillContentRepository) {
        this.skillRepository = skillRepository;
        this.skillContentRepository = skillContentRepository;
    }

    @GetMapping({"", "/"})
    public String adminHome(Model model) {
        List<ContentSkillCardDto> skills = skillRepository.findAll().stream()
                .map(skill -> {
                    ContentSkillCardDto dto = new ContentSkillCardDto();
                    dto.setSkillId(skill.getId());
                    dto.setSkillName(skill.getName());
                    dto.setDescription(skill.getDescription());
                    dto.setContentAvailable(skillContentRepository.existsBySkillId(skill.getId()));
                    return dto;
                })
                .sorted(Comparator.comparing(ContentSkillCardDto::getSkillName))
                .toList();

        model.addAttribute("skills", skills);
        return "content/admin-list";
    }

    @GetMapping("/edit")
    public String editContent(@RequestParam Long skillId, Model model) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        SkillNotesFormDto form = new SkillNotesFormDto();
        form.setSkillId(skill.getId());
        form.setSkillName(skill.getName());

        skillContentRepository.findBySkillId(skillId).ifPresentOrElse(saved -> {
            form.setTitle(saved.getTitle());
            form.setSummary(saved.getSummary());
            form.setContent(saved.getContent());
        }, () -> {
            form.setTitle(skill.getName() + " Notes");
            form.setSummary(skill.getDescription());
            form.setContent("");
        });

        model.addAttribute("form", form);
        return "content/admin-edit";
    }

    @PostMapping("/save")
    public String saveContent(@ModelAttribute("form") SkillNotesFormDto form) {
        Skill skill = skillRepository.findById(form.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        SkillContent skillContent = skillContentRepository.findBySkillId(skill.getId())
                .orElseGet(SkillContent::new);

        skillContent.setSkill(skill);
        skillContent.setTitle(form.getTitle());
        skillContent.setSummary(form.getSummary());
        skillContent.setContent(form.getContent());

        skillContentRepository.save(skillContent);

        return "redirect:/admin/content";
    }
}