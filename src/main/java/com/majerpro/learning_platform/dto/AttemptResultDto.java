package com.majerpro.learning_platform.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttemptResultDto {
    private Long attemptId;
    private Long quizId;
    private Long userId;
    private Integer totalQuestions;
    private Integer correctCount;
    private Double score; // percent
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private List<AttemptAnswerResultDto> answers;
}
