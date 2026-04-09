package com.majerpro.learning_platform.dto.learn;

import lombok.Data;

@Data
public class LearnSkillListItemDto {
    private Long skillId;
    private String skillName;
    private String description;
    private boolean alreadyAdded;
}