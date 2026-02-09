package com.majerpro.learning_platform.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentResponseDto {

    private Long id;
    private Long userId;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String description;
    private String category;
    private String sourceUrl;
    private LocalDateTime uploadDate;
    private Boolean isProcessed;
    private Integer chunkCount;
}
