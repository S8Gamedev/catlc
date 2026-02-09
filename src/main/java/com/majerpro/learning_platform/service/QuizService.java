package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.*;
import com.majerpro.learning_platform.model.*;
import com.majerpro.learning_platform.repository.*;
import com.majerpro.learning_platform.service.revision.RevisionOutcomeService; // NEW
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizService {

    @Autowired private QuizRepository quizRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private QuizAttemptRepository quizAttemptRepository;
    @Autowired private AttemptAnswerRepository attemptAnswerRepository;

    @Autowired private UserRepository userRepository;
    @Autowired private KnowledgeStateRepository knowledgeStateRepository;

    // This exists in your project (from Stage 2)
    @Autowired private KnowledgeTracingService knowledgeTracingService;

    @Autowired private RevisionOutcomeService revisionOutcomeService; // NEW

    @Transactional
    public QuizResponseDto generateQuiz(GenerateQuizRequestDto req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<KnowledgeState> weakStates = knowledgeStateRepository.findWeakSkills(user, req.getWeakThreshold());
        if (weakStates == null || weakStates.isEmpty()) {
            throw new RuntimeException("No weak skills found for this user (try higher threshold or add more skills).");
        }

        List<Skill> weakSkills = weakStates.stream().map(KnowledgeState::getSkill).toList();

        List<Question> pool = questionRepository.findBySkillIn(weakSkills);
        if (pool == null || pool.isEmpty()) {
            throw new RuntimeException("No questions found for user's weak skills. Add questions first.");
        }

        Collections.shuffle(pool);
        int n = Math.min(req.getNumberOfQuestions(), pool.size());
        List<Question> selected = pool.subList(0, n);

        Quiz quiz = new Quiz();
        quiz.setUser(user);
        quiz.setTitle("Adaptive Quiz (" + n + " questions)");
        quiz.setStatus(QuizStatus.ACTIVE);
        quiz.setQuestions(new ArrayList<>(selected));

        Quiz saved = quizRepository.save(quiz);
        return toQuizResponseDto(saved);
    }

    @Transactional
    public Long startAttempt(Long quizId, StartAttemptRequestDto req) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!Objects.equals(quiz.getUser().getId(), user.getId())) {
            throw new RuntimeException("This quiz does not belong to the given user.");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setScore(null);
        attempt.setSubmittedAt(null);

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        return saved.getId();
    }

    @Transactional
    public AttemptResultDto submitAttempt(Long attemptId, SubmitAttemptDto req) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (attempt.getSubmittedAt() != null) {
            throw new RuntimeException("Attempt already submitted.");
        }

        Quiz quiz = attempt.getQuiz();
        Map<Long, Question> quizQuestions = new HashMap<>();
        for (Question q : quiz.getQuestions()) quizQuestions.put(q.getId(), q);

        int correctCount = 0;
        List<AttemptAnswerResultDto> answerResults = new ArrayList<>();

        for (SubmitAnswerDto a : req.getAnswers()) {
            Question q = quizQuestions.get(a.getQuestionId());
            if (q == null) {
                throw new RuntimeException("Question " + a.getQuestionId() + " is not part of this quiz.");
            }

            if (attemptAnswerRepository.existsByAttemptAndQuestionId(attempt, q.getId())) {
                throw new RuntimeException("Duplicate answer submitted for questionId=" + q.getId());
            }

            boolean isCorrect = Objects.equals(a.getSelectedOptionIndex(), q.getCorrectOptionIndex());
            if (isCorrect) correctCount++;

            AttemptAnswer attemptAnswer = new AttemptAnswer();
            attemptAnswer.setAttempt(attempt);
            attemptAnswer.setQuestion(q);
            attemptAnswer.setSelectedOptionIndex(a.getSelectedOptionIndex());
            attemptAnswer.setIsCorrect(isCorrect);
            attemptAnswerRepository.save(attemptAnswer);

            // Update mastery using your existing Stage-2 service
            UpdateMasteryDto update = new UpdateMasteryDto();
            update.setUserId(attempt.getUser().getId());
            update.setSkillId(q.getSkill().getId());
            update.setLearningGain(isCorrect ? 10.0 : 2.0);
            knowledgeTracingService.updateMastery(update);

            // NEW: Feed Stage 8 forgetting-curve model
            revisionOutcomeService.applyOutcome(
                    attempt.getUser().getId(),
                    q.getSkill().getId(),
                    isCorrect ? 1.0 : 0.0,
                    "QUIZ",
                    isCorrect ? "QUIZ_CORRECT" : "QUIZ_WRONG"
            );

            AttemptAnswerResultDto ar = new AttemptAnswerResultDto();
            ar.setQuestionId(q.getId());
            ar.setSkillId(q.getSkill().getId());
            ar.setSkillName(q.getSkill().getName());
            ar.setSelectedOptionIndex(a.getSelectedOptionIndex());
            ar.setCorrect(isCorrect);
            answerResults.add(ar);
        }

        int total = attempt.getTotalQuestions() == null ? quiz.getQuestions().size() : attempt.getTotalQuestions();
        double scorePercent = total == 0 ? 0.0 : (correctCount * 100.0) / total;

        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setScore(scorePercent);
        attempt.setTotalQuestions(total);
        quizAttemptRepository.save(attempt);

        AttemptResultDto result = new AttemptResultDto();
        result.setAttemptId(attempt.getId());
        result.setQuizId(quiz.getId());
        result.setUserId(attempt.getUser().getId());
        result.setTotalQuestions(total);
        result.setCorrectCount(correctCount);
        result.setScore(scorePercent);
        result.setStartedAt(attempt.getStartedAt());
        result.setSubmittedAt(attempt.getSubmittedAt());
        result.setAnswers(answerResults);
        return result;
    }

    public AttemptResultDto getAttemptResult(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        List<AttemptAnswer> answers = attemptAnswerRepository.findByAttempt(attempt);

        int correctCount = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();

        List<AttemptAnswerResultDto> answerResults = answers.stream().map(a -> {
            AttemptAnswerResultDto ar = new AttemptAnswerResultDto();
            ar.setQuestionId(a.getQuestion().getId());
            ar.setSkillId(a.getQuestion().getSkill().getId());
            ar.setSkillName(a.getQuestion().getSkill().getName());
            ar.setSelectedOptionIndex(a.getSelectedOptionIndex());
            ar.setCorrect(a.getIsCorrect());
            return ar;
        }).toList();

        AttemptResultDto result = new AttemptResultDto();
        result.setAttemptId(attempt.getId());
        result.setQuizId(attempt.getQuiz().getId());
        result.setUserId(attempt.getUser().getId());
        result.setTotalQuestions(attempt.getTotalQuestions());
        result.setCorrectCount(correctCount);
        result.setScore(attempt.getScore());
        result.setStartedAt(attempt.getStartedAt());
        result.setSubmittedAt(attempt.getSubmittedAt());
        result.setAnswers(answerResults);
        return result;
    }

    private QuizResponseDto toQuizResponseDto(Quiz quiz) {
        QuizResponseDto dto = new QuizResponseDto();
        dto.setQuizId(quiz.getId());
        dto.setUserId(quiz.getUser().getId());
        dto.setTitle(quiz.getTitle());
        dto.setStatus(quiz.getStatus());
        dto.setCreatedAt(quiz.getCreatedAt());

        List<QuizQuestionDto> qs = quiz.getQuestions().stream().map(q -> {
            QuizQuestionDto qq = new QuizQuestionDto();
            qq.setQuestionId(q.getId());
            qq.setSkillId(q.getSkill().getId());
            qq.setSkillName(q.getSkill().getName());
            qq.setType(q.getType());
            qq.setDifficulty(q.getDifficulty());
            qq.setPrompt(q.getPrompt());
            qq.setOptions(List.of(q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()));
            return qq;
        }).toList();

        dto.setQuestions(qs);
        return dto;
    }
}
