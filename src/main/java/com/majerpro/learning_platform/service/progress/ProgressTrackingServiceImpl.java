package com.majerpro.learning_platform.service.progress;

import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.UserSkillProgress;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.UserSkillProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ProgressTrackingServiceImpl implements ProgressTrackingService {

    private final UserSkillProgressRepository progressRepository;
    private final SkillRepository skillRepository;

    public ProgressTrackingServiceImpl(UserSkillProgressRepository progressRepository,
                                       SkillRepository skillRepository) {
        this.progressRepository = progressRepository;
        this.skillRepository = skillRepository;
    }

    @Override
    public void ensureSkillTracked(Long userId, Long skillId) {
        progressRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseGet(() -> {
                    Skill skill = skillRepository.findById(skillId)
                            .orElseThrow(() -> new RuntimeException("Skill not found"));

                    UserSkillProgress progress = new UserSkillProgress();
                    progress.setUserId(userId);
                    progress.setSkill(skill);
                    progress.setMastery(0.0);
                    progress.setLearnedPercent(0.0);
                    progress.setConfidenceLevel(0.0);
                    progress.setTimesPracticed(0);
                    progress.setTimesQuizzed(0);
                    progress.setTimesLearnedNew(0);
                    progress.setRetentionScore(100.0);
                    progress.setStatus("ACTIVE");

                    return progressRepository.save(progress);
                });
    }

    @Override
    public void recordLearning(Long userId, Long skillId, double noveltyGain) {
        UserSkillProgress progress = getOrCreate(userId, skillId);

        progress.setTimesLearnedNew(progress.getTimesLearnedNew() + 1);
        progress.setLearnedPercent(Math.min(100.0, progress.getLearnedPercent() + noveltyGain));
        progress.setMastery(Math.min(100.0, progress.getMastery() + noveltyGain * 0.8));
        progress.setConfidenceLevel(Math.min(100.0, progress.getConfidenceLevel() + noveltyGain * 0.5));
        progress.setLastLearnedAt(LocalDateTime.now());

        progressRepository.save(progress);
    }

    @Override
    public void recordPractice(Long userId, Long skillId, double scorePercent) {
        UserSkillProgress progress = getOrCreate(userId, skillId);

        progress.setTimesPracticed(progress.getTimesPracticed() + 1);
        progress.setLastPracticedAt(LocalDateTime.now());

        if (scorePercent >= 80) {
            progress.setMastery(Math.min(100.0, progress.getMastery() + 2.0));
            progress.setConfidenceLevel(Math.min(100.0, progress.getConfidenceLevel() + 1.5));
        } else if (scorePercent < 50) {
            progress.setConfidenceLevel(Math.max(0.0, progress.getConfidenceLevel() - 2.0));
        }

        progressRepository.save(progress);
    }

    @Override
    public void recordQuiz(Long userId, Long skillId, double scorePercent) {
        UserSkillProgress progress = getOrCreate(userId, skillId);

        progress.setTimesQuizzed(progress.getTimesQuizzed() + 1);
        progress.setLastQuizzedAt(LocalDateTime.now());

        if (scorePercent >= 85) {
            progress.setMastery(Math.min(100.0, progress.getMastery() + 3.0));
            progress.setConfidenceLevel(Math.min(100.0, progress.getConfidenceLevel() + 2.0));
        } else if (scorePercent >= 60) {
            progress.setMastery(Math.min(100.0, progress.getMastery() + 1.0));
        } else {
            progress.setConfidenceLevel(Math.max(0.0, progress.getConfidenceLevel() - 2.0));
        }

        progressRepository.save(progress);
    }

    @Override
    public void applyDecay(Long userId, Long skillId) {
        UserSkillProgress progress = getOrCreate(userId, skillId);

        LocalDateTime reference = progress.getLastQuizzedAt() != null
                ? progress.getLastQuizzedAt()
                : progress.getLastPracticedAt() != null
                ? progress.getLastPracticedAt()
                : progress.getLastLearnedAt();

        if (reference == null) {
            return;
        }

        long days = Duration.between(reference, LocalDateTime.now()).toDays();
        double decay = Math.min(40.0, days * 0.4);

        progress.setRetentionScore(Math.max(0.0, 100.0 - decay));

        if (progress.getRetentionScore() < 50) {
            progress.setMastery(Math.max(0.0, progress.getMastery() - 5.0));
        }

        progress.setLastDecayCheckAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    private UserSkillProgress getOrCreate(Long userId, Long skillId) {
        return progressRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseGet(() -> {
                    ensureSkillTracked(userId, skillId);
                    return progressRepository.findByUserIdAndSkillId(userId, skillId)
                            .orElseThrow(() -> new RuntimeException("Progress creation failed"));
                });
    }
}