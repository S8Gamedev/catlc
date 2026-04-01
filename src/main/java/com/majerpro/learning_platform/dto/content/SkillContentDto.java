package com.majerpro.learning_platform.dto.content;

import lombok.Data;

@Data
public class SkillContentDto {
    private Long skillId;
    private String skillName;
    private String title;
    private String summary;
    private String content;
    private String recommendedDifficulty;
    private boolean contentAvailable;
}