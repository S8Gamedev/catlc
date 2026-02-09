package com.majerpro.learning_platform.model.revision;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "revision_plans",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevisionPlanStatus status = RevisionPlanStatus.ACTIVE;

    // Memory model (per user-skill)
    @Column(name = "stability_days", nullable = false)
    private Double stabilityDays = 3.0; // start small, will adapt per user

    @Column(name = "difficulty", nullable = false)
    private Double difficulty = 0.5; // 0.3..0.9

    // Scheduling
    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    // Stats
    @Column(name = "last_recall_score")
    private Double lastRecallScore; // 0..1

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(name = "lapse_count", nullable = false)
    private Integer lapseCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
