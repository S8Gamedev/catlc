package com.majerpro.learning_platform.model.practice;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "code_test_cases")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodeProblem problem;

    @Column(columnDefinition = "TEXT")
    private String stdin;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    private Boolean isSample = false;  // show in UI or hide
    private Integer weight = 1;        // scoring weight
}
