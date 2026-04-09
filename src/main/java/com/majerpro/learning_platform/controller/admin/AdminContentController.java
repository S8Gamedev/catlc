package com.majerpro.learning_platform.controller.admin;

import com.majerpro.learning_platform.service.content.ContentIngestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/content")
public class AdminContentController {

    private final ContentIngestionService contentIngestionService;

    public AdminContentController(ContentIngestionService contentIngestionService) {
        this.contentIngestionService = contentIngestionService;
    }

    @GetMapping("/ingest")
    public String ingestPage() {
        return "admin/content-ingest";
    }

    @PostMapping("/ingest-by-skill-id")
    public String ingestBySkillId(@RequestParam Long skillId, Model model) {
        Long runId = contentIngestionService.ingestSkillById(skillId);
        model.addAttribute("message", "Ingestion started/completed for skillId=" + skillId + ", runId=" + runId);
        return "admin/content-ingest";
    }

    @PostMapping("/ingest-by-skill-name")
    public String ingestBySkillName(@RequestParam String skillName, Model model) {
        Long runId = contentIngestionService.ingestSkillByName(skillName);
        model.addAttribute("message", "Ingestion started/completed for skillName=" + skillName + ", runId=" + runId);
        return "admin/content-ingest";
    }
}