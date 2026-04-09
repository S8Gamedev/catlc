package com.majerpro.learning_platform.controller.quiz;

import com.majerpro.learning_platform.dto.quiz.QuizQuestionDto;
import com.majerpro.learning_platform.dto.quiz.QuizResultDto;
import com.majerpro.learning_platform.dto.quiz.QuizSelectedSkillDto;
import com.majerpro.learning_platform.dto.quiz.QuizSkillListItemDto;
import com.majerpro.learning_platform.model.Question;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.UserSkillProgress;
import com.majerpro.learning_platform.repository.QuestionRepository;
import com.majerpro.learning_platform.repository.UserSkillProgressRepository;
import com.majerpro.learning_platform.service.progress.ProgressTrackingService;
import com.majerpro.learning_platform.service.quiz.QuizGenerationServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/quizzes")
@SessionAttributes({"quizQuestions", "quizResult"})
public class QuizUiController {

//    private final QuestionRepository questionRepository;
    private final QuizGenerationServiceImpl quizGenerationService;
    private final UserSkillProgressRepository progressRepository;
    private final QuestionRepository questionRepository;
    private final ProgressTrackingService progressTrackingService;

    public QuizUiController(UserSkillProgressRepository progressRepository,
                            QuestionRepository questionRepository,
                            ProgressTrackingService progressTrackingService,
                            QuizGenerationServiceImpl quizGenerationService) {
        this.progressRepository = progressRepository;
        this.questionRepository = questionRepository;
        this.progressTrackingService = progressTrackingService;
        this.quizGenerationService = quizGenerationService;
    }

    @ModelAttribute("quizQuestions")
    public List<QuizQuestionDto> quizQuestions() {
        return new java.util.ArrayList<>();
    }

    @ModelAttribute("quizResult")
    public QuizResultDto quizResult() {
        return null;
    }

    @GetMapping({"", "/"})
    public String quizOverview(@RequestParam(defaultValue = "1") Long userId,
                               @RequestParam(required = false) Long skillId,
                               @ModelAttribute("quizQuestions") List<QuizQuestionDto> quizQuestions,
                               @ModelAttribute("quizResult") QuizResultDto quizResult,
                               Model model) {

        List<UserSkillProgress> trackedSkills = progressRepository.findByUserIdOrderByMasteryDesc(userId);

        List<QuizSkillListItemDto> skills = trackedSkills.stream()
                .map(progress -> {
                    QuizSkillListItemDto dto = new QuizSkillListItemDto();
                    dto.setSkillId(progress.getSkill().getId());
                    dto.setSkillName(progress.getSkill().getName());
                    dto.setDescription(progress.getSkill().getDescription());
                    dto.setMastery(progress.getMastery());
                    return dto;
                })
                .sorted(Comparator.comparing(QuizSkillListItemDto::getSkillName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        model.addAttribute("skills", skills);
        model.addAttribute("userId", userId);

        if (skillId == null) {
            model.addAttribute("selectedSkill", null);
            model.addAttribute("questions", List.of());
            model.addAttribute("quizResult", null);
            return "quizzes/overview";
        }

        UserSkillProgress progress = progressRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new RuntimeException("Tracked skill not found for user"));

        QuizSelectedSkillDto selectedSkill = new QuizSelectedSkillDto();
        selectedSkill.setSkillId(progress.getSkill().getId());
        selectedSkill.setSkillName(progress.getSkill().getName());
        selectedSkill.setDescription(progress.getSkill().getDescription());
        selectedSkill.setMastery(progress.getMastery());
        selectedSkill.setRetentionScore(progress.getRetentionScore());

        List<Question> dbQuestions = questionRepository.findBySkill(progress.getSkill());

        List<QuizQuestionDto> mappedQuestions = dbQuestions.stream()
                .map(this::toDto)
                .toList();

        quizQuestions.clear();
        quizQuestions.addAll(mappedQuestions);

        model.addAttribute("selectedSkill", selectedSkill);
        model.addAttribute("questions", quizQuestions);
        model.addAttribute("quizResult", quizResult);

        return "quizzes/overview";
    }

    @PostMapping("/submit")
    public String submitQuiz(@RequestParam Long skillId,
                             @RequestParam(defaultValue = "1") Long userId,
                             @RequestParam(name = "questionIds", required = false) List<Long> questionIds,
                             @RequestParam(name = "answers", required = false) List<String> answers,
                             @ModelAttribute("quizQuestions") List<QuizQuestionDto> quizQuestions,
                             Model model) {

        if (questionIds == null || answers == null || quizQuestions.isEmpty()) {
            QuizResultDto result = new QuizResultDto();
            result.setScorePercent(0.0);
            result.setSummary("No answers were submitted.");
            model.addAttribute("quizResult", result);
            return "redirect:/quizzes?skillId=" + skillId + "&userId=" + userId;
        }

        int total = quizQuestions.size();
        int correct = 0;

        for (int i = 0; i < quizQuestions.size() && i < answers.size(); i++) {
            QuizQuestionDto question = quizQuestions.get(i);
            String submitted = answers.get(i);

            if (submitted == null || submitted.isBlank()) {
                continue;
            }

            int submittedIndex;
            try {
                submittedIndex = switch (submitted.toUpperCase()) {
                    case "A" -> 0;
                    case "B" -> 1;
                    case "C" -> 2;
                    case "D" -> 3;
                    default -> -1;
                };
            } catch (Exception e) {
                submittedIndex = -1;
            }

            if (submittedIndex == question.getCorrectOptionIndex()) {
                correct++;
            }
        }

        double scorePercent = total == 0 ? 0.0 : ((double) correct / total) * 100.0;

        progressTrackingService.recordQuiz(userId, skillId, scorePercent);

        QuizResultDto result = new QuizResultDto();
        result.setScorePercent(scorePercent);
        result.setSummary(buildQuizSummary(correct, total, scorePercent));

        model.addAttribute("quizResult", result);

        return "redirect:/quizzes?skillId=" + skillId + "&userId=" + userId;
    }

    private QuizQuestionDto toDto(Question q) {
        QuizQuestionDto dto = new QuizQuestionDto();
        dto.setId(q.getId());
        dto.setQuestionText(q.getPrompt());
        dto.setOptionA(q.getOptionA());
        dto.setOptionB(q.getOptionB());
        dto.setOptionC(q.getOptionC());
        dto.setOptionD(q.getOptionD());
        dto.setCorrectOptionIndex(q.getCorrectOptionIndex());
        dto.setExplanation(q.getExplanation());
        return dto;
    }

    private String buildQuizSummary(int correct, int total, double scorePercent) {
        if (scorePercent >= 85) {
            return "Strong result. You answered " + correct + " out of " + total + " correctly and showed good command of the topic.";
        }
        if (scorePercent >= 60) {
            return "Decent result. You answered " + correct + " out of " + total + " correctly, but there are still some gaps to review.";
        }
        return "You answered " + correct + " out of " + total + " correctly. It would help to revisit the content and try again.";
    }
}