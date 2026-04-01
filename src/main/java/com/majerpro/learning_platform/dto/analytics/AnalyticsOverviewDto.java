package com.majerpro.learning_platform.dto.analytics;

import lombok.Data;

@Data
public class AnalyticsOverviewDto {
    private Long userId;

    private int skillsTracked;
    private double avgMastery;      // 0..100, avg of KnowledgeState.masteryScore
    private double retentionRate;   // 0..1, avg R across ACTIVE plans (0.0 if none)

    private int dueTasksNow;        // count of PENDING tasks due now
}
