package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Document;
import com.majerpro.learning_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByUser(User user);

    List<Document> findByUserAndCategory(User user, String category);

    List<Document> findByUserAndIsProcessed(User user, Boolean isProcessed);
}
