package com.majerpro.learning_platform.model.revision;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "revision_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private RevisionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private RevisionEventType eventType;

    @Column(name = "source")
    private String source; // QUIZ / CODE / MANUAL / SCHEDULER

    @Column(name = "reason")
    private String reason;

    @Column(name = "computed_recall_probability")
    private Double computedRecallProbability;

    @Column(name = "recall_score")
    private Double recallScore;

    @Column(name = "old_stability_days")
    private Double oldStabilityDays;

    @Column(name = "new_stability_days")
    private Double newStabilityDays;

    @Column(name = "old_difficulty")
    private Double oldDifficulty;

    @Column(name = "new_difficulty")
    private Double newDifficulty;

    @Column(name = "old_next_review_at")
    private LocalDateTime oldNextReviewAt;

    @Column(name = "new_next_review_at")
    private LocalDateTime newNextReviewAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
