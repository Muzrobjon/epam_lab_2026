package com.epam.gym.storage;

import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final TrainingTypeRepository trainingTypeRepository;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("Initializing training types...");

        List<String> trainingTypeNames = List.of(
                "Fitness",
                "Yoga",
                "Cardio",
                "Strength",
                "Pilates",
                "CrossFit",
                "Zumba"
        );

        for (String typeName : trainingTypeNames) {
            if (trainingTypeRepository.findByTrainingTypeName(typeName).isEmpty()) {
                TrainingType type = TrainingType.builder()
                        .trainingTypeName(typeName)
                        .build();
                trainingTypeRepository.save(type);
                log.info("Created training type: {}", typeName);
            }
        }

        log.info("Training types initialization completed");
    }
}
