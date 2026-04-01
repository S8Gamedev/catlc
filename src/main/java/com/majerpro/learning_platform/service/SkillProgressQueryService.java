package com.majerpro.learning_platform.service;

import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.dto.SkillProgressDto;
import com.majerpro.learning_platform.model.Skill;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.SkillRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SkillProgressQueryService {

    private final SkillRepository skillRepository;
    private final RevisionTaskRepository revisionTaskRepository;

    public SkillProgressQueryService(SkillRepository skillRepository,
                                     RevisionTaskRepository revisionTaskRepository) {
        this.skillRepository = skillRepository;
        this.revisionTaskRepository = revisionTaskRepository;
    }

    @Transactional(readOnly = true)
    public SkillProgressDto getSkillProgress(Long skillId, Long userId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));

        List<RevisionTask> tasks = revisionTaskRepository.findByUserIdAndSkillIdOrderByDueAtAsc(userId, skillId);

        LocalDateTime now = LocalDateTime.now();

        int totalTasks = tasks.size();
        int pendingTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                .count();

        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.DONE)
                .count();

        int overdueTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                .filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(now))
                .count();

        LocalDateTime nextDueAt = tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                .map(RevisionTask::getDueAt)
                .filter(d -> d != null)
                .sorted()
                .findFirst()
                .orElse(null);

        double completionPercent = totalTasks == 0
                ? 0.0
                : ((double) completedTasks / totalTasks) * 100.0;

        int recommendedQuizSize = tasks.stream()
                .map(RevisionTask::getRecommendedQuizSize)
                .filter(v -> v != null)
                .findFirst()
                .orElse(5);

        String recommendedDifficulty = tasks.stream()
                .map(RevisionTask::getRecommendedDifficulty)
                .filter(v -> v != null)
                .map(Object::toString)
                .findFirst()
                .orElse("MEDIUM");

        SkillProgressDto dto = new SkillProgressDto();
        dto.setSkillId(skill.getId());
        dto.setSkillName(skill.getName());

        try {
            dto.setDescription(skill.getDescription());
        } catch (Exception e) {
            dto.setDescription("No description available yet.");
        }

        dto.setTotalTasks(totalTasks);
        dto.setPendingTasks(pendingTasks);
        dto.setCompletedTasks(completedTasks);
        dto.setOverdueTasks(overdueTasks);
        dto.setCompletionPercent(completionPercent);
        dto.setNextDueAt(nextDueAt);
        dto.setRecommendedQuizSize(recommendedQuizSize);
        dto.setRecommendedDifficulty(recommendedDifficulty);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<RevisionTaskDto> getSkillTasks(Long skillId, Long userId) {
        List<RevisionTask> tasks = revisionTaskRepository.findByUserIdAndSkillIdOrderByDueAtAsc(userId, skillId);

        return tasks.stream().map(this::toDto).toList();
    }

    private RevisionTaskDto toDto(RevisionTask t) {
        RevisionTaskDto dto = new RevisionTaskDto();
        dto.setTaskId(t.getId());
        dto.setUserId(t.getUser().getId());
        dto.setSkillId(t.getSkill().getId());
        dto.setSkillName(t.getSkill().getName());
        dto.setTaskType(t.getTaskType());
        dto.setStatus(t.getStatus());
        dto.setDueAt(t.getDueAt());
        dto.setRecommendedQuizSize(t.getRecommendedQuizSize());
        dto.setRecommendedDifficulty(t.getRecommendedDifficulty());
        return dto;
    }
}