package com.epam.gym.service;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerService Tests")
class TrainerServiceTest {

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

    @Captor
    private ArgumentCaptor<Trainer> trainerCaptor;

    private User testUser;
    private Trainer testTrainer;
    private TrainingType fitnessType;
    private TrainingType yogaType;
    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Mike")
                .lastName("Trainer")
                .username("Mike.Trainer")
                .password("password123")
                .isActive(true)
                .build();

        fitnessType = new TrainingType();
        fitnessType.setId(1L);
        fitnessType.setTrainingTypeName(TrainingTypeName.FITNESS);

        yogaType = new TrainingType();
        yogaType.setId(2L);
        yogaType.setTrainingTypeName(TrainingTypeName.YOGA);

        testTrainer = Trainer.builder()
                .id(1L)
                .user(testUser)
                .specialization(fitnessType)
                .trainees(new ArrayList<>())
                .build();

        User traineeUser = User.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("pass123")
                .isActive(true)
                .build();

        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .trainers(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("createProfile Tests")
    class CreateProfileTests {

        @Test
        @DisplayName("Should create trainer profile successfully")
        void createProfile_Success() {
            // Given
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(fitnessType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            Trainer result = trainerService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUser().getUsername()).isEqualTo("Mike.Trainer");
            assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);

            verify(userService).createUser("Mike", "Trainer");
            verify(trainingTypeRepository).findByTrainingTypeName(TrainingTypeName.FITNESS);
            verify(validator).validate(any(Trainer.class));
            verify(trainerRepository).save(trainerCaptor.capture());

            Trainer capturedTrainer = trainerCaptor.getValue();
            assertThat(capturedTrainer.getUser()).isEqualTo(testUser);
            assertThat(capturedTrainer.getSpecialization()).isEqualTo(fitnessType);
        }

        @Test
        @DisplayName("Should create trainer with YOGA specialization")
        void createProfile_WithYogaSpecialization() {
            // Given
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Jane")
                    .lastName("Yoga")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            User yogaUser = User.builder()
                    .id(3L)
                    .firstName("Jane")
                    .lastName("Yoga")
                    .username("Jane.Yoga")
                    .password("pass123")
                    .isActive(true)
                    .build();

            Trainer yogaTrainer = Trainer.builder()
                    .id(2L)
                    .user(yogaUser)
                    .specialization(yogaType)
                    .build();

            when(userService.createUser("Jane", "Yoga")).thenReturn(yogaUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.YOGA))
                    .thenReturn(Optional.of(yogaType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(yogaTrainer);

            // When
            Trainer result = trainerService.createProfile(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should throw NotFoundException when training type not found")
        void createProfile_TrainingTypeNotFound() {
            // Given
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.CROSSFIT)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.CROSSFIT))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found")
                    .hasMessageContaining("CROSSFIT");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void createProfile_ValidationFails() {
            // Given
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(fitnessType));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("User must not be null");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("User must not be null");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException with multiple errors")
        void createProfile_MultipleValidationErrors() {
            // Given
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(fitnessType));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation1 = org.mockito.Mockito.mock(ConstraintViolation.class);
            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation2 = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("Error 1");
            when(violation2.getMessage()).thenReturn("Error 2");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.createProfile(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");

            verify(trainerRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getByUsername Tests")
    class GetByUsernameTests {

        @Test
        @DisplayName("Should return trainer when found")
        void getByUsername_Success() {
            // Given
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("Mike.Trainer");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUser().getUsername()).isEqualTo("Mike.Trainer");
            assertThat(result.getSpecialization().getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
            verify(trainerRepository).findByUser_Username("Mike.Trainer");
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void getByUsername_NotFound() {
            // Given
            when(trainerRepository.findByUser_Username("Unknown.Trainer"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getByUsername("Unknown.Trainer"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainer not found: Unknown.Trainer");
        }

        @Test
        @DisplayName("Should return trainer with all details")
        void getByUsername_WithAllDetails() {
            // Given
            testTrainer.getTrainees().add(testTrainee);
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("Mike.Trainer");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTrainees()).hasSize(1);
            assertThat(result.getUser().getFirstName()).isEqualTo("Mike");
            assertThat(result.getUser().getLastName()).isEqualTo("Trainer");
        }
    }

    @Nested
    @DisplayName("updateProfile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateProfile_Success() {
            // Given
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("Mike.Trainer")
                    .firstName("Michael")
                    .lastName("Updated")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            Trainer result = trainerService.updateProfile("Mike.Trainer", request);

            // Then
            assertThat(result).isNotNull();
            verify(userService).isAuthenticated("Mike.Trainer");
            verify(userService).updateUserBasicInfo(testUser, "Michael", "Updated", true);
            verify(trainerRepository).save(testTrainer);
        }

        @Test
        @DisplayName("Should update trainer and deactivate")
        void updateProfile_Deactivate() {
            // Given
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("Mike.Trainer")
                    .firstName("Mike")
                    .lastName("Trainer")
                    .isActive(false)
                    .build();

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            trainerService.updateProfile("Mike.Trainer", request);

            // Then
            verify(userService).updateUserBasicInfo(testUser, "Mike", "Trainer", false);
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void updateProfile_TrainerNotFound() {
            // Given
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("Unknown.Trainer")
                    .firstName("Unknown")
                    .lastName("User")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("Unknown.Trainer");
            when(trainerRepository.findByUser_Username("Unknown.Trainer"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile("Unknown.Trainer", request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainer not found: Unknown.Trainer");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void updateProfile_ValidationFails() {
            // Given
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("Mike.Trainer")
                    .firstName("")
                    .lastName("")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            @SuppressWarnings("unchecked")
            ConstraintViolation<Trainer> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("First name is required");

            Set<ConstraintViolation<Trainer>> violations = new HashSet<>();
            violations.add(violation);
            when(validator.validate(any(Trainer.class))).thenReturn(violations);

            // When & Then
            assertThatThrownBy(() -> trainerService.updateProfile("Mike.Trainer", request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("First name is required");

            verify(trainerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should call authentication check before update")
        void updateProfile_AuthenticationFirst() {
            // Given
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("Mike.Trainer")
                    .firstName("Mike")
                    .lastName("Trainer")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When
            trainerService.updateProfile("Mike.Trainer", request);

            // Then
            verify(userService).isAuthenticated("Mike.Trainer");
        }
    }

    @Nested
    @DisplayName("getUnassignedTrainers Tests")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should return unassigned trainers")
        void getUnassignedTrainers_Success() {
            // Given
            User anotherTrainerUser = User.builder()
                    .id(3L)
                    .firstName("Jane")
                    .lastName("Coach")
                    .username("Jane.Coach")
                    .password("pass")
                    .isActive(true)
                    .build();

            Trainer anotherTrainer = Trainer.builder()
                    .id(2L)
                    .user(anotherTrainerUser)
                    .specialization(yogaType)
                    .build();

            List<Trainer> unassignedTrainers = List.of(testTrainer, anotherTrainer);

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("John.Doe"))
                    .thenReturn(unassignedTrainers);

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("John.Doe");

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(testTrainer, anotherTrainer);
            verify(userService).isAuthenticated("John.Doe");
            verify(traineeRepository).findByUser_Username("John.Doe");
            verify(trainerRepository).findAvailableTrainers("John.Doe");
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_EmptyList() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("John.Doe"))
                    .thenReturn(Collections.emptyList());

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("John.Doe");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getUnassignedTrainers_TraineeNotFound() {
            // Given
            doNothing().when(userService).isAuthenticated("Unknown.Trainee");
            when(traineeRepository.findByUser_Username("Unknown.Trainee"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getUnassignedTrainers("Unknown.Trainee"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Trainee not found: Unknown.Trainee");

            verify(trainerRepository, never()).findAvailableTrainers(any());
        }

        @Test
        @DisplayName("Should call authentication before fetching")
        void getUnassignedTrainers_AuthenticationFirst() {
            // Given
            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("John.Doe"))
                    .thenReturn(Collections.emptyList());

            // When
            trainerService.getUnassignedTrainers("John.Doe");

            // Then
            verify(userService).isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("Should return only active unassigned trainers")
        void getUnassignedTrainers_OnlyActive() {
            // Given
            User activeTrainerUser = User.builder()
                    .id(3L)
                    .firstName("Active")
                    .lastName("Trainer")
                    .username("Active.Trainer")
                    .password("pass")
                    .isActive(true)
                    .build();

            Trainer activeTrainer = Trainer.builder()
                    .id(2L)
                    .user(activeTrainerUser)
                    .specialization(yogaType)
                    .build();

            doNothing().when(userService).isAuthenticated("John.Doe");
            when(traineeRepository.findByUser_Username("John.Doe"))
                    .thenReturn(Optional.of(testTrainee));
            when(trainerRepository.findAvailableTrainers("John.Doe"))
                    .thenReturn(List.of(activeTrainer));

            // When
            List<Trainer> result = trainerService.getUnassignedTrainers("John.Doe");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getUser().getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle trainer with no trainees")
        void getByUsername_TrainerWithNoTrainees() {
            // Given
            testTrainer.setTrainees(new ArrayList<>());
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("Mike.Trainer");

            // Then
            assertThat(result.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should handle trainer with multiple trainees")
        void getByUsername_TrainerWithMultipleTrainees() {
            // Given
            User traineeUser2 = User.builder()
                    .id(4L)
                    .firstName("Jane")
                    .lastName("Student")
                    .username("Jane.Student")
                    .build();

            Trainee trainee2 = Trainee.builder()
                    .id(2L)
                    .user(traineeUser2)
                    .build();

            testTrainer.setTrainees(List.of(testTrainee, trainee2));
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When
            Trainer result = trainerService.getByUsername("Mike.Trainer");

            // Then
            assertThat(result.getTrainees()).hasSize(2);
        }

        @Test
        @DisplayName("Should handle special characters in username")
        void getByUsername_SpecialCharacters() {
            // Given
            when(trainerRepository.findByUser_Username("Mike.O'Connor"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> trainerService.getByUsername("Mike.O'Connor"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should handle null specialization in request gracefully")
        void createProfile_AllTrainingTypes() {
            // Test all training types
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                TrainingType type = new TrainingType();
                type.setId((long) typeName.ordinal() + 1);
                type.setTrainingTypeName(typeName);

                TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                        .firstName("Test")
                        .lastName("Trainer")
                        .specialization(typeName)
                        .build();

                when(userService.createUser("Test", "Trainer")).thenReturn(testUser);
                when(trainingTypeRepository.findByTrainingTypeName(typeName))
                        .thenReturn(Optional.of(type));
                when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
                when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> {
                    Trainer t = inv.getArgument(0);
                    t.setId(1L);
                    return t;
                });

                // Should not throw
                Trainer result = trainerService.createProfile(request);
                assertThat(result).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("Integration Scenarios Tests")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("Should create and then fetch trainer")
        void createAndFetchTrainer() {
            // Given - Create
            TrainerRegistrationRequest createRequest = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(fitnessType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When - Create
            Trainer created = trainerService.createProfile(createRequest);

            // Given - Fetch
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When - Fetch
            Trainer fetched = trainerService.getByUsername("Mike.Trainer");

            // Then
            assertThat(created.getId()).isEqualTo(fetched.getId());
            assertThat(created.getUser().getUsername()).isEqualTo(fetched.getUser().getUsername());
        }

        @Test
        @DisplayName("Should update trainer after creation")
        void createAndUpdateTrainer() {
            // Given - Create
            TrainerRegistrationRequest createRequest = TrainerRegistrationRequest.builder()
                    .firstName("Mike")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            when(userService.createUser("Mike", "Trainer")).thenReturn(testUser);
            when(trainingTypeRepository.findByTrainingTypeName(TrainingTypeName.FITNESS))
                    .thenReturn(Optional.of(fitnessType));
            when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
            when(trainerRepository.save(any(Trainer.class))).thenReturn(testTrainer);

            // When - Create
            trainerService.createProfile(createRequest);

            // Given - Update
            UpdateTrainerRequest updateRequest = UpdateTrainerRequest.builder()
                    .username("Mike.Trainer")
                    .firstName("Michael")
                    .lastName("Updated")
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated("Mike.Trainer");
            when(trainerRepository.findByUser_Username("Mike.Trainer"))
                    .thenReturn(Optional.of(testTrainer));

            // When - Update
            Trainer updated = trainerService.updateProfile("Mike.Trainer", updateRequest);

            // Then
            assertThat(updated).isNotNull();
            verify(userService).updateUserBasicInfo(testUser, "Michael", "Updated", true);
        }
    }
}