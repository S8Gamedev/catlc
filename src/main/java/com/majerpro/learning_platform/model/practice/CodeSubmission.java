package com.majerpro.learning_platform.model.practice;

import com.majerpro.learning_platform.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "code_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodeProblem problem;

    @Column(nullable = false)
    private Integer languageId; // Judge0 language_id

    @Column(columnDefinition = "TEXT", nullable = false)
    private String sourceCode;

    @Column(columnDefinition = "TEXT")
    private String stdin;

    @Column(columnDefinition = "TEXT")
    private String judgeTokensCsv; // token1,token2,...

    @Column(columnDefinition = "TEXT")
    private String testcaseReport; // JSON string summary (keep simple for now)

    @Column(nullable = false)
    private Integer passedCount = 0;

    @Column(nullable = false)
    private Integer totalCount = 0;

    @Column(nullable = false)
    private Boolean isCorrect = false;

    @Column(nullable = false)
    private Double score = 0.0;

    @Column(nullable = false)
    private String status = "QUEUED";


    @Column(columnDefinition = "TEXT")
    private String stdout;

    @Column(columnDefinition = "TEXT")
    private String stderr;

    @Column(columnDefinition = "TEXT")
    private String compileOutput;

    private LocalDateTime createdAt;
    private LocalDateTime evaluatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
