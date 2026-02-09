package com.majerpro.learning_platform.dto;

import lombok.Data;

@Data
public class SearchResultDto {

    private Long chunkId;
    private Long documentId;
    private String documentTitle;
    private String chunkContent;
    private Double similarityScore; // 0.0 to 1.0
    private Integer chunkIndex;
}
