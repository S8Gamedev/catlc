package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.SkillDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @PostMapping
    public ResponseEntity<?> createSkill(@Valid @RequestBody SkillDto dto) {
        try {
            Skill skill = skillService.createSkill(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(skill);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        return ResponseEntity.ok(skillService.getAllSkills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(skillService.getSkillById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
