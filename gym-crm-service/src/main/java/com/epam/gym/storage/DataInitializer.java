package com.epam.gym.storage;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final TrainingTypeRepository trainingTypeRepository;

    @PostConstruct
    @Transactional
    public void init() {
        log.info("Initializing training types...");

        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            if (trainingTypeRepository.findByTrainingTypeName(typeName).isEmpty()) {
                TrainingType type = new TrainingType(null, typeName);
                trainingTypeRepository.save(type);
                log.info("Created training type: {}", typeName);
            }
        }

        log.info("Training types initialization completed");
    }
}