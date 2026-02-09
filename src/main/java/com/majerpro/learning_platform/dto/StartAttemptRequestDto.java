package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StartAttemptRequestDto {
    @NotNull(message = "User ID is required")
    private Long userId;
}
