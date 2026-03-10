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

        String rawPassword = passwordGenerator.generatePassword();

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .password(rawPassword)
                .build();

        String username = usernameGenerator.generateUsername(user, userRepository::existsByUsername);
        user.setUsername(username);

        User savedUser = userRepository.save(user);

        Trainee trainee = Trainee.builder()
                .id(savedUser.getId())
                .user(savedUser)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .build();

        validateEntity(trainee);

        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee: {} with username: {} and password: {}",
                saved.getId(), savedUser.getUsername(), rawPassword);

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

        authenticate(username, password);

        Trainee existing = selectByUsername(username);

        User user = existing.getUser();

        if (updatedTrainee.getUser() != null) {
            if (updatedTrainee.getUser().getFirstName() != null) {
                user.setFirstName(updatedTrainee.getUser().getFirstName());
            }
            if (updatedTrainee.getUser().getLastName() != null) {
                user.setLastName(updatedTrainee.getUser().getLastName());
            }
        }

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

        authenticate(username, password);

        Trainee trainee = selectByUsername(username);

        traineeRepository.delete(trainee);

        log.info("Deleted trainee profile: {}", username);
    }

    @Transactional
    public void updateTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);

        authenticate(traineeUsername, password);

        Trainee trainee = selectByUsername(traineeUsername);

        trainee.getTrainers().clear();

        List<Trainer> trainers = trainerUsernames.stream()
                .map(trainerUsername -> trainerRepository.findByUser_Username(trainerUsername)
                        .orElseThrow(() -> new NotFoundException("Trainer not found: " + trainerUsername)))
                .collect(Collectors.toList());

        trainee.setTrainers(trainers);

        log.info("Updated trainers list for trainee: {}", traineeUsername);
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