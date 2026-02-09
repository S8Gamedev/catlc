package com.majerpro.learning_platform.dto.revision;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RevisionExplainDto {
    private Long userId;
    private Long skillId;
    private String skillName;

    private Double stabilityDays;
    private Double difficulty;
    private LocalDateTime lastReviewedAt;
    private LocalDateTime nextReviewAt;

    private Double currentRecallProbability;

    private List<RevisionEventDto> recentEvents;
}
