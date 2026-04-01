package com.gym.metrics;

import com.gym.repository.TraineeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TraineeMetrics {

    private final Counter traineeRegistrationCounter;

    @Autowired
    public TraineeMetrics(MeterRegistry meterRegistry, TraineeRepository traineeRepository) {

        this.traineeRegistrationCounter = Counter.builder("gym.trainee.registrations.total")
                .description("Total number of trainee registrations")
                .register(meterRegistry);

        Gauge.builder("gym.trainee.current.total", traineeRepository, TraineeRepository::count)
                .description("Current number of trainees in the system")
                .register(meterRegistry);
    }

    public void incrementRegistrationCounter() {
        traineeRegistrationCounter.increment();
    }
}
