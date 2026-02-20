package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.model.revision.RevisionEvent;
import com.majerpro.learning_platform.model.revision.RevisionEventType;
import com.majerpro.learning_platform.model.revision.RevisionPlan;
import com.majerpro.learning_platform.repository.revision.RevisionEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SpacedRepetitionEngineService {

    private final RevisionEventRepository revisionEventRepository;

    // Tunables (MVP)
    private final double R_TARGET = 0.85;
    private final double A = 0.60; // stability growth factor
    private final double B = 0.15; // difficulty penalty on stability
    private final double C = 0.08; // difficulty update factor

    public SpacedRepetitionEngineService(RevisionEventRepository revisionEventRepository) {
        this.revisionEventRepository = revisionEventRepository;
    }

    public double computeRecallProbability(RevisionPlan plan, LocalDateTime now) {
        if (plan.getLastReviewedAt() == null) return 0.0;
        double s = Math.max(0.25, plan.getStabilityDays());
        double tDays = Math.max(0.0, Duration.between(plan.getLastReviewedAt(), now).toMinutes() / (60.0 * 24.0));
        return Math.exp(-tDays / s);
    }

    public LocalDateTime computeNextReviewAt(RevisionPlan plan, double targetRetention) {
        double s = Math.max(0.25, plan.getStabilityDays());
        double tNextDays = -s * Math.log(Math.max(0.01, Math.min(0.99, targetRetention)));
        LocalDateTime base = (plan.getLastReviewedAt() != null) ? plan.getLastReviewedAt() : LocalDateTime.now();
        return base.plusMinutes((long) (tNextDays * 24.0 * 60.0));
    }

    @Transactional
    public void applyOutcomeAndReschedule(RevisionPlan plan, double recallScore, String source, String reason) {
        double oldS = plan.getStabilityDays();
        double oldD = plan.getDifficulty();
        LocalDateTime oldNext = plan.getNextReviewAt();

        // Update stability & difficulty (MVP adaptive rule)
        double sNew = oldS * (1.0 + A * (recallScore - 0.5) - B * oldD);
        sNew = clamp(sNew, 0.25, 365.0);

        double dNew = oldD + C * (0.5 - recallScore);
        dNew = clamp(dNew, 0.3, 0.9);

        plan.setStabilityDays(sNew);
        plan.setDifficulty(dNew);
        plan.setLastRecallScore(recallScore);

        // Stats
        plan.setReviewCount((plan.getReviewCount() == null ? 0 : plan.getReviewCount()) + 1);
        if (recallScore < 0.5) {
            plan.setLapseCount((plan.getLapseCount() == null ? 0 : plan.getLapseCount()) + 1);
        }

        // lastReviewedAt updates when outcome is applied
        plan.setLastReviewedAt(LocalDateTime.now());
        plan.setNextReviewAt(computeNextReviewAt(plan, R_TARGET));

        RevisionEvent ev = new RevisionEvent();
        ev.setPlan(plan);
        ev.setEventType(RevisionEventType.COMPLETED);
        ev.setSource(source);
        ev.setReason(reason);

        ev.setRecallScore(recallScore);
        ev.setOldStabilityDays(oldS);
        ev.setNewStabilityDays(plan.getStabilityDays());
        ev.setOldDifficulty(oldD);
        ev.setNewDifficulty(plan.getDifficulty());
        ev.setOldNextReviewAt(oldNext);
        ev.setNewNextReviewAt(plan.getNextReviewAt());

        ev.setComputedRecallProbability(computeRecallProbability(plan, LocalDateTime.now()));
        revisionEventRepository.save(ev);
    }


    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
