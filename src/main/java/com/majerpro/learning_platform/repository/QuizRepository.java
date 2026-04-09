package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Quiz;
import com.majerpro.learning_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUser(User user);

}
