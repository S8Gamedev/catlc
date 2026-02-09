package com.majerpro.learning_platform.dto.revision;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevisionEventDto {
    private String eventType;
    private String source;
    private String reason;

    private Double computedRecallProbability;
    private Double recallScore;

    private Double oldStabilityDays;
    private Double newStabilityDays;

    private Double oldDifficulty;
    private Double newDifficulty;

    private LocalDateTime oldNextReviewAt;
    private LocalDateTime newNextReviewAt;

    private LocalDateTime createdAt;
}
