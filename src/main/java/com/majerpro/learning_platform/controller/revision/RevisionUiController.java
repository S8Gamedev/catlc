package com.majerpro.learning_platform.controller.revision;

import com.majerpro.learning_platform.dto.revision.RevisionTaskDto;
import com.majerpro.learning_platform.service.revision.RevisionTaskQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/revisions")
public class RevisionUiController {

    private final RevisionTaskQueryService queryService;

    public RevisionUiController(RevisionTaskQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping({"", "/"})
    public String todayTasksUi(@RequestParam(defaultValue = "1") Long userId, Model model) {
        List<RevisionTaskDto> tasks = queryService.getTodayTasks(userId);

        LocalDateTime now = LocalDateTime.now();
        long urgentCount = tasks.stream()
                .filter(t -> t.getDueAt() != null && !t.getDueAt().isAfter(now.plusHours(1)))
                .count();
        long overdueCount = tasks.stream()
                .filter(t -> t.getDueAt() != null && t.getDueAt().isBefore(now))
                .count();

        model.addAttribute("tasks", tasks);
        model.addAttribute("userId", userId);
        model.addAttribute("taskCount", tasks.size());
        model.addAttribute("urgentCount", urgentCount);
        model.addAttribute("overdueCount", overdueCount);

        return "revisions/today";
    }

    @GetMapping("/{taskId}")
    public String revisionTaskDetail(@PathVariable Long taskId, Model model) {
        RevisionTaskDto task = queryService.getTaskById(taskId);

        String actionUrl;
        if ("CODE".equals(task.getTaskType().name())) {
            actionUrl = "/practice?skillId=" + task.getSkillId();
        } else if ("QUIZ".equals(task.getTaskType().name())) {
            actionUrl = "/quizzes?skillId=" + task.getSkillId();
        } else {
            actionUrl = "/content?skillId=" + task.getSkillId();
        }

        model.addAttribute("task", task);
        model.addAttribute("skillId", task.getSkillId());
        model.addAttribute("skillName", task.getSkillName());
        model.addAttribute("taskType", task.getTaskType().name());
        model.addAttribute("dueAt", task.getDueAt());
        model.addAttribute("actionUrl", actionUrl);
        model.addAttribute("userId", 1L);
        return "revisions/detail";
    }
}