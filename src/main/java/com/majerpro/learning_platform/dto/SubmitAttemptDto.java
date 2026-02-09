package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAttemptDto {
    @NotNull(message = "Answers are required")
    private List<SubmitAnswerDto> answers;
}
