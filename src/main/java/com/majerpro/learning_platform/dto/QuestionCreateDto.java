package com.majerpro.learning_platform.dto;

import com.majerpro.learning_platform.model.QuestionDifficulty;
import com.majerpro.learning_platform.model.QuestionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateDto {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Type is required")
    private QuestionType type; // MCQ

    @NotNull(message = "Difficulty is required")
    private QuestionDifficulty difficulty; // EASY/MEDIUM/HARD

    @NotBlank(message = "Prompt is required")
    private String prompt;

    @NotNull(message = "Options are required")
    @Size(min = 4, max = 4, message = "Options must contain exactly 4 items")
    private List<@NotBlank String> options;

    @NotNull(message = "Correct option index is required")
    @Min(value = 0, message = "Correct option index must be 0..3")
    @Max(value = 3, message = "Correct option index must be 0..3")
    private Integer correctOptionIndex;

    private String explanation;
}
