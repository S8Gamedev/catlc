package com.majerpro.learning_platform.model.practice;

import com.majerpro.learning_platform.model.Skill;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional link to Skill so code practice updates mastery for that concept
    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String prompt;

    // Example: "JAVA", "PYTHON" etc (your UI can filter)
    private String category;

    // Example: "BEGINNER/INTERMEDIATE/ADVANCED"
    private String difficultyLevel;

    // For simple auto-grading: compare stdout to expectedOutput (trimmed)
    @Column(columnDefinition = "TEXT")
    private String sampleInput;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
