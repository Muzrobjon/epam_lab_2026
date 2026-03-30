package com.epam.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainingMetrics {

    private final Counter trainingsCreated;
    private final Counter trainingsCompleted;
    private final Timer trainingCreationTimer;

    public TrainingMetrics(MeterRegistry registry) {
        this.trainingsCreated = Counter.builder("gym_trainings_created_total")
                .description("Total number of trainings created")
                .tag("type", "training")
                .register(registry);

        this.trainingsCompleted = Counter.builder("gym_trainings_completed_total")
                .description("Total number of trainings completed")
                .tag("type", "training")
                .register(registry);

        this.trainingCreationTimer = Timer.builder("gym_training_creation_seconds")
                .description("Time taken to create a training")
                .tag("type", "training")
                .register(registry);

        log.info("Training metrics initialized");
    }

    public void incrementCreated() {
        trainingsCreated.increment();
        log.debug("Training created counter incremented");
    }

    public void incrementCompleted() {
        trainingsCompleted.increment();
        log.debug("Training completed counter incremented");
    }

    public Timer.Sample startTimer() {
        return Timer.start();
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(trainingCreationTimer);
    }
}