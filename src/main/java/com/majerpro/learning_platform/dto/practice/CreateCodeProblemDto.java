package com.majerpro.learning_platform.dto.practice;

import lombok.Data;

@Data
public class CreateCodeProblemDto {
    private Long skillId; // optional
    private String title;
    private String prompt;
    private String category;
    private String difficultyLevel;
    private String sampleInput;
    private String expectedOutput;
}
