package com.majerpro.learning_platform.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository {

    // User overview stats
    @Query("""
        SELECT 
            COUNT(DISTINCT ks.skill.id) as totalSkills,
            AVG(ks.masteryScore) as avgMastery,
            COUNT(DISTINCT re.id) as totalReviews,
            COUNT(DISTINCT qa.id) as totalQuizzes,
            COUNT(DISTINCT cs.id) as totalCodeSubmissions
        FROM KnowledgeState ks
        LEFT JOIN RevisionEvent re ON re.plan.user.id = :userId
        LEFT JOIN QuizAttempt qa ON qa.user.id = :userId
        LEFT JOIN CodeSubmission cs ON cs.user.id = :userId
        WHERE ks.user.id = :userId
        """)
    Object[] getUserOverviewStats(@Param("userId") Long userId);

    // Retention rate: completed tasks / total tasks
    @Query("""
        SELECT 
            COUNT(CASE WHEN rt.status = 'DONE' THEN 1 END) * 100.0 / COUNT(rt.id)
        FROM RevisionTask rt
        WHERE rt.user.id = :userId
        """)
    Double getRetentionRate(@Param("userId") Long userId);

    // Weak skills count
    @Query("""
        SELECT COUNT(ks.id)
        FROM KnowledgeState ks
        WHERE ks.user.id = :userId AND ks.masteryScore < :threshold
        """)
    Integer getWeakSkillsCount(@Param("userId") Long userId, @Param("threshold") Double threshold);
}
