package com.majerpro.learning_platform.service.dashboard;

import com.majerpro.learning_platform.dto.dashboard.DashboardSkillSummaryDto;
import com.majerpro.learning_platform.dto.dashboard.DashboardSummaryDto;
import com.majerpro.learning_platform.model.revision.RevisionTask;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardQueryService {

    private final RevisionTaskRepository revisionTaskRepository;

    public DashboardQueryService(RevisionTaskRepository revisionTaskRepository) {
        this.revisionTaskRepository = revisionTaskRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        List<RevisionTask> tasks = revisionTaskRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        int pendingTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                .count();

        int completedTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.DONE)
                .count();

        int overdueTasks = (int) tasks.stream()
                .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                .filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(now))
                .count();

        Map<Long, List<RevisionTask>> groupedBySkill = tasks.stream()
                .filter(t -> t.getSkill() != null && t.getSkill().getId() != null)
                .collect(Collectors.groupingBy(t -> t.getSkill().getId()));

        List<DashboardSkillSummaryDto> skillSummaries = new ArrayList<>();

        for (Map.Entry<Long, List<RevisionTask>> entry : groupedBySkill.entrySet()) {
            List<RevisionTask> skillTasks = entry.getValue();
            RevisionTask first = skillTasks.get(0);

            int total = skillTasks.size();
            int pending = (int) skillTasks.stream()
                    .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                    .count();

            int completed = (int) skillTasks.stream()
                    .filter(t -> t.getStatus() == RevisionTaskStatus.DONE)
                    .count();

            int overdue = (int) skillTasks.stream()
                    .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                    .filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(now))
                    .count();

            LocalDateTime nextDueAt = skillTasks.stream()
                    .filter(t -> t.getStatus() == RevisionTaskStatus.PENDING)
                    .map(RevisionTask::getDueAt)
                    .filter(Objects::nonNull)
                    .sorted()
                    .findFirst()
                    .orElse(null);

            double completionPercent = total == 0 ? 0.0 : ((double) completed / total) * 100.0;

            DashboardSkillSummaryDto dto = new DashboardSkillSummaryDto();
            dto.setSkillId(first.getSkill().getId());
            dto.setSkillName(first.getSkill().getName());
            dto.setTotalTasks(total);
            dto.setPendingTasks(pending);
            dto.setCompletedTasks(completed);
            dto.setOverdueTasks(overdue);
            dto.setCompletionPercent(completionPercent);
            dto.setNextDueAt(nextDueAt);

            skillSummaries.add(dto);
        }

        List<DashboardSkillSummaryDto> urgentSkills = skillSummaries.stream()
                .sorted(Comparator
                        .comparingInt(DashboardSkillSummaryDto::getOverdueTasks).reversed()
                        .thenComparingInt(DashboardSkillSummaryDto::getPendingTasks).reversed())
                .limit(5)
                .toList();

        DashboardSummaryDto summary = new DashboardSummaryDto();
        summary.setPendingTasks(pendingTasks);
        summary.setCompletedTasks(completedTasks);
        summary.setOverdueTasks(overdueTasks);
        summary.setSkillsCount(skillSummaries.size());
        summary.setSkillSummaries(skillSummaries);
        summary.setUrgentSkills(urgentSkills);

        return summary;
    }
}