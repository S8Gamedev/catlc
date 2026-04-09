package com.majerpro.learning_platform.model;

import com.majerpro.learning_platform.model.Skill;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_skill_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"})
)
@Data
public class UserSkillProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(nullable = false)
    private Double mastery = 0.0;

    @Column(name = "learned_percent", nullable = false)
    private Double learnedPercent = 0.0;

    @Column(name = "confidence_level", nullable = false)
    private Double confidenceLevel = 0.0;

    @Column(name = "times_practiced", nullable = false)
    private Integer timesPracticed = 0;

    @Column(name = "times_quizzed", nullable = false)
    private Integer timesQuizzed = 0;

    @Column(name = "times_learned_new", nullable = false)
    private Integer timesLearnedNew = 0;

    @Column(name = "retention_score", nullable = false)
    private Double retentionScore = 100.0;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "last_learned_at")
    private LocalDateTime lastLearnedAt;

    @Column(name = "last_practiced_at")
    private LocalDateTime lastPracticedAt;

    @Column(name = "last_quizzed_at")
    private LocalDateTime lastQuizzedAt;

    @Column(name = "last_decay_check_at")
    private LocalDateTime lastDecayCheckAt;

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