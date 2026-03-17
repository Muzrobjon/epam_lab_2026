package com.epam.gym.service;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TraineeService extends AbstractUserService<Trainee> {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public TraineeService(
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            UserRepository userRepository,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            Validator validator
    ) {
        super(userRepository, usernameGenerator, passwordGenerator, validator);
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional
    public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
        log.info("Creating trainee profile for {} {}", firstName, lastName);

        User savedUser = createAndSaveUser(firstName, lastName);

        Trainee trainee = Trainee.builder()
                .id(savedUser.getId())
                .user(savedUser)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();

        validateEntity(trainee);

        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee: {} with username: {} and password: {}",
                saved.getId(), savedUser.getUsername(), savedUser.getPassword());

        return saved;
    }

    @Transactional(readOnly = true)
    public Trainee selectByUsername(String username) {
        log.info("Selecting trainee by username: {}", username);
        return traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    @Transactional
    public Trainee updateProfile(String username, String password, Trainee updatedTrainee) {
        log.info("Updating trainee profile: {}", username);

        authenticateUser(username, password);

        Trainee existing = selectByUsername(username);

        updateUserBasicInfo(existing.getUser(), updatedTrainee.getUser());

        if (updatedTrainee.getDateOfBirth() != null) {
            existing.setDateOfBirth(updatedTrainee.getDateOfBirth());
        }
        if (updatedTrainee.getAddress() != null) {
            existing.setAddress(updatedTrainee.getAddress());
        }

        validateEntity(existing);

        Trainee saved = traineeRepository.save(existing);
        log.info("Updated trainee profile: {}", username);

        return saved;
    }

    @Transactional
    public void deleteByUsername(String username, String password) {
        log.info("Deleting trainee profile: {}", username);

        authenticateUser(username, password);

        Trainee trainee = selectByUsername(username);

        traineeRepository.delete(trainee);

        log.info("Deleted trainee profile: {}", username);
    }

    @Transactional
    public void updateTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);

        authenticateUser(traineeUsername, password);

        Trainee trainee = selectByUsername(traineeUsername);

        List<Trainer> trainers = fetchTrainersByUsernames(trainerUsernames);

        trainee.getTrainers().clear();
        trainee.setTrainers(trainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers list for trainee: {} with {} trainers",
                traineeUsername, trainers.size());
    }


    private List<Trainer> fetchTrainersByUsernames(List<String> trainerUsernames) {
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            return List.of();
        }

        List<Trainer> foundTrainers = trainerRepository.findByUser_UsernameIn(trainerUsernames);

        if (foundTrainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = foundTrainers.stream()
                    .map(t -> t.getUser().getUsername())
                    .collect(Collectors.toSet());

            List<String> missingUsernames = trainerUsernames.stream()
                    .filter(username -> !foundUsernames.contains(username))
                    .toList();

            throw new NotFoundException("Trainers not found for usernames: " + missingUsernames);
        }

        return foundTrainers;
    }

    @Override
    protected Function<String, Trainee> findByUsername() {
        return username -> traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    @Override
    protected User extractUser(Trainee entity) {
        return entity != null ? entity.getUser() : null;
    }
}