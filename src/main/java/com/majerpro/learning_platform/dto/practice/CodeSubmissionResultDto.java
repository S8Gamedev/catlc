package com.majerpro.learning_platform.dto.practice;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeSubmissionResultDto {
    private Long submissionId;
    private Long problemId;
    private String judgeToken;

    private String status;
    private Boolean isCorrect;
    private Double score;

    private String stdout;
    private String stderr;
    private String compileOutput;

    private LocalDateTime createdAt;
    private LocalDateTime evaluatedAt;
}
