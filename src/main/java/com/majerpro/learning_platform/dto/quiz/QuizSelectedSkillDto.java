package com.majerpro.learning_platform.dto.quiz;

import lombok.Data;

@Data
public class QuizSelectedSkillDto {
    private Long skillId;
    private String skillName;
    private String description;
    private Double mastery;
    private Double retentionScore;
}