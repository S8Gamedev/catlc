package com.majerpro.learning_platform.dto.revision;

import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.model.revision.RevisionTaskType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevisionTaskDto {
    private Long taskId;
    private Long userId;
    private Long skillId;
    private String skillName;
    private RevisionTaskType taskType;
    private RevisionTaskStatus status;
    private LocalDateTime dueAt;
    private Integer recommendedQuizSize;
    private String recommendedDifficulty;
}
