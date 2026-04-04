package com.gym.health;

import com.gym.repository.TraineeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TraineeServiceHealthIndicator implements HealthIndicator {

    private TraineeRepository traineeRepository;

    @Autowired
    public void setTraineeRepository(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Override
    public Health health() {
        try {
            long count = traineeRepository.count();
            return Health.up()
                    .withDetail("status", "Trainee service is operational")
                    .withDetail("totalTrainees", count)
                    .build();
        } catch (Exception e) {
            log.error("Trainee service health check failed", e);
            return Health.down()
                    .withDetail("status", "Trainee service is not operational")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
