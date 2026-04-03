package com.epam.gym.service;

import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

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

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "RawPass@123";
    private static final String ENCODED_PASSWORD = "$2a$12$encodedHash";

    private User user;
    private Trainee trainee;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .isActive(true)
                .build();

        trainee = Trainee.builder()
                .id(1L)
                .user(user)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .address("123 Main St")
                .trainers(new ArrayList<>())
                .build();
    }

    // ==================== CREATE PROFILE TESTS ====================

    @Nested
    @DisplayName("Create Profile Tests")
    class CreateProfileTests {

        private TraineeRegistrationRequest request;

        @BeforeEach
        void setUp() {
            request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setDateOfBirth(LocalDate.of(1995, 5, 15));
            request.setAddress("123 Main St");
        }

        @Test
        @DisplayName("Should create trainee profile successfully")
        void createProfile_Success() {
            // Given
            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            RegistrationResponse response = traineeService.createProfile(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getPassword()).isEqualTo(RAW_PASSWORD);

            verify(userService).createUser("John", "Doe");
            verify(passwordService).encodePassword(RAW_PASSWORD);
            verify(validator).validate(any(Trainee.class));
            verify(traineeRepository).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createProfile_EncodesPassword() {
            // Given
            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee saved = invocation.getArgument(0);
                assertThat(saved.getUser().getPassword()).isEqualTo(ENCODED_PASSWORD);
                return saved;
            });

            // When
            traineeService.createProfile(request);

            // Then
            verify(passwordService).encodePassword(RAW_PASSWORD);
        }

        @Test
        @DisplayName("Should return raw password in response")
        void createProfile_ReturnsRawPassword() {
            // Given
            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            RegistrationResponse response = traineeService.createProfile(request);

            // Then
            assertThat(response.getPassword()).isEqualTo(RAW_PASSWORD);
            assertThat(response.getPassword()).isNotEqualTo(ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("Should set date of birth and address from request")
        void createProfile_SetsFieldsFromRequest() {
            // Given
            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
                Trainee saved = invocation.getArgument(0);
                assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 15));
                assertThat(saved.getAddress()).isEqualTo("123 Main St");
                assertThat(saved.getTrainers()).isEmpty();
                return saved;
            });

            // When
            traineeService.createProfile(request);

            // Then
            verify(traineeRepository).save(any(Trainee.class));
        }

        @Test
        @DisplayName("Should create profile without optional fields")
        void createProfile_WithoutOptionalFields_Success() {
            // Given
            request.setDateOfBirth(null);
            request.setAddress(null);

            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            RegistrationResponse response = traineeService.createProfile(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails_ThrowsValidationException() {
            // Given
            when(userService.createUser("John", "Doe")).thenReturn(user);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User must not be null");
            when(validator.validate(any(Trainee.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> traineeService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User must not be null");

            verify(traineeRepository, never()).save(any());
        }
    }

    // ==================== GET BY USERNAME TESTS ====================

    @Nested
    @DisplayName("Get By Username Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should get trainee by username successfully")
        void getByUsername_Success() {
            // Given
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));

            // When
            Trainee result = traineeService.getByUsername(USERNAME);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo(USERNAME);
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 15));
            assertThat(result.getAddress()).isEqualTo("123 Main St");

            verify(traineeRepository).findByUser_Username(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getByUsername_NotFound_ThrowsNotFoundException() {
            // Given
            when(traineeRepository.findByUser_Username("NonExistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.getByUsername("NonExistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found: NonExistent");
        }
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        private UpdateTraineeRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTraineeRequest();
            updateRequest.setUsername(USERNAME);
            updateRequest.setFirstName("John");
            updateRequest.setLastName("Updated");
            updateRequest.setDateOfBirth(LocalDate.of(1996, 6, 20));
            updateRequest.setAddress("456 New St");
            updateRequest.setIsActive(true);
        }

        @Test
        @DisplayName("Should update trainee profile successfully")
        void updateProfile_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(User.class),
                    eq("John"), eq("Updated"), eq(true));
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            Trainee result = traineeService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(result).isNotNull();

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(userService).updateUserBasicInfo(user, "John", "Updated", true);
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("Should update date of birth")
        void updateProfile_UpdatesDateOfBirth() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            traineeService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(trainee.getDateOfBirth()).isEqualTo(LocalDate.of(1996, 6, 20));
        }

        @Test
        @DisplayName("Should update address")
        void updateProfile_UpdatesAddress() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            traineeService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(trainee.getAddress()).isEqualTo("456 New St");
        }

        @Test
        @DisplayName("Should not update date of birth when null")
        void updateProfile_NullDateOfBirth_KeepsExisting() {
            // Given
            updateRequest.setDateOfBirth(null);
            LocalDate originalDate = trainee.getDateOfBirth();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            traineeService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(trainee.getDateOfBirth()).isEqualTo(originalDate);
        }

        @Test
        @DisplayName("Should not update address when null")
        void updateProfile_NullAddress_KeepsExisting() {
            // Given
            updateRequest.setAddress(null);
            String originalAddress = trainee.getAddress();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());
            when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            traineeService.updateProfile(USERNAME, updateRequest);

            // Then
            assertThat(trainee.getAddress()).isEqualTo(originalAddress);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void updateProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeRepository, never()).findByUser_Username(anyString());
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void updateProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void updateProfile_ValidationFails_ThrowsValidationException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(userService).updateUserBasicInfo(any(), any(), any(), any());

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Invalid field");
            when(validator.validate(any(Trainee.class))).thenReturn(Set.of(violation));

            // When & Then
            assertThatThrownBy(() -> traineeService.updateProfile(USERNAME, updateRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(traineeRepository, never()).save(any());
        }
    }

    // ==================== DELETE BY USERNAME TESTS ====================

    @Nested
    @DisplayName("Delete By Username Tests")
    class DeleteByUsernameTests {

        @Test
        @DisplayName("Should delete trainee successfully")
        void deleteByUsername_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            doNothing().when(traineeRepository).delete(trainee);

            // When
            traineeService.deleteByUsername(USERNAME);

            // Then
            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(traineeRepository).delete(trainee);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void deleteByUsername_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeService.deleteByUsername(USERNAME))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeRepository, never()).findByUser_Username(anyString());
            verify(traineeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void deleteByUsername_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> traineeService.deleteByUsername(USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(traineeRepository, never()).delete(any());
        }
    }

    // ==================== UPDATE TRAINERS LIST TESTS ====================

    @Nested
    @DisplayName("Update Trainers List Tests")
    class UpdateTrainersListTests {

        private List<Trainer> trainerList;

        @BeforeEach
        void setUp() {
            TrainingType specialization = TrainingType.builder()
                    .id(1L)
                    .trainingTypeName(TrainingTypeName.FITNESS)
                    .build();

            User trainerUser1 = User.builder()
                    .id(2L)
                    .firstName("Trainer")
                    .lastName("One")
                    .username("Trainer.One")
                    .build();

            User trainerUser2 = User.builder()
                    .id(3L)
                    .firstName("Trainer")
                    .lastName("Two")
                    .username("Trainer.Two")
                    .build();

            trainerList = List.of(
                    Trainer.builder().id(1L).user(trainerUser1).specialization(specialization).build(),
                    Trainer.builder().id(2L).user(trainerUser2).specialization(specialization).build()
            );
        }

        @Test
        @DisplayName("Should update trainers list successfully")
        void updateTrainersList_Success() {
            // Given
            List<String> trainerUsernames = List.of("Trainer.One", "Trainer.Two");

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainerList);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Then
            assertThat(result).hasSize(2);
            assertThat(trainee.getTrainers()).hasSize(2);

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeRepository).findByUser_Username(USERNAME);
            verify(trainerRepository).findByUser_UsernameIn(trainerUsernames);
            verify(traineeRepository).save(trainee);
        }

        @Test
        @DisplayName("Should clear existing trainers and set new ones")
        void updateTrainersList_ClearsExistingTrainers() {
            // Given
            trainee.getTrainers().add(Trainer.builder().id(99L).build());
            assertThat(trainee.getTrainers()).hasSize(1);

            List<String> trainerUsernames = List.of("Trainer.One", "Trainer.Two");

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames)).thenReturn(trainerList);
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            traineeService.updateTrainersList(USERNAME, trainerUsernames);

            // Then
            assertThat(trainee.getTrainers()).hasSize(2);
            assertThat(trainee.getTrainers()).containsExactlyElementsOf(trainerList);
        }

        @Test
        @DisplayName("Should handle empty trainer usernames list")
        void updateTrainersList_EmptyList_ClearsTrainers() {
            // Given
            trainee.getTrainers().add(Trainer.builder().id(99L).build());

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, List.of());

            // Then
            assertThat(result).isEmpty();
            assertThat(trainee.getTrainers()).isEmpty();

            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
        }

        @Test
        @DisplayName("Should handle null trainer usernames list")
        void updateTrainersList_NullList_ClearsTrainers() {
            // Given
            trainee.getTrainers().add(Trainer.builder().id(99L).build());

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

            // When
            List<Trainer> result = traineeService.updateTrainersList(USERNAME, null);

            // Then
            assertThat(result).isEmpty();
            assertThat(trainee.getTrainers()).isEmpty();

            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
        }

        @Test
        @DisplayName("Should throw NotFoundException when some trainers not found")
        void updateTrainersList_TrainerNotFound_ThrowsNotFoundException() {
            // Given
            List<String> trainerUsernames = List.of("Trainer.One", "NonExistent.Trainer");

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(List.of(trainerList.getFirst())); // Only 1 found out of 2

            // When & Then
            assertThatThrownBy(() ->
                    traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found")
                    .hasMessageContaining("NonExistent.Trainer");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when all trainers not found")
        void updateTrainersList_AllTrainersNotFound_ThrowsNotFoundException() {
            // Given
            List<String> trainerUsernames = List.of("NonExistent.One", "NonExistent.Two");

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.of(trainee));
            when(trainerRepository.findByUser_UsernameIn(trainerUsernames))
                    .thenReturn(List.of());

            // When & Then
            assertThatThrownBy(() ->
                    traineeService.updateTrainersList(USERNAME, trainerUsernames))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found");

            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void updateTrainersList_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() ->
                    traineeService.updateTrainersList(USERNAME, List.of("Trainer.One")))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeRepository, never()).findByUser_Username(anyString());
            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
            verify(traineeRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void updateTrainersList_TraineeNotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeRepository.findByUser_Username(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    traineeService.updateTrainersList(USERNAME, List.of("Trainer.One")))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(trainerRepository, never()).findByUser_UsernameIn(anyList());
            verify(traineeRepository, never()).save(any());
        }
    }
}