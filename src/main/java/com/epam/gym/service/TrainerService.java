package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TraineeRepository;
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
    private final TraineeRepository traineeRepository;
    private final UserService userService;
    private final Validator validator;


    @Transactional
    public Trainer createProfile(TrainerRegistrationRequest request) {
        log.info("Creating trainer profile for {} {}", request.getFirstName(), request.getLastName());

        User savedUser = userService.createUser(request.getFirstName(), request.getLastName());

        TrainingType trainingType = trainingTypeRepository.findByTrainingTypeName(request.getSpecialization())
                .orElseThrow(() -> new NotFoundException("Training type not found: " + request.getSpecialization()));

        Trainer trainer = Trainer.builder()
                .user(savedUser)
                .specialization(trainingType)
                .trainees(List.of())
                .build();

        validateEntity(trainer);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Trainer profile created for ID: {}, username: {}", saved.getId(), savedUser.getUsername());

        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(String username) {
        log.info("Retrieving trainer by username: {}", username);
        return trainerRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainer not found: " + username));
    }

    @Transactional
    public Trainer updateProfile(String username, UpdateTrainerRequest request) {
        log.info("Updating trainer profile for username: {}", username);

        userService.authenticate(username, request.getPassword());

        Trainer trainer = getByUsername(username);

        userService.updateUserBasicInfo(
                trainer.getUser(),
                request.getFirstName(),
                request.getLastName(),
                request.getIsActive()
        );

        validateEntity(trainer);

        Trainer saved = trainerRepository.save(trainer);
        log.info("Trainer profile updated successfully: {}", username);

        return saved;
    }

    public void authenticate(String username, String password) {
        log.info("Authenticating trainer: {}", username);

        userService.authenticate(username, password);

        Trainer trainer = getByUsername(username);
        log.info("Trainer authenticated successfully: {}", trainer.getUser().getUsername());
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername, String password) {
        log.info("Retrieving unassigned trainers for trainee: {}", traineeUsername);

        userService.authenticate(traineeUsername, password);

        traineeRepository.findByUser_Username(traineeUsername)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + traineeUsername));

        return trainerRepository.findAvailableTrainers(traineeUsername);
    }

    private void validateEntity(Trainer trainer) {
        Set<ConstraintViolation<Trainer>> violations = validator.validate(trainer);
        if (!violations.isEmpty()) {
            String errors = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + errors);
        }
    }
}