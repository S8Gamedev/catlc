package com.majerpro.learning_platform.dto.quiz;

import lombok.Data;

@Data
public class QuizQuestionDto {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer correctOptionIndex;
    private String explanation;
}