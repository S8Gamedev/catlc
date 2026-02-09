package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.QuestionCreateDto;
import com.majerpro.learning_platform.dto.QuestionResponseDto;
import com.majerpro.learning_platform.model.Question;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.repository.QuestionRepository;
import com.majerpro.learning_platform.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionService {

    @Autowired private QuestionRepository questionRepository;
    @Autowired private SkillRepository skillRepository;

    public QuestionResponseDto createQuestion(QuestionCreateDto dto) {
        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        if (dto.getOptions() == null || dto.getOptions().size() != 4) {
            throw new RuntimeException("Options must contain exactly 4 items");
        }

        Question q = new Question();
        q.setSkill(skill);
        q.setType(dto.getType());
        q.setDifficulty(dto.getDifficulty());
        q.setPrompt(dto.getPrompt());
        q.setOptionA(dto.getOptions().get(0));
        q.setOptionB(dto.getOptions().get(1));
        q.setOptionC(dto.getOptions().get(2));
        q.setOptionD(dto.getOptions().get(3));
        q.setCorrectOptionIndex(dto.getCorrectOptionIndex());
        q.setExplanation(dto.getExplanation());

        Question saved = questionRepository.save(q);
        return toResponseDto(saved);
    }

    public List<QuestionResponseDto> getQuestionsBySkill(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        return questionRepository.findBySkill(skill).stream()
                .map(this::toResponseDto)
                .toList();
    }

    private QuestionResponseDto toResponseDto(Question q) {
        QuestionResponseDto dto = new QuestionResponseDto();
        dto.setId(q.getId());
        dto.setSkillId(q.getSkill().getId());
        dto.setSkillName(q.getSkill().getName());
        dto.setType(q.getType());
        dto.setDifficulty(q.getDifficulty());
        dto.setPrompt(q.getPrompt());
        dto.setOptions(List.of(q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()));
        dto.setCorrectOptionIndex(q.getCorrectOptionIndex());
        dto.setExplanation(q.getExplanation());
        return dto;
    }
}
