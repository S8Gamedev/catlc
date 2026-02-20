package com.majerpro.learning_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillAnalyticsDto {
    private Long skillId;
    private String skillName;
    private Double currentMastery; // 0-100
    private String confidenceLevel; // LOW, MEDIUM, HIGH
    private Integer totalReviews;
    private Double avgRecallScore; // 0.0-1.0
    private Integer quizAttempts;
    private Integer codeAttempts;
    private LocalDateTime lastPracticed;
    private LocalDateTime nextReviewDue;
}
