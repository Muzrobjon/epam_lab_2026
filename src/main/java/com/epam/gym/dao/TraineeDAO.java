package com.epam.gym.dao;

import com.epam.gym.model.Trainee;
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
public class TraineeDAO {
    private static final String NAMESPACE = "trainees";
    private final Storage storage;

    public Trainee save(Trainee trainee) {
        if (trainee.getUserId() == null) {
            trainee.setUserId(storage.generateId(NAMESPACE));
        }
        storage.put(NAMESPACE, trainee.getUserId(), trainee);
        log.info("Saved trainee: {}", trainee.getUserName());
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable((Trainee) storage.get(NAMESPACE, id));
    }

    public Optional<Trainee> findByUsername(String username) {
        return findAll().stream()
                .filter(t -> t.getUserName().equals(username))
                .findFirst();
    }

    public List<Trainee> findAll() {
        return storage.getAll(NAMESPACE).values().stream()
                .map(obj -> (Trainee) obj)
                .collect(Collectors.toList());
    }

    public void delete(Long id) {
        storage.remove(NAMESPACE, id);
        log.info("Deleted trainee with id: {}", id);
    }

    public boolean exists(Long id) {
        return storage.contains(NAMESPACE, id);
    }
}

