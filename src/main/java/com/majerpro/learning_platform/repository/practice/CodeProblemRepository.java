package com.majerpro.learning_platform.repository.practice;

import com.majerpro.learning_platform.model.practice.CodeProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeProblemRepository extends JpaRepository<CodeProblem, Long> {
    List<CodeProblem> findByCategory(String category);
    // NEW
    long countBySkill(com.majerpro.learning_platform.model.Skill skill);

}
