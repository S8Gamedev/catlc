package com.majerpro.learning_platform.dto.content;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContentSkillListItemDto {
    private Long skillId;
    private String skillName;
    private String description;
    private int pendingTasks;
    private LocalDateTime nextDueAt;
}