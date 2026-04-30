package com.epam.gym.actuator;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Endpoint(id = "gym")
@RequiredArgsConstructor
public class GymInfoEndpoint {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;

    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "Gym CRM System");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().toString());

        Map<String, Long> statistics = new LinkedHashMap<>();
        statistics.put("trainees", traineeRepository.count());
        statistics.put("trainers", trainerRepository.count());
        statistics.put("trainings", trainingRepository.count());
        info.put("statistics", statistics);

        return info;
    }

    @ReadOperation
    public Map<String, Object> detail(@Selector String name) {
        Map<String, Object> result = new LinkedHashMap<>();

        switch (name) {
            case "trainees" -> {
                result.put("entity", "Trainee");
                result.put("count", traineeRepository.count());
            }
            case "trainers" -> {
                result.put("entity", "Trainer");
                result.put("count", trainerRepository.count());
            }
            case "trainings" -> {
                result.put("entity", "Training");
                result.put("count", trainingRepository.count());
            }
            default -> result.put("error", "Unknown: " + name);
        }

        return result;
    }
}