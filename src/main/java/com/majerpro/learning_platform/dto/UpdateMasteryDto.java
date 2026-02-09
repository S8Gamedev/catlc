package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMasteryDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Learning gain is required")
    private Double learningGain; // Points gained from this practice (0.0 to 10.0)
}
