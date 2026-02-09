package com.majerpro.learning_platform.repository;

import com.majerpro.learning_platform.model.Question;
import com.majerpro.learning_platform.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySkill(Skill skill);
    List<Question> findBySkillIn(List<Skill> skills);
}
