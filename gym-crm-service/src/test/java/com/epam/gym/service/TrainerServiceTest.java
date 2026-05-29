package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private PasswordService passwordService;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserService userService;

    @Mock
    private Validator validator;

    @InjectMocks
    private TrainerService trainerService;

    private static final String USERNAME = "jane.smith";
    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String RAW_PASSWORD = "rawPassword123";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    private User user;
    private Trainer trainer;
    private TrainingType trainingType;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("Jane")
                .lastName("Smith")
                .password(ENCODED_PASSWORD)
                .isActive(true)
                .build();

        trainingType = TrainingType.builder()
                .id(1L)
                .trainingTypeName(TrainingTypeName.YOGA)
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(trainingType)
                .trainees(List.of())
                .build();
    }

    @Test
    void createProfile_ValidRequest_ShouldReturnRegistrationResponse() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        User userWithRawPassword = User.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("Jane")
                .lastName("Smith")
                .password(RAW_PASSWORD)
                .isActive(true)
                .build();

        when(userService.createUser("Jane", "Smith")).thenReturn(userWithRawPassword);
        when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                .thenReturn(Optional.of(trainingType));
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        RegistrationResponse response = trainerService.createProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo(USERNAME);
        assertThat(response.getPassword()).isEqualTo(RAW_PASSWORD);

        verify(userService).createUser("Jane", "Smith");
        verify(passwordService).encodePassword(RAW_PASSWORD);
        verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.YOGA);
        verify(validator).validate(any(Trainer.class));
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void createProfile_TrainingTypeNotFound_ShouldThrowNotFoundException() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        User userWithRawPassword = User.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(userService.createUser("Jane", "Smith")).thenReturn(userWithRawPassword);
        when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.createProfile(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Training type not found");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void createProfile_ValidationFails_ShouldThrowValidationException() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        User userWithRawPassword = User.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .build();

        when(userService.createUser("Jane", "Smith")).thenReturn(userWithRawPassword);
        when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                .thenReturn(Optional.of(trainingType));

        ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Specialization is required");
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> trainerService.createProfile(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getByUsername_ExistingUsername_ShouldReturnTrainer() {
        when(trainerRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getByUsername(USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);

        verify(trainerRepository).findByUser_Username(USERNAME);
    }

    @Test
    void getByUsername_NonExistingUsername_ShouldThrowNotFoundException() {
        when(trainerRepository.findByUser_Username("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getByUsername("unknown"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found: unknown");

        verify(trainerRepository).findByUser_Username("unknown");
    }

    @Test
    void updateProfile_ValidRequest_ShouldReturnUpdatedTrainer() {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(USERNAME);
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainer));
        doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of());
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.updateProfile(USERNAME, request);

        assertThat(result).isNotNull();

        verify(userService).isAuthenticated(USERNAME);
        verify(userService).updateUserBasicInfo(
                eq(user), eq("Jane"), eq("Updated"), eq(true));
        verify(validator).validate(any(Trainer.class));
        verify(trainerRepository).save(trainer);
    }

    @Test
    void updateProfile_TrainerNotFound_ShouldThrowNotFoundException() {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(USERNAME);
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainer not found");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateProfile_ValidationFails_ShouldThrowValidationException() {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(USERNAME);
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerRepository.findByUser_Username(USERNAME))
                .thenReturn(Optional.of(trainer));
        doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());

        ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Specialization is required");
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void getUnassignedTrainers_ValidTrainee_ShouldReturnList() {
        Trainer trainer2 = Trainer.builder()
                .id(2L)
                .user(User.builder().username("trainer2").build())
                .build();

        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(User.builder().username(TRAINEE_USERNAME).build())
                .build();

        doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
        when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of(trainer, trainer2));

        List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(t -> t.getUser().getUsername())
                .containsExactlyInAnyOrder(USERNAME, "trainer2");

        verify(userService).isAuthenticated(TRAINEE_USERNAME);
        verify(traineeRepository).findByUser_Username(TRAINEE_USERNAME);
        verify(trainerRepository).findAvailableTrainers(TRAINEE_USERNAME);
    }

    @Test
    void getUnassignedTrainers_EmptyResult_ShouldReturnEmptyList() {
        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(User.builder().username(TRAINEE_USERNAME).build())
                .build();

        doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
        when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of());

        List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

        assertThat(result).isEmpty();
    }

    @Test
    void getUnassignedTrainers_TraineeNotFound_ShouldThrowNotFoundException() {
        doNothing().when(userService).isAuthenticated(TRAINEE_USERNAME);
        when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Trainee not found");

        verify(trainerRepository, never()).findAvailableTrainers(any());
    }
}