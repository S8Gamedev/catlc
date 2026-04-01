package com.majerpro.learning_platform.dto.dashboard;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DashboardSkillSummaryDto {
    private Long skillId;
    private String skillName;

    private int totalTasks;
    private int pendingTasks;
    private int completedTasks;
    private int overdueTasks;

    private double completionPercent;
    private LocalDateTime nextDueAt;
}