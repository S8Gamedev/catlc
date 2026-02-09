package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentUploadDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    private String fileName;

    private String fileType; // "PDF", "TEXT", "NOTE"

    private String description;

    private String category;

    private String sourceUrl;
}
