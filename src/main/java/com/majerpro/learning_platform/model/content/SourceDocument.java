package com.majerpro.learning_platform.model.content;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "source_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourceDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_run_id", nullable = false)
    private IngestionRun ingestionRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_node_id")
    private SkillNode skillNode;

    @Column(nullable = false, length = 2000)
    private String url;

    @Column(length = 255)
    private String domain;

    @Column(length = 1000)
    private String title;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Lob
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Lob
    @Column(name = "cleaned_text", columnDefinition = "TEXT")
    private String cleanedText;

    @Column(name = "ranking_score")
    private Double rankingScore;
}