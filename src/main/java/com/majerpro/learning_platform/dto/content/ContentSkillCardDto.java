package com.majerpro.learning_platform.dto.content;

import lombok.Data;

@Data
public class ContentSkillCardDto {
    private Long skillId;
    private String skillName;
    private String description;
    private boolean contentAvailable;
}