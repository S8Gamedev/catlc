package com.majerpro.learning_platform.dto.content;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HierarchyTreeDto {
    private String skill;
    private List<HierarchyNodeDto> nodes = new ArrayList<>();
}