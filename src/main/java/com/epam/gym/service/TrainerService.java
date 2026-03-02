package com.epam.gym.service;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final Validator validator;

    @Transactional
    public Trainer createProfile(String firstName,
                                 String lastName,
                                 String specializationName) {

        log.info("Creating trainer profile for {} {}", firstName, lastName);

        TrainingType specialization = trainingTypeRepository
                .findByTrainingTypeName(specializationName)
                .orElseThrow(() ->
                        new NotFoundException("Training type not found: " + specializationName));

        Trainer trainer = Trainer.builder()
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization)
                .isActive(true)
                .build();

        String username = usernameGenerator.generateUsername(
                trainer,
                name -> trainerRepository.existsByUserName(name)
        );
        trainer.setUserName(username);

        String generatedPassword = passwordGenerator.generatePassword(10);
        trainer.setPassword(generatedPassword);

        validateTrainer(trainer);

        Trainer saved = trainerRepository.save(trainer);

        log.info("Created trainer: {} with username: {}",
                saved.getUserId(),
                saved.getUserName());

        return saved;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.info("Authenticating trainer: {}", username);

        Trainer trainer = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new AuthenticationException("Trainer not found: " + username));

        if (!trainer.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid password for trainer: " + username);
        }

        log.info("Trainer authenticated successfully: {}", username);
    }

    @Transactional(readOnly = true)
    public Trainer selectByUsername(String username) {
        log.info("Selecting trainer by username: {}", username);
        return trainerRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainer: {}", username);

        authenticate(username, oldPassword);

        Trainer trainer = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        trainer.setPassword(newPassword);
        trainerRepository.save(trainer);

        log.info("Password changed for trainer: {}", username);
    }

    @Transactional
    public Trainer updateProfile(String username, String password, Trainer updatedTrainer) {
        log.info("Updating trainer profile: {}", username);

        authenticate(username, password);

        Trainer existing = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        if (updatedTrainer.getFirstName() != null) {
            existing.setFirstName(updatedTrainer.getFirstName());
        }
        if (updatedTrainer.getLastName() != null) {
            existing.setLastName(updatedTrainer.getLastName());
        }
        if (updatedTrainer.getSpecialization() != null
                && updatedTrainer.getSpecialization().getTrainingTypeName() != null) {

            TrainingType spec = trainingTypeRepository
                    .findByTrainingTypeName(updatedTrainer.getSpecialization().getTrainingTypeName())
                    .orElseThrow(() ->
                            new NotFoundException("Training type not found: "
                                    + updatedTrainer.getSpecialization().getTrainingTypeName()));

            existing.setSpecialization(spec);
        }

        validateTrainer(existing);

        Trainer saved = trainerRepository.save(existing);

        log.info("Updated trainer profile: {}", username);
        return saved;
    }

    @Transactional
    public void toggleActiveStatus(String username, String password) {
        log.info("Toggling active status for trainer: {}", username);

        authenticate(username, password);

        Trainer trainer = trainerRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));

        trainer.setIsActive(!trainer.getIsActive());
        trainerRepository.save(trainer);

        log.info("Trainer {} is now {}", username,
                trainer.getIsActive() ? "active" : "inactive");
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.info("Getting unassigned trainers for trainee: {}", traineeUsername);
        return trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);
    }

    @Transactional(readOnly = true)
    public Trainer selectProfile(Long id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    private void validateTrainer(Trainer trainer) {
        Set<ConstraintViolation<Trainer>> violations = validator.validate(trainer);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }
}