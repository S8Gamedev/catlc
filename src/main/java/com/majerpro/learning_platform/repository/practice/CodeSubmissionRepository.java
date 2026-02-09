package com.majerpro.learning_platform.repository.practice;

import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.practice.CodeProblem; // NEW
import com.majerpro.learning_platform.model.practice.CodeSubmission;
import org.springframework.data.domain.Page;      // NEW
import org.springframework.data.domain.Pageable;  // NEW
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {
    List<CodeSubmission> findByUserOrderByCreatedAtDesc(User user);

    // NEW: for “single question history (latest N)”
    Page<CodeSubmission> findByUserAndProblem(User user, CodeProblem problem, Pageable pageable); // NEW
}
