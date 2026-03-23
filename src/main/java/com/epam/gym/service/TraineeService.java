package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserService userService;
    private final Validator validator;

    @Transactional
    public Trainee createProfile(TraineeRegistrationRequest request)  {
        log.info("Creating trainee profile for {} {}", request.getFirstName(), request.getLastName());

        User savedUser = userService.createUser(request.getFirstName(), request.getLastName());

        Trainee trainee = Trainee.builder()
                .user(savedUser)
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .trainers(new ArrayList<>())
                .build();

        validateEntity(trainee);

        Trainee saved = traineeRepository.save(trainee);
        log.info("Created trainee: {} with username: {}", saved.getId(), savedUser.getUsername());

        return saved;
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        log.debug("Selecting trainee by username: {}", username);
        return traineeRepository.findByUser_Username(username)
                .orElseThrow(() -> new NotFoundException("Trainee not found: " + username));
    }

    @Transactional
    public Trainee updateProfile(String username, UpdateTraineeRequest request) {
        log.info("Updating trainee profile: {}", username);

        userService.isAuthenticated(request.getUsername());

        Trainee existing = getByUsername(username);

        userService.updateUserBasicInfo(
                existing.getUser(),
                request.getFirstName(),
                request.getLastName(),
                request.getIsActive()
        );

        if (request.getDateOfBirth() != null) {
            existing.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getAddress() != null) {
            existing.setAddress(request.getAddress());
        }

        validateEntity(existing);

        Trainee saved = traineeRepository.save(existing);
        log.info("Updated trainee profile: {}", username);

        return saved;
    }

    @Transactional
    public void deleteByUsername(String username) {
        log.info("Deleting trainee profile: {}", username);

        userService.isAuthenticated(username);

        Trainee trainee = getByUsername(username);
        traineeRepository.delete(trainee);

        log.info("Deleted trainee profile: {}", username);
    }

    @Transactional
    public List<Trainer> updateTrainersList(String traineeUsername, List<String> trainerUsernames) {
        log.info("Updating trainers list for trainee: {}", traineeUsername);

        userService.isAuthenticated(traineeUsername);

        Trainee trainee = getByUsername(traineeUsername);
        List<Trainer> trainers = fetchTrainersByUsernames(trainerUsernames);

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);
        traineeRepository.save(trainee);

        log.info("Updated trainers list for trainee: {} with {} trainers", traineeUsername, trainers.size());

        return trainers;
    }


    private List<Trainer> fetchTrainersByUsernames(List<String> trainerUsernames) {
        if (trainerUsernames == null || trainerUsernames.isEmpty()) {
            return new ArrayList<>();
        }

        List<Trainer> foundTrainers = trainerRepository.findByUser_UsernameIn(trainerUsernames);

        if (foundTrainers.size() != trainerUsernames.size()) {
            Set<String> foundUsernames = foundTrainers.stream()
                    .map(t -> t.getUser().getUsername())
                    .collect(Collectors.toSet());

            List<String> missingUsernames = trainerUsernames.stream()
                    .filter(u -> !foundUsernames.contains(u))
                    .toList();

            throw new NotFoundException("Trainers not found: " + missingUsernames);
        }

        return foundTrainers;
    }

    private void validateEntity(Trainee entity) {
        Set<ConstraintViolation<Trainee>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }
}