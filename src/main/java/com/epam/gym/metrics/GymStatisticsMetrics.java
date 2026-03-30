package com.epam.gym.metrics;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GymStatisticsMetrics {

    public GymStatisticsMetrics(MeterRegistry registry,
                                TraineeRepository traineeRepository,
                                TrainerRepository trainerRepository,
                                TrainingRepository trainingRepository) {

        Gauge.builder("gym_trainees_total", traineeRepository, repo -> repo.count())
                .description("Total number of trainees")
                .tag("entity", "trainee")
                .register(registry);

        Gauge.builder("gym_trainers_total", trainerRepository, repo -> repo.count())
                .description("Total number of trainers")
                .tag("entity", "trainer")
                .register(registry);

        Gauge.builder("gym_trainings_total", trainingRepository, repo -> repo.count())
                .description("Total number of trainings")
                .tag("entity", "training")
                .register(registry);

        log.info("Gym statistics metrics initialized");
    }
}