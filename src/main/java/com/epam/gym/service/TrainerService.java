package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class TrainerService extends AbstractUserService<Trainer> {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainerService(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainingTypeRepository trainingTypeRepository,
            UserRepository userRepository,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            Validator validator
    ) {
        super(userRepository, usernameGenerator, passwordGenerator, validator);
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Transactional
    public Trainer createProfile(String firstName, String lastName, TrainingTypeName specialization) {
        log.info("Creating trainer profile for {} {}", firstName, lastName);

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

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(specialization)
                .orElseThrow(() -> new NotFoundException("Training type not found: " + specialization));

        Trainer trainer = Trainer.builder()
                .id(savedUser.getId())
                .user(savedUser)
                .specialization(trainingType)
                .build();

        validateEntity(trainer);

        Trainer saved = trainerRepository.save(trainer);

        log.info("Created trainer: {} with username: {} and password: {}",
                saved.getId(), savedUser.getUsername(), rawPassword);

        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer selectByUsername(String username) {
        log.info("Selecting trainer by username: {}", username);
        return trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    @Transactional
    public Trainer updateProfile(String username, String password, Trainer updatedTrainer) {
        log.info("Updating trainer profile: {}", username);

        authenticateUser(username, password);

        Trainer existing = selectByUsername(username);

        User user = existing.getUser();

        if (updatedTrainer.getUser() != null) {
            if (updatedTrainer.getUser().getFirstName() != null) {
                user.setFirstName(updatedTrainer.getUser().getFirstName());
            }
            if (updatedTrainer.getUser().getLastName() != null) {
                user.setLastName(updatedTrainer.getUser().getLastName());
            }
        }

        if (updatedTrainer.getSpecialization() != null) {
            existing.setSpecialization(updatedTrainer.getSpecialization());
        }

        validateEntity(existing);

        Trainer saved = trainerRepository.save(existing);

        log.info("Updated trainer profile: {}", username);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.info("Getting unassigned trainers for trainee: {}", traineeUsername);

        traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + traineeUsername));

        List<Trainer> unassignedTrainers = trainerRepository.findAvailableTrainers(traineeUsername);

        log.info("Found {} unassigned trainers for trainee: {}", unassignedTrainers.size(), traineeUsername);

        return unassignedTrainers;
    }

    @Override
    protected Function<String, Trainer> findByUsername() {
        return username -> trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    @Override
    protected User extractUser(Trainer entity) {
        return entity != null ? entity.getUser() : null;
    }
}