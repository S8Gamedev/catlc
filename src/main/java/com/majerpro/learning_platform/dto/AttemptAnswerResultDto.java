package com.majerpro.learning_platform.dto;

import lombok.Data;

@Data
public class AttemptAnswerResultDto {
    private Long questionId;
    private Long skillId;
    private String skillName;
    private Integer selectedOptionIndex;
    private Boolean correct;
}
