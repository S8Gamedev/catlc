package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubmitAnswerDto {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Selected option index is required")
    @Min(0) @Max(3)
    private Integer selectedOptionIndex;
}
