package com.majerpro.learning_platform.dto.content;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SkillNotesFormDto {
    private Long userId;
    private Long skillId;
    private String skillName;
    private String title;
    private String summary;
    private String content;
    private MultipartFile textFile;
}