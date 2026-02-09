package com.majerpro.learning_platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeStateDto {

    private Long id;
    private Long userId;
    private Long skillId;
    private String skillName;
    private Double masteryScore;
    private LocalDateTime lastPracticed;
    private Integer practiceCount;
    private String confidenceLevel;
    private LocalDateTime updatedAt;
}
