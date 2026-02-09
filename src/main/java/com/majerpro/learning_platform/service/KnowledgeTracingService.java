package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.KnowledgeStateDto;
import com.majerpro.learning_platform.dto.UpdateMasteryDto;
import com.majerpro.learning_platform.model.KnowledgeState;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.repository.KnowledgeStateRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeTracingService {

    @Autowired
    private KnowledgeStateRepository knowledgeStateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    // Decay factor: how much knowledge decays per day
    private static final double DECAY_RATE = 0.02; // 2% per day

    /**
     * Update mastery score after a practice session
     */
    public KnowledgeStateDto updateMastery(UpdateMasteryDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Skill skill = skillRepository.findById(dto.getSkillId())
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        // Find or create knowledge state
        KnowledgeState knowledgeState = knowledgeStateRepository
                .findByUserAndSkill(user, skill)
                .orElse(new KnowledgeState());

        if (knowledgeState.getId() == null) {
            knowledgeState.setUser(user);
            knowledgeState.setSkill(skill);
        }

        // Apply time decay if last practiced date exists
        double currentMastery = knowledgeState.getMasteryScore();
        if (knowledgeState.getLastPracticed() != null) {
            currentMastery = applyTimeDecay(
                    knowledgeState.getMasteryScore(),
                    knowledgeState.getLastPracticed()
            );
        }

        // Calculate new mastery: old_mastery * decay + learning_gain
        double newMastery = Math.min(100.0, currentMastery + dto.getLearningGain());

        knowledgeState.setMasteryScore(newMastery);
        knowledgeState.setLastPracticed(LocalDateTime.now());
        knowledgeState.setPracticeCount(knowledgeState.getPracticeCount() + 1);
        knowledgeState.setConfidenceLevel(calculateConfidenceLevel(newMastery));

        KnowledgeState saved = knowledgeStateRepository.save(knowledgeState);
        return convertToDto(saved);
    }

    /**
     * Apply time-decay formula
     */
    private double applyTimeDecay(double masteryScore, LocalDateTime lastPracticed) {
        long daysSinceLastPractice = ChronoUnit.DAYS.between(lastPracticed, LocalDateTime.now());

        // Formula: mastery * (1 - decay_rate)^days
        double decayedMastery = masteryScore * Math.pow(1 - DECAY_RATE, daysSinceLastPractice);

        return Math.max(0.0, decayedMastery); // Never go below 0
    }

    /**
     * Calculate confidence level based on mastery score
     */
    private String calculateConfidenceLevel(double masteryScore) {
        if (masteryScore >= 70.0) {
            return "HIGH";
        } else if (masteryScore >= 40.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Get all knowledge states for a user
     */
    public List<KnowledgeStateDto> getUserKnowledgeStates(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<KnowledgeState> states = knowledgeStateRepository.findByUser(user);

        return states.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get weak skills (mastery < 50%)
     */
    public List<KnowledgeStateDto> getWeakSkills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<KnowledgeState> weakSkills = knowledgeStateRepository.findWeakSkills(user, 50.0);

        return weakSkills.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private KnowledgeStateDto convertToDto(KnowledgeState state) {
        KnowledgeStateDto dto = new KnowledgeStateDto();
        dto.setId(state.getId());
        dto.setUserId(state.getUser().getId());
        dto.setSkillId(state.getSkill().getId());
        dto.setSkillName(state.getSkill().getName());
        dto.setMasteryScore(state.getMasteryScore());
        dto.setLastPracticed(state.getLastPracticed());
        dto.setPracticeCount(state.getPracticeCount());
        dto.setConfidenceLevel(state.getConfidenceLevel());
        dto.setUpdatedAt(state.getUpdatedAt());
        return dto;
    }
}
