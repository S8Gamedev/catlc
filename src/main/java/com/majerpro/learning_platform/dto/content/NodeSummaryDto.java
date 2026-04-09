package com.majerpro.learning_platform.dto.content;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeSummaryDto {
    private String nodeTitle;
    private String summary;
    private List<String> keyPoints = new ArrayList<>();
    private String example;
}