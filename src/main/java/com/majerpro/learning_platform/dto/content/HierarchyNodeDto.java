package com.majerpro.learning_platform.dto.content;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HierarchyNodeDto {
    private String title;
    private String objective;
    private List<HierarchyNodeDto> children = new ArrayList<>();
}