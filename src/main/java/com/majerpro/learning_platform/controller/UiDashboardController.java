package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.dashboard.DashboardSummaryDto;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.service.analytics.AnalyticsService;
import com.majerpro.learning_platform.service.dashboard.DashboardQueryService;
import com.majerpro.learning_platform.repository.KnowledgeStateRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class UiDashboardController {

    private final UserRepository userRepository;
    private final AnalyticsService analyticsService;
    private final KnowledgeStateRepository knowledgeStateRepository;
    private final DashboardQueryService dashboardQueryService;

    public UiDashboardController(UserRepository userRepository,
                                 AnalyticsService analyticsService,
                                 KnowledgeStateRepository knowledgeStateRepository,
                                 DashboardQueryService dashboardQueryService) {
        this.userRepository = userRepository;
        this.analyticsService = analyticsService;
        this.knowledgeStateRepository = knowledgeStateRepository;
        this.dashboardQueryService = dashboardQueryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        var email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        var overview = analyticsService.getUserOverview(user.getId());
        var skills = analyticsService.getSkillAnalytics(user.getId());
        DashboardSummaryDto summary = dashboardQueryService.getDashboardSummary(user.getId());

        model.addAttribute("userEmail", email);
        model.addAttribute("overview", overview);
        model.addAttribute("skills", skills);
        model.addAttribute("summary", summary);
        model.addAttribute("userId", user.getId());

        return "dashboard/overview";
    }

    @GetMapping("/skills")
    public String skills(Model model, Principal principal) {
        var email = principal.getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

        var states = knowledgeStateRepository.findByUserId(user.getId());
        model.addAttribute("userEmail", email);
        model.addAttribute("states", states);
        return "skills";
    }
}