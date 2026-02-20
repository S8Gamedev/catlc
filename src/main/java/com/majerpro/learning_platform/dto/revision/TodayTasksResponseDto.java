package com.majerpro.learning_platform.dto.revision;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TodayTasksResponseDto {
    private List<RevisionTaskDto> tasks;
    private int totalCount;
}
