package com.majerpro.learning_platform.dto.revision;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompleteRevisionTaskRequestDto {

    @NotNull
    @Min(0)
    @Max(1)
    private Double recallScore; // 0.0..1.0
}
