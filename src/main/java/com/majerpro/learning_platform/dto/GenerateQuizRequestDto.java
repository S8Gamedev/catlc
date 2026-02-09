package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateQuizRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Number of questions is required")
    @Min(value = 1, message = "Minimum 1 question")
    @Max(value = 50, message = "Maximum 50 questions")
    private Integer numberOfQuestions;

    @NotNull(message = "Weak threshold is required")
    private Double weakThreshold; // e.g. 50.0
}
