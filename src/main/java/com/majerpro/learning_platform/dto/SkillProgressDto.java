package com.majerpro.learning_platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SkillProgressDto {
    private Long skillId;
    private String skillName;
    private String description;

    private int totalTasks;
    private int pendingTasks;
    private int completedTasks;
    private int overdueTasks;

    private double completionPercent;
    private LocalDateTime nextDueAt;

    private int recommendedQuizSize;
    private String recommendedDifficulty;
}