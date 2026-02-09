package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.SkillDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public Skill createSkill(SkillDto dto) {
        if (skillRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Skill already exists");
        }

        Skill skill = new Skill();
        skill.setName(dto.getName());
        skill.setDescription(dto.getDescription());
        skill.setCategory(dto.getCategory());
        skill.setDifficultyLevel(dto.getDifficultyLevel());

        return skillRepository.save(skill);
    }

    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
    }
}
