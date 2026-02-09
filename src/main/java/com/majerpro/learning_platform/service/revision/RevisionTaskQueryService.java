package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RevisionTaskQueryService {

    private final RevisionTaskRepository revisionTaskRepository;
    private final UserRepository userRepository;

    public RevisionTaskQueryService(RevisionTaskRepository revisionTaskRepository, UserRepository userRepository) {
        this.revisionTaskRepository = revisionTaskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RevisionTaskDto> getTodayTasks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        List<RevisionTask> tasks = revisionTaskRepository.findByUserAndStatusAndDueAtBefore(
                user, RevisionTaskStatus.PENDING, now.plusSeconds(1)
        );

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
