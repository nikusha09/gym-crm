package com.gym.health;

import com.gym.repository.TrainingTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public void setTrainingTypeRepository(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        try {
            long count = trainingTypeRepository.count();
            if (count > 0) {
                return Health.up()
                        .withDetail("trainingTypes", count + " types seeded")
                        .withDetail("database", "reachable")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "reachable")
                        .withDetail("trainingTypes", "no training types seeded")
                        .build();
            }
        } catch (Exception e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
