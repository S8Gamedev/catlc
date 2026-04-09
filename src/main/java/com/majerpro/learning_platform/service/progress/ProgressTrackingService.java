package com.majerpro.learning_platform.service.progress;

public interface ProgressTrackingService {
    void ensureSkillTracked(Long userId, Long skillId);
    void recordLearning(Long userId, Long skillId, double noveltyGain);
    void recordPractice(Long userId, Long skillId, double scorePercent);
    void recordQuiz(Long userId, Long skillId, double scorePercent);
    void applyDecay(Long userId, Long skillId);
}