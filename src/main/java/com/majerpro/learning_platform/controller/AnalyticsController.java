package com.majerpro.learning_platform.controller;

import com.majerpro.learning_platform.dto.analytics.*;
import com.majerpro.learning_platform.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get comprehensive overview for a user
     * GET /api/analytics/user/{userId}/overview
     */
    @GetMapping("/user/{userId}/overview")
    public ResponseEntity<UserOverviewDto> getUserOverview(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getUserOverview(userId));
    }

    /**
     * Get analytics breakdown by skill for a user
     * GET /api/analytics/user/{userId}/skills
     */
    @GetMapping("/user/{userId}/skills")
    public ResponseEntity<List<SkillAnalyticsDto>> getSkillAnalytics(@PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.getSkillAnalytics(userId));
    }

    /**
     * Get mastery progression chart for a specific skill
     * GET /api/analytics/user/{userId}/skill/{skillId}/chart
     */
    @GetMapping("/user/{userId}/skill/{skillId}/chart")
    public ResponseEntity<MasteryChartDto> getSkillMasteryChart(
            @PathVariable Long userId,
            @PathVariable Long skillId) {
        return ResponseEntity.ok(analyticsService.getSkillMasteryChart(userId, skillId));
    }
}

@RestController
@RequestMapping("/api/leaderboard")
class LeaderboardController {

    private final AnalyticsService analyticsService;

    LeaderboardController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get top N users by total mastery
     * GET /api/leaderboard/users?top=10
     */
    @GetMapping("/users")
    public ResponseEntity<List<LeaderboardUserDto>> getLeaderboard(
            @RequestParam(defaultValue = "10") int top) {
        return ResponseEntity.ok(analyticsService.getLeaderboard(top));
    }
}
