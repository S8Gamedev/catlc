package com.majerpro.learning_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOverviewDto {
    private Long userId;
    private Integer totalSkillsTracked;
    private Double avgMasteryScore; // 0-100
    private Integer totalReviews;
    private Integer totalQuizAttempts;
    private Integer totalCodeSubmissions;
    private Double retentionRate; // % of tasks completed on time
    private Integer currentStreak; // consecutive days with activity
    private Integer weakSkillsCount; // mastery < 50
}
