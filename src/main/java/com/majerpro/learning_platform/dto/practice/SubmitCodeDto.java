package com.majerpro.learning_platform.dto.practice;

import lombok.Data;

@Data
public class SubmitCodeDto {
    private Long userId;
    private Long problemId;
    private Integer languageId;
    private String sourceCode;
    private String stdin; // optional override; if null use problem.sampleInput
}
