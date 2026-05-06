package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.TrainerWorkloadRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private WorkloadMessageProducer workloadMessageProducer;

    @Mock
    private PasswordService passwordService;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserService userService;

    @Mock
    private Validator validator;

    @InjectMocks
    private TraineeService traineeService;

    // --- Test data ---
    private static final String USERNAME = "john.doe";

    private User user;
    private Trainee trainee;
    private Trainer trainer;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("John")
                .lastName("Doe")
                .password("rawPassword")
                .isActive(true)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .user(User.builder()
                        .username("trainer1")
                        .build())
                .build();
    }

    // ==================== createProfile ====================

    @Test
    void createProfile_ValidRequest_ShouldReturnRegistrationResponse() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("123 Main St");

        when(userService.createUser("John", "Doe")).thenReturn(user);
        when(passwordService.encodePassword("rawPassword")).thenReturn("encodedPassword");
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        RegistrationResponse response = traineeService.createProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(USERNAME);
        assertThat(response.getPassword()).isEqualTo("rawPassword");

        verify(userService).createUser("John", "Doe");
        verify(passwordService).encodePassword("rawPassword");
        verify(validator).validate(any(Trainee.class));
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void createProfile_ValidationFails_ShouldThrowValidationException() {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userService.createUser("John", "Doe")).thenReturn(user);
        when(passwordService.encodePassword(any())).thenReturn("encodedPassword");

        ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Address is required");
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> traineeService.createProfile(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");

        verify(traineeRepository, never()).save(any());
    }

    // ==================== getByUsername ====================

    @Test
    void getByUsername_ExistingUsername_ShouldReturnTrainee() {
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getByUsername(USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);

        verify(traineeRepository).findByUser_Username(USERNAME);
    }

    @Test
    void getByUsername_NonExistingUsername_ShouldThrowNotFoundException() {
        when(traineeRepository.findByUser_Username("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.getByUsername("unknown"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found: unknown");

        verify(traineeRepository).findByUser_Username("unknown");
    }

    // ==================== updateProfile ====================

    @Test
    void updateProfile_ValidRequest_ShouldReturnUpdatedTrainee() {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Updated");
        request.setIsActive(true);
        request.setDateOfBirth(LocalDate.of(1992, 5, 15));
        request.setAddress("456 New St");

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateProfile(USERNAME, request);

        assertThat(result).isNotNull();
        assertThat(result.getAddress()).isEqualTo("456 New St");
        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1992, 5, 15));

        verify(userService).isAuthenticated(USERNAME);
        verify(userService).updateUserBasicInfo(any(), eq("John"), eq("Updated"), eq(true));
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void updateProfile_NullOptionalFields_ShouldNotOverwriteExistingValues() {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of());
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.updateProfile(USERNAME, request);

        assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(result.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void updateProfile_TraineeNotFound_ShouldThrowNotFoundException() {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(traineeRepository, never()).save(any());
    }

    // ==================== deleteByUsername ====================

    @Test
    void deleteByUsername_ExistingTrainee_ShouldDeleteSuccessfully() {
        // Build training with trainer info for workload notification
        Training training = Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2024, 6, 1))
                .trainingDurationMinutes(60)
                .build();

        trainee.setTrainings(new ArrayList<>(List.of(training)));

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        doNothing().when(workloadMessageProducer).sendNotification(
                any(), any(), any(), any());
        doNothing().when(traineeRepository).delete(trainee);

        traineeService.deleteByUsername(USERNAME);

        verify(userService).isAuthenticated(USERNAME);
        verify(workloadMessageProducer).sendNotification(
                eq(trainer),
                eq(LocalDate.of(2024, 6, 1)),
                eq(60),
                eq(TrainerWorkloadRequest.ActionType.DELETE)
        );
        verify(traineeRepository).delete(trainee);
    }

    @Test
    void deleteByUsername_TraineeWithNoTrainings_ShouldDeleteWithoutNotification() {
        trainee.setTrainings(new ArrayList<>()); // no trainings

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        doNothing().when(traineeRepository).delete(trainee);

        traineeService.deleteByUsername(USERNAME);

        verify(traineeRepository).delete(trainee);
        verifyNoInteractions(workloadMessageProducer);
    }

    @Test
    void deleteByUsername_TraineeNotFound_ShouldThrowNotFoundException() {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.deleteByUsername(USERNAME))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(traineeRepository, never()).delete(any());
        verifyNoInteractions(workloadMessageProducer);
    }

    // ==================== updateTrainersList ====================

    @Test
    void updateTrainersList_ValidUsernames_ShouldReturnUpdatedTrainers() {
        List<String> trainerUsernames = List.of("trainer1", "trainer2");

        Trainer trainer2 = Trainer.builder()
                .id(2L)
                .user(User.builder().username("trainer2").build())
                .build();

        List<Trainer> trainers = List.of(trainer, trainer2);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                .thenReturn(trainers);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        List<Trainer> result = traineeService.updateTrainersList(USERNAME, trainerUsernames);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder("trainer1", "trainer2");

        verify(traineeRepository).save(trainee);
    }

    @Test
    void updateTrainersList_EmptyList_ShouldClearTrainers() {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        List<Trainer> result = traineeService.updateTrainersList(USERNAME, List.of());

        assertThat(result).isEmpty();
        assertThat(trainee.getTrainers()).isEmpty();

        verify(traineeRepository).save(trainee);
        verify(trainerRepository, never()).findByUser_UsernameIn(any());
    }

    @Test
    void updateTrainersList_SomeTrainersNotFound_ShouldThrowNotFoundException() {
        List<String> trainerUsernames = List.of("trainer1", "nonexistent");

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainee));
        // Only 1 found, but 2 requested
        when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                .thenReturn(List.of(trainer));

        assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, trainerUsernames))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainers not found")
                .hasMessageContaining("nonexistent");

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTrainersList_TraineeNotFound_ShouldThrowNotFoundException() {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> traineeService.updateTrainersList(USERNAME, List.of("trainer1")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(traineeRepository, never()).save(any());
        verify(trainerRepository, never()).findByUser_UsernameIn(any());
    }
}