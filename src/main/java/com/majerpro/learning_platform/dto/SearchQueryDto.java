package com.majerpro.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SearchQueryDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Query is required")
    private String query;

    private Integer topK = 3; // Number of results to return

    private String category; // Optional: filter by category
}
