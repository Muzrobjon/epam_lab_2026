package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final String USERNAME = "Mike.Tyson";
    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "RawPass@123";
    private static final String ENCODED_PASSWORD = "$2a$12$encodedHash";

    private User user;
    private Trainer trainer;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("Mike")
                .lastName("Tyson")
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .isActive(true)
                .build();

        specialization = TrainingType.builder()
                .id(1L)
                .trainingTypeName(TrainingTypeName.FITNESS)
                .build();

        trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(specialization)
                .trainees(List.of())
                .build();
    }

    // ==================== CREATE PROFILE TESTS ====================

    @Nested
    @DisplayName("Create Profile Tests")
    class CreateProfileTests {

        private TrainerRegistrationRequest request;

        @BeforeEach
        void setUp() {
            request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Tyson");
            request.setSpecialization(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should create trainer profile successfully")
        void createProfile_Success() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(specialization));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

            // When
            RegistrationResponse response = trainerService.createProfile(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getPassword()).isEqualTo(RAW_PASSWORD);

            verify(userService).createUser("Mike", "Tyson");
            verify(passwordService).encodePassword(RAW_PASSWORD);
            verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.FITNESS);
            verify(validator).validate(any(Trainer.class));
            verify(trainerRepository).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createProfile_EncodesPassword() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(specialization));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
                Trainer saved = invocation.getArgument(0);
                assertThat(saved.getUser().getPassword()).isEqualTo(ENCODED_PASSWORD);
                return saved;
            });

            // When
            trainerService.createProfile(request);

            // Then
            verify(passwordService).encodePassword(RAW_PASSWORD);
        }

        @Test
        @DisplayName("Should return raw password in response")
        void createProfile_ReturnsRawPassword() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(specialization));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

            // When
            RegistrationResponse response = trainerService.createProfile(request);

            // Then
            assertThat(response.getPassword()).isEqualTo(RAW_PASSWORD);
            assertThat(response.getPassword()).isNotEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("Should set specialization from training type")
        void createProfile_SetsSpecialization() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(specialization));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
                Trainer saved = invocation.getArgument(0);
                assertThat(saved.getSpecialization()).isEqualTo(specialization);
                assertThat(saved.getSpecialization().getTrainingTypeName())
                        .isEqualTo(TrainingTypeName.FITNESS);
                return saved;
            });

            // When
            trainerService.createProfile(request);

            // Then
            verify(trainerRepository).save(any(Trainer.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void createProfile_TrainingTypeNotFound_ThrowsNotFoundException() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found: FITNESS");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails_ThrowsValidationException() {
            // Given
            when(userService.createUser("Mike", "Tyson")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(specialization));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User must not be null");
            when(validator.validate(any(Trainer.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User must not be null");

            verify(trainerRepository, never()).save(any());
        }
    }

    // ==================== GET BY USERNAME TESTS ====================

    @Nested
    @DisplayName("Get By Username Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should get trainer by username successfully")
        void getByUsername_Success() {
            // Given
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));

            // When
            Trainer result = trainerService.getByUsername(USERNAME);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);
            assertThat(result.getSpecialization().getTrainingTypeName())
                    .isEqualTo(TrainingTypeName.FITNESS);

            verify(trainerRepository).findByUser_Username(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void getByUsername_NotFound_ThrowsNotFoundException() {
            // Given
            when(trainerRepository.findByUser_Username("NonExistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getByUsername("NonExistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found: NonExistent");
        }
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        private UpdateTrainerRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTrainerRequest();
            updateRequest.setUsername(USERNAME);
            updateRequest.setFirstName("Mike");
            updateRequest.setLastName("Updated");
            updateRequest.setIsActive(true);
        }

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateProfile_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
            doNothing().when(userService).updateUserBasicInfo(
                    any(User.class), eq("Mike"), eq("Updated"), eq(true));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

            // When
            Trainer result = trainerService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(result).isNotNull();

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(userService).updateUserBasicInfo(user, "Mike", "Updated", true);
            verify(validator).validate(trainer);
            verify(trainerRepository).save(trainer);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void updateProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(AuthenticationException.class);

            verify(trainerRepository, never()).findByUser_Username(anyString());
            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void updateProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void updateProfile_ValidationFails_ThrowsValidationException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid field");
            when(validator.validate(any(Trainer.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should delegate user info update to UserService")
        void updateProfile_DelegatesUserInfoUpdate() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainer));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

            // When
            trainerService.updateProfile(USERNAME, updateRequest);

            // Then
            verify(userService).updateUserBasicInfo(
                    eq(user),
                    eq("Mike"),
                    eq("Updated"),
                    eq(true)
            );
        }
    }

    // ==================== GET UNASSIGNED TRAINERS TESTS ====================

    @Nested
    @DisplayName("Get Unassigned Trainers Tests")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should get unassigned trainers successfully")
        void getUnassignedTrainers_Success() {
            // Given
            User trainerUser1 = User.builder()
                    .username("Trainer.One")
                    .firstName("Trainer")
                    .lastName("One")
                    .build();

            User trainerUser2 = User.builder()
                    .username("Trainer.Two")
                    .firstName("Trainer")
                    .lastName("Two")
                    .build();

            List<Trainer> availableTrainers = List.of(
                    Trainer.builder().id(1L).user(trainerUser1).specialization(specialization).build(),
                    Trainer.builder().id(2L).user(trainerUser2).specialization(specialization).build()
            );

            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                    .thenReturn(Optional.of(mock(com.epam.gym.entity.Trainee.class)));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME))
                    .thenReturn(availableTrainers);

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getUser().getUsername()).isEqualTo("Trainer.One");
            assertThat(result.get(1).getUser().getUsername()).isEqualTo("Trainer.Two");

            verify(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            verify(traineeRepository).findByUser_Username(TRAINEE_USERNAME);
            verify(trainerRepository).findAvailableTrainers(TRAINEE_USERNAME);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_NoTrainers_ReturnsEmptyList() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                    .thenReturn(Optional.of(mock(com.epam.gym.entity.Trainee.class)));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME))
                    .thenReturn(List.of());

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void getUnassignedTrainers_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(TRAINEE_USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeRepository, never()).findByUser_Username(anyString());
            verify(trainerRepository, never()).findAvailableTrainers(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getUnassignedTrainers_TraineeNotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: " + TRAINEE_USERNAME);

            verify(trainerRepository, never()).findAvailableTrainers(anyString());
        }

        @Test
        @DisplayName("Should pass correct trainee username to repository")
        void getUnassignedTrainers_PassesCorrectUsername() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(TRAINEE_USERNAME);
            when(traineeRepository.findByUser_Username(TRAINEE_USERNAME))
                    .thenReturn(Optional.of(mock(com.epam.gym.entity.Trainee.class)));
            when(trainerRepository.findAvailableTrainers(TRAINEE_USERNAME))
                    .thenReturn(List.of());

            // When
            trainerService.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            verify(trainerRepository).findAvailableTrainers(eq(TRAINEE_USERNAME));
        }
    }
}