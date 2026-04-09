package com.majerpro.learning_platform.service.quiz;

public interface QuizGenerationService {
    void generateAndSaveQuizForSkill(Long skillId, int questionCount);
}