package com.majerpro.learning_platform.dto.chat;

import lombok.Data;

@Data
public class ChatSelectedSkillDto {
    private Long skillId;
    private String skillName;
    private String description;
}