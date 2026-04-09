package com.majerpro.learning_platform.repository.content;

import com.majerpro.learning_platform.model.content.IngestionRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionRunRepository extends JpaRepository<IngestionRun, Long> {
}