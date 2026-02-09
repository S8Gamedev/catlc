package com.majerpro.learning_platform.model.revision;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "revision_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevisionTask {

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
    @Column(name = "task_type", nullable = false)
    private RevisionTaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RevisionTaskStatus status = RevisionTaskStatus.PENDING;

    @Column(name = "due_at", nullable = false)
    private LocalDateTime dueAt;

    // Optional: recommendations for Stage 6 quiz generation
    @Column(name = "recommended_quiz_size")
    private Integer recommendedQuizSize;

    @Column(name = "recommended_difficulty")
    private String recommendedDifficulty;

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
