package com.majerpro.learning_platform.model.content;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "node_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_node_id", nullable = false, unique = true)
    private SkillNode skillNode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_document_id")
    private SourceDocument sourceDocument;

    @Column(length = 2000)
    private String summary;

    @Lob
    @Column(name = "key_points", columnDefinition = "TEXT")
    private String keyPoints;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String example;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}