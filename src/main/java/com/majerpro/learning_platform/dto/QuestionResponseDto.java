package com.majerpro.learning_platform.dto;

import com.majerpro.learning_platform.model.QuestionDifficulty;
import com.majerpro.learning_platform.model.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionResponseDto {
    private Long id;
    private Long skillId;
    private String skillName;
    private QuestionType type;
    private QuestionDifficulty difficulty;
    private String prompt;
    private List<String> options;
    private Integer correctOptionIndex;
    private String explanation;
}
