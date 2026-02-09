package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Chunk;
import com.majerpro.learning_platform.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    List<Chunk> findByDocument(Document document);

    List<Chunk> findByDocumentOrderByChunkIndexAsc(Document document);
}
