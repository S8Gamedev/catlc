package com.majerpro.learning_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardUserDto {
    private Integer rank;
    private Long userId;
    private String userName;
    private Double totalMasteryScore; // sum across all skills
    private Integer skillsCount;
    private Integer totalReviews;
    private Double avgRecallScore; // 0.0-1.0
}
