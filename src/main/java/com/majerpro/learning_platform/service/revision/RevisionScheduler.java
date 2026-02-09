package com.majerpro.learning_platform.service.revision;

import com.majerpro.learning_platform.model.User;
import com.majerpro.learning_platform.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RevisionScheduler {

    private final UserRepository userRepository;
    private final RevisionPlannerService plannerService;

    public RevisionScheduler(UserRepository userRepository, RevisionPlannerService plannerService) {
        this.userRepository = userRepository;
        this.plannerService = plannerService;
    }

    // Runs daily at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyRebuild() {
        for (User u : userRepository.findAll()) {
            plannerService.rebuildForUser(u.getId());
        }
    }
}
