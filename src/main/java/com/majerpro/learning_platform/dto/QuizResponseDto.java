package com.majerpro.learning_platform.dto;

import com.majerpro.learning_platform.model.QuizStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizResponseDto {
    private Long quizId;
    private Long userId;
    private String title;
    private QuizStatus status;
    private LocalDateTime createdAt;
    private List<QuizQuestionDto> questions;
}
