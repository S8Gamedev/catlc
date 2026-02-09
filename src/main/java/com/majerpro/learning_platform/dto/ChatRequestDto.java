package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long conversationId; // Optional: null for new conversation

    @NotBlank(message = "Message is required")
    private String message;

    private Boolean useRAG = true; // Whether to use document retrieval

    private String category; // Optional: filter documents by category
}
