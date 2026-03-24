package com.epam.gym.health;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("gymServices")
@RequiredArgsConstructor
public class TraineeServiceHealthIndicator implements HealthIndicator {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public Health health() {
        try {
            long traineeCount = traineeRepository.count();
            long trainerCount = trainerRepository.count();

            log.debug("Gym services health check - Trainees: {}, Trainers: {}",
                    traineeCount, trainerCount);

            return Health.up()
                    .withDetail("trainees", traineeCount)
                    .withDetail("trainers", trainerCount)
                    .withDetail("status", "All services operational")
                    .build();

        } catch (Exception e) {
            log.error("Gym services health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}