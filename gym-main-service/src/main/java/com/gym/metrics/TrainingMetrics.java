package com.gym.metrics;

import com.gym.repository.TrainingRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrainingMetrics {

    private final Counter trainingAddedCounter;

    @Autowired
    public TrainingMetrics(MeterRegistry meterRegistry, TrainingRepository trainingRepository) {

        this.trainingAddedCounter = Counter.builder("gym.training.added.total")
                .description("Total number of trainings added")
                .register(meterRegistry);

        Gauge.builder("gym.training.current.total", trainingRepository, TrainingRepository::count)
                .description("Current number of trainings in the system")
                .register(meterRegistry);
    }

    public void incrementTrainingAddedCounter() {
        trainingAddedCounter.increment();
    }
}
