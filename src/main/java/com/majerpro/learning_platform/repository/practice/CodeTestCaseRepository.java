package com.majerpro.learning_platform.repository.practice;

import com.majerpro.learning_platform.model.practice.CodeProblem;
import com.majerpro.learning_platform.model.practice.CodeTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeTestCaseRepository extends JpaRepository<CodeTestCase, Long> {
    List<CodeTestCase> findByProblem(CodeProblem problem);
}
