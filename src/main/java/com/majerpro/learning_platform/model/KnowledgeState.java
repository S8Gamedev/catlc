package com.majerpro.learning_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_states",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "skill_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "mastery_score", nullable = false)
    private Double masteryScore = 0.0; // 0.0 to 100.0

    @Column(name = "last_practiced")
    private LocalDateTime lastPracticed;

    @Column(name = "practice_count")
    private Integer practiceCount = 0;

    @Column(name = "confidence_level")
    private String confidenceLevel; // "LOW", "MEDIUM", "HIGH"

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
