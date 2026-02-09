package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.VectorEmbedding;
import com.majerpro.learning_platform.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface VectorEmbeddingRepository extends JpaRepository<VectorEmbedding, Long> {

    Optional<VectorEmbedding> findByChunk(Chunk chunk);

    List<VectorEmbedding> findByEmbeddingModel(String embeddingModel);
}
