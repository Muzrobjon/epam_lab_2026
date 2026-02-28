package com.epam.gym.dao;

import com.epam.gym.model.Trainer;
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
public class TrainerDAO {
    private static final String NAMESPACE = "trainers";
    private final Storage storage;

    public Trainer save(Trainer trainer) {
        if (trainer.getUserId() == null) {
            trainer.setUserId(storage.generateId(NAMESPACE));
        }
        storage.put(NAMESPACE, trainer.getUserId(), trainer);
        log.info("Saved trainer: {}", trainer.getUserName());
        return trainer;
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable((Trainer) storage.get(NAMESPACE, id));
    }

    public Optional<Trainer> findByUsername(String username) {
        return findAll().stream()
                .filter(t -> t.getUserName().equals(username))
                .findFirst();
    }

    public List<Trainer> findAll() {
        return storage.getAll(NAMESPACE).values().stream()
                .map(obj -> (Trainer) obj)
                .collect(Collectors.toList());
    }

    public boolean exists(Long id) {
        return storage.contains(NAMESPACE, id);
    }
}

