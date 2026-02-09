package com.majerpro.learning_platform.repository.revision;

import com.majerpro.learning_platform.model.revision.RevisionEvent;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionEventRepository extends JpaRepository<RevisionEvent, Long> {
    List<RevisionEvent> findTop20ByPlanOrderByCreatedAtDesc(RevisionPlan plan);
}
