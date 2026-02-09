package com.majerpro.learning_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType; // "PDF", "TEXT", "NOTE"

    @Column(name = "file_size")
    private Long fileSize; // in bytes

    @Column(length = 2000)
    private String description;

    @Column(name = "category")
    private String category; // e.g., "Java", "Spring Boot", "Database"

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
