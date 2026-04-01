package com.majerpro.learning_platform.service.analytics;

import com.majerpro.learning_platform.dto.analytics.*;
import com.majerpro.learning_platform.model.KnowledgeState;
import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.model.revision.RevisionEvent;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.model.revision.RevisionPlanStatus;
import com.majerpro.learning_platform.model.revision.RevisionTaskStatus;
import com.majerpro.learning_platform.repository.KnowledgeStateRepository;
import com.majerpro.learning_platform.repository.UserRepository;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import com.majerpro.learning_platform.repository.revision.RevisionPlanRepository;
import com.majerpro.learning_platform.repository.revision.RevisionTaskRepository;
import com.majerpro.learning_platform.service.revision.SpacedRepetitionEngineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AnalyticsService {

    private final UserRepository userRepository;
    private final KnowledgeStateRepository knowledgeStateRepository;

    private final RevisionEventRepository revisionEventRepository;
    private final RevisionTaskRepository revisionTaskRepository;
    private final RevisionPlanRepository revisionPlanRepository;

    private final SpacedRepetitionEngineService engine;

    public AnalyticsService(
            UserRepository userRepository,
            KnowledgeStateRepository knowledgeStateRepository,
            RevisionEventRepository revisionEventRepository,
            RevisionTaskRepository revisionTaskRepository,
            RevisionPlanRepository revisionPlanRepository,
            SpacedRepetitionEngineService engine
    ) {
        this.userRepository = userRepository;
        this.knowledgeStateRepository = knowledgeStateRepository;
        this.revisionEventRepository = revisionEventRepository;
        this.revisionTaskRepository = revisionTaskRepository;
        this.revisionPlanRepository = revisionPlanRepository;
        this.engine = engine;
    }

    // ---- From service.analytics.AnalyticsService ----

    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<KnowledgeState> states = knowledgeStateRepository.findByUserId(userId);

        int skillsTracked = states.size();
        double avgMastery = (skillsTracked == 0)
                ? 0.0
                : states.stream().mapToDouble(KnowledgeState::getMasteryScore).average().orElse(0.0);

        LocalDateTime now = LocalDateTime.now();

        List<RevisionPlan> activePlans =
                revisionPlanRepository.findByUserAndStatus(user, RevisionPlanStatus.ACTIVE);

        double retentionRate = (activePlans.isEmpty())
                ? 0.0
                : activePlans.stream()
                .mapToDouble(p -> engine.computeRecallProbability(p, now))
                .average()
                .orElse(0.0);

        int dueTasksNow = revisionTaskRepository
                .findByUserAndStatusAndDueAtBefore(user, RevisionTaskStatus.PENDING, now.plusSeconds(1))
                .size();

        AnalyticsOverviewDto dto = new AnalyticsOverviewDto();
        dto.setUserId(userId);
        dto.setSkillsTracked(skillsTracked);
        dto.setAvgMastery(avgMastery);
        dto.setRetentionRate(retentionRate);
        dto.setDueTasksNow(dueTasksNow);
        return dto;
    }

    // ---- From service.AnalyticsService ----

    @Transactional(readOnly = true)
    public UserOverviewDto getUserOverview(Long userId) {
        var knowledgeStates = knowledgeStateRepository.findByUserId(userId);

        int totalSkills = knowledgeStates.size();
        double avgMastery = knowledgeStates.stream()
                .mapToDouble(KnowledgeState::getMasteryScore)
                .average()
                .orElse(0.0);

        int weakSkills = (int) knowledgeStates.stream()
                .filter(ks -> ks.getMasteryScore() < 50.0)
                .count();

        User user = userRepository.findById(userId).orElseThrow();
        var plans = revisionPlanRepository.findByUser(user);

        int totalReviews = plans.stream()
                .mapToInt(plan -> revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan).size())
                .sum();

        var allTasks = revisionTaskRepository.findByUserId(userId);
        long completedTasks = allTasks.stream()
                .filter(task -> task.getStatus() == RevisionTaskStatus.DONE)
                .count();

        double retentionRate = allTasks.isEmpty()
                ? 0.0
                : (completedTasks * 100.0) / allTasks.size();

        int currentStreak = calculateStreak(userId);

        return new UserOverviewDto(
                userId,
                totalSkills,
                avgMastery,
                totalReviews,
                0,
                0,
                retentionRate,
                currentStreak,
                weakSkills
        );
    }

    @Transactional(readOnly = true)
    public List<SkillAnalyticsDto> getSkillAnalytics(Long userId) {
        var knowledgeStates = knowledgeStateRepository.findByUserId(userId);

        return knowledgeStates.stream().map(ks -> {
            Long skillId = ks.getSkill().getId();

            // Keeping YOUR current repository contract:
            // Optional<RevisionPlan> findByUserAndSkill(Long userId, Long skillId)
            var planOpt = revisionPlanRepository.findByUserAndSkill(userId, skillId);

            int reviewCount = 0;
            double avgRecall = 0.0;
            LocalDateTime nextReview = null;

            if (planOpt.isPresent()) {
                var plan = planOpt.get();
                var events = revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan);

                reviewCount = events.size();
                avgRecall = events.stream()
                        .filter(e -> e.getRecallScore() != null)
                        .mapToDouble(RevisionEvent::getRecallScore)
                        .average()
                        .orElse(0.0);

                nextReview = plan.getNextReviewAt();
            }

            return new SkillAnalyticsDto(
                    skillId,
                    ks.getSkill().getName(),
                    ks.getMasteryScore(),
                    ks.getConfidenceLevel().toUpperCase(),
                    reviewCount,
                    avgRecall,
                    0,
                    0,
                    ks.getLastPracticed(),
                    nextReview
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaderboardUserDto> getLeaderboard(int top) {
        var allUsers = userRepository.findAll();

        List<LeaderboardUserDto> leaderboard = allUsers.stream()
                .map(user -> {
                    var states = knowledgeStateRepository.findByUserId(user.getId());
                    if (states.isEmpty()) return null;

                    double totalMastery = states.stream()
                            .mapToDouble(KnowledgeState::getMasteryScore)
                            .sum();

                    var plans = revisionPlanRepository.findByUser(user);
                    int totalReviews = plans.stream()
                            .mapToInt(p -> revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(p).size())
                            .sum();

                    double avgRecall = plans.stream()
                            .flatMap(p -> revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(p).stream())
                            .filter(e -> e.getRecallScore() != null)
                            .mapToDouble(RevisionEvent::getRecallScore)
                            .average()
                            .orElse(0.0);

                    return new LeaderboardUserDto(
                            0,
                            user.getId(),
                            user.getEmail(),
                            totalMastery,
                            states.size(),
                            totalReviews,
                            avgRecall
                    );
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> Double.compare(b.getTotalMasteryScore(), a.getTotalMasteryScore()))
                .limit(top)
                .collect(Collectors.toList());

        IntStream.range(0, leaderboard.size()).forEach(i ->
                leaderboard.get(i).setRank(i + 1)
        );

        return leaderboard;
    }

    @Transactional(readOnly = true)
    public MasteryChartDto getSkillMasteryChart(Long userId, Long skillId) {
        var ks = knowledgeStateRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new RuntimeException("KnowledgeState not found"));

        var planOpt = revisionPlanRepository.findByUserAndSkill(userId, skillId);
        List<MasteryChartDto.DataPoint> points = new ArrayList<>();

        if (planOpt.isPresent()) {
            var plan = planOpt.get();
            var events = revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan);
            Collections.reverse(events);

            for (var event : events) {
                points.add(new MasteryChartDto.DataPoint(
                        event.getCreatedAt(),
                        ks.getMasteryScore(),
                        event.getEventType().name()
                ));
            }
        }

        points.add(new MasteryChartDto.DataPoint(
                LocalDateTime.now(),
                ks.getMasteryScore(),
                "CURRENT"
        ));

        return new MasteryChartDto(skillId, ks.getSkill().getName(), points);
    }

    private int calculateStreak(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        var plans = revisionPlanRepository.findByUser(user);

        Set<java.time.LocalDate> dates = new HashSet<>();
        for (var plan : plans) {
            for (var event : revisionEventRepository.findTop20ByPlanOrderByCreatedAtDesc(plan)) {
                dates.add(event.getCreatedAt().toLocalDate());
            }
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        int streak = 0;
        while (dates.contains(today.minusDays(streak))) streak++;
        return streak;
    }
}
