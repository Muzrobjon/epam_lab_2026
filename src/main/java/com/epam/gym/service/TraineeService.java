package com.epam.gym.service;


import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final Validator validator;

    @Transactional
    public Trainee createProfile(String firstName, String lastName,
                                 LocalDate dateOfBirth, String address) {
        log.info("Creating trainee profile for {} {}", firstName, lastName);

        Trainee trainee = Trainee.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth)
                .address(address)
                .isActive(true)
                .build();

        String username = usernameGenerator.generateUsername(
                trainee,
                name -> traineeRepository.existsByUserName(name)
        );
        trainee.setUserName(username);

        String generatedPassword = passwordGenerator.generatePassword(10);
        trainee.setPassword(generatedPassword);

        validateTrainee(trainee);

        Trainee saved = traineeRepository.save(trainee);

        log.info("Created trainee: {} with username: {}",
                saved.getId(), saved.getUserName());

        return saved;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.info("Authenticating trainee: {}", username);

        Trainee trainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new AuthenticationException("Trainee not found: " + username));

        if (!trainee.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid password for trainee: " + username);
        }

        log.info("Trainee authenticated successfully: {}", username);
    }


    @Transactional(readOnly = true)
    public Trainee selectByUsername(String username) {
        log.info("Selecting trainee by username: {}", username);
        return traineeRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for trainee: {}", username);

        authenticate(username, oldPassword);

        Trainee trainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        trainee.setPassword(newPassword);
        traineeRepository.save(trainee);

        log.info("Password changed for trainee: {}", username);
    }

    @Transactional
    public Trainee updateProfile(String username, String password, Trainee updatedTrainee) {
        log.info("Updating trainee profile: {}", username);

        authenticate(username, password);

        Trainee existing = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        if (updatedTrainee.getFirstName() != null) {
            existing.setFirstName(updatedTrainee.getFirstName());
        }
        if (updatedTrainee.getLastName() != null) {
            existing.setLastName(updatedTrainee.getLastName());
        }
        if (updatedTrainee.getDateOfBirth() != null) {
            existing.setDateOfBirth(updatedTrainee.getDateOfBirth());
        }
        if (updatedTrainee.getAddress() != null) {
            existing.setAddress(updatedTrainee.getAddress());
        }

        validateTrainee(existing);

        Trainee saved = traineeRepository.save(existing);

        log.info("Updated trainee profile: {}", username);
        return saved;
    }

    @Transactional
    public void toggleActiveStatus(String username, String password) {
        log.info("Toggling active status for trainee: {}", username);

        authenticate(username, password);

        Trainee trainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        trainee.setIsActive(!trainee.getIsActive());
        traineeRepository.save(trainee);

        log.info("Trainee {} is now {}", username,
                trainee.getIsActive() ? "active" : "inactive");
    }

    @Transactional
    public void deleteByUsername(String username, String password) {
        log.info("Deleting trainee profile: {}", username);

        authenticate(username, password);

        Trainee trainee = traineeRepository.findByUserName(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));

        traineeRepository.delete(trainee);

        log.info("Deleted trainee profile: {} (including all trainings)", username);
    }

    @Transactional
    public void updateTrainersList(String traineeUsername, String password,
                                   List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);

        authenticate(traineeUsername, password);

        Trainee trainee = traineeRepository.findByUserName(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + traineeUsername));

        trainee.getTrainers().clear();

        List<Trainer> trainers = trainerUsernames.stream()
                .map(username -> trainerRepository.findByUserName(username)
                        .orElseThrow(() -> new NotFoundException("Trainer not found: " + username)))
                .collect(Collectors.toList());

        trainee.setTrainers(trainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers list for trainee: {}. New trainers count: {}",
                traineeUsername, trainers.size());
    }

    @Transactional(readOnly = true)
    public Trainee selectProfile(Long id) {
        return traineeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    private void validateTrainee(Trainee trainee) {
        Set<ConstraintViolation<Trainee>> violations = validator.validate(trainee);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }
}