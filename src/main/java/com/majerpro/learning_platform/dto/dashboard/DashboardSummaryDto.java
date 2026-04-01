package com.majerpro.learning_platform.dto.dashboard;

import lombok.Data;

import java.util.List;

@Data
public class DashboardSummaryDto {
    private int pendingTasks;
    private int overdueTasks;
    private int completedTasks;
    private int skillsCount;

    private List<DashboardSkillSummaryDto> skillSummaries;
    private List<DashboardSkillSummaryDto> urgentSkills;
}