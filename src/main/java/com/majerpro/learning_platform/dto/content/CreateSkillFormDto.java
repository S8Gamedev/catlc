package com.majerpro.learning_platform.dto.content;

import lombok.Data;

@Data
public class CreateSkillFormDto {
//    private Long userId;
    private String name;
    private String description;
    private Long userId;
}