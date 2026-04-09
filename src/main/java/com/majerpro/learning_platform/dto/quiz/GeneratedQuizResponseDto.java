package com.majerpro.learning_platform.dto.quiz;

import lombok.Data;
import java.util.List;

@Data
public class GeneratedQuizResponseDto {
    private List<QuizQuestionDto> questions;
}