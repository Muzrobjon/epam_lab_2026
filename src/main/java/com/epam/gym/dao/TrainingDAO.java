package com.epam.gym.dao;

import com.epam.gym.model.Training;
import com.epam.gym.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TrainingDAO {
    private static final String NAMESPACE = "trainings";
    private final Storage storage;

    public Training save(Training training) {
        if (training.getTrainingId() == null) {
            training.setTrainingId(storage.generateId(NAMESPACE));
        }
        storage.put(NAMESPACE, training.getTrainingId(), training);
        log.info("Saved training: {}", training.getTrainingName());
        return training;
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable((Training) storage.get(NAMESPACE, id));
    }

    public List<Training> findAll() {
        return storage.getAll(NAMESPACE).values().stream()
                .map(obj -> (Training) obj)
                .collect(Collectors.toList());
    }

    public List<Training> findByTraineeId(Long traineeId) {
        return findAll().stream()
                .filter(t -> t.getTraineeId().equals(traineeId))
                .collect(Collectors.toList());
    }

    public List<Training> findByTrainerId(Long trainerId) {
        return findAll().stream()
                .filter(t -> t.getTrainerId().equals(trainerId))
                .collect(Collectors.toList());
    }

    public boolean exists(Long id) {
        return storage.contains(NAMESPACE, id);
    }
}

