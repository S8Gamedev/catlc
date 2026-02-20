package com.majerpro.learning_platform.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasteryChartDto {
    private Long skillId;
    private String skillName;
    private java.util.List<DataPoint> dataPoints;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double masteryScore;
        private String eventType; // QUIZ, CODE, REVIEW
    }
}
