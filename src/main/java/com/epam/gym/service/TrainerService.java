package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TrainerRepository;
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
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final Validator validator;

    @Transactional
    public Trainer createProfile(String firstName,
                                 String lastName,
                                 TrainingTypeName specialization) {

        log.info("Creating trainer profile for {} {}", firstName, lastName);

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
                saved.getId(),
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
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        log.info("Getting unassigned trainers for trainee: {}", traineeUsername);
        return trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername);
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