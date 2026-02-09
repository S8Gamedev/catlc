package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.AttemptAnswer;
import com.majerpro.learning_platform.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {
    List<AttemptAnswer> findByAttempt(QuizAttempt attempt);
    boolean existsByAttemptAndQuestionId(QuizAttempt attempt, Long questionId);
}
