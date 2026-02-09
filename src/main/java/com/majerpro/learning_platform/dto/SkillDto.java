package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillDto {

    @NotBlank(message = "Skill name is required")
    private String name;

    private String description;

    private String category;

    private String difficultyLevel; // "BEGINNER", "INTERMEDIATE", "ADVANCED"
}
