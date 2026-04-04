package com.gym.metrics;

import com.gym.repository.TrainerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainerMetrics {

    private final Counter trainerRegistrationCounter;

    @Autowired
    public TrainerMetrics(MeterRegistry meterRegistry, TrainerRepository trainerRepository) {

        this.trainerRegistrationCounter = Counter.builder("gym.trainer.registrations.total")
                .description("Total number of trainer registrations")
                .register(meterRegistry);

        Gauge.builder("gym.trainer.current.total", trainerRepository, TrainerRepository::count)
                .description("Current number of trainers in the system")
                .register(meterRegistry);
    }

    public void incrementRegistrationCounter() {
        trainerRegistrationCounter.increment();
    }
}
