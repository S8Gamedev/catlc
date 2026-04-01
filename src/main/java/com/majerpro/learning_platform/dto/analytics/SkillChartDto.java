// NEW: src/main/java/com/majerpro/learningplatform/dto/analytics/SkillChartDto.java
package com.majerpro.learningplatform.dto.analytics;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SkillChartDto {
    private Long skillId;
    private String skillName;
    private List<Point> points; // mastery trend
    private String metric; // e.g. "MASTERY"

    @Data
    public static class Point {
        private LocalDate date;
        private Double masteryScore; // 0..100 (whatever you use)
    }
}
