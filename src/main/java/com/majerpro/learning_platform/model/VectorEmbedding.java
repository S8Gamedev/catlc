package com.majerpro.learning_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vector_embeddings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chunk_id", nullable = false)
    private Chunk chunk;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String vectorData; // JSON array of floats stored as string

    @Column(name = "embedding_model")
    private String embeddingModel; // e.g., "all-MiniLM-L6-v2"

    @Column(name = "vector_dimension")
    private Integer vectorDimension; // e.g., 384
}
