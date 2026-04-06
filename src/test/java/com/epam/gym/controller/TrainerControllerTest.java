package com.epam.gym.controller;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerController trainerController;

    private static final String USERNAME = "Mike.Tyson";
    private static final String TRAINEE_USERNAME = "John.Doe";

    private Trainer trainer;
    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .firstName("Mike")
                .lastName("Tyson")
                .username(USERNAME)
                .password("encodedPassword")
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

    // ==================== REGISTER TRAINER TESTS ====================

    @Nested
    @DisplayName("Register Trainer Tests")
    class RegisterTrainerTests {

        @Test
        @DisplayName("Should register trainer successfully")
        void registerTrainer_Success() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Tyson");
            request.setSpecialization(TrainingTypeName.FITNESS);

            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setUsername(USERNAME);
            registrationResponse.setPassword("rawPassword123");

            when(trainerService.createProfile(request)).thenReturn(registrationResponse);

            // When
            ResponseEntity<RegistrationResponse> response =
                    trainerController.registerTrainer(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getBody().getPassword()).isEqualTo("rawPassword123");

            verify(trainerService).createProfile(request);
        }

        @Test
        @DisplayName("Should propagate exception when service fails")
        void registerTrainer_ServiceFails_PropagatesException() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Tyson");
            request.setSpecialization(TrainingTypeName.FITNESS);

            when(trainerService.createProfile(request))
                    .thenThrow(new NotFoundException("Training type not found: FITNESS"));

            // When & Then
            assertThatThrownBy(() -> trainerController.registerTrainer(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Training type not found");
        }

        @Test
        @DisplayName("Should propagate validation exception")
        void registerTrainer_ValidationFails_PropagatesException() {
            // Given
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            request.setFirstName("Mike");
            request.setLastName("Tyson");
            request.setSpecialization(TrainingTypeName.FITNESS);

            when(trainerService.createProfile(request))
                    .thenThrow(new ValidationException("Validation failed"));

            // When & Then
            assertThatThrownBy(() -> trainerController.registerTrainer(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");
        }
    }

    // ==================== GET TRAINER PROFILE TESTS ====================

    @Nested
    @DisplayName("Get Trainer Profile Tests")
    class GetTrainerProfileTests {

        @Test
        @DisplayName("Should get trainer profile successfully")
        void getTrainerProfile_Success() {
            // Given
            TrainerProfileResponse profileResponse = TrainerProfileResponse.builder()
                    .firstName("Mike")
                    .lastName("Tyson")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of())
                    .build();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

            // When
            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(USERNAME);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo("Mike");
            assertThat(response.getBody().getLastName()).isEqualTo("Tyson");
            assertThat(response.getBody().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getBody().getIsActive()).isTrue();
            assertThat(response.getBody().getTrainees()).isEmpty();

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(trainerService).getByUsername(USERNAME);
            verify(trainerMapper).toProfileResponse(trainer);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void getTrainerProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied: you can only modify your own resources"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainerController.getTrainerProfile(USERNAME))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Access denied");

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(trainerService, never()).getByUsername(anyString());
            verify(trainerMapper, never()).toProfileResponse(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void getTrainerProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerService.getByUsername(USERNAME))
                    .thenThrow(new NotFoundException("Trainer not found: " + USERNAME));

            // When & Then
            assertThatThrownBy(() -> trainerController.getTrainerProfile(USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found");

            verify(trainerMapper, never()).toProfileResponse(any());
        }
    }

    // ==================== UPDATE TRAINER PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Trainer Profile Tests")
    class UpdateTrainerProfileTests {

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
        void updateTrainerProfile_Success() {
            // Given
            Trainer updatedTrainer = Trainer.builder()
                    .id(1L)
                    .user(User.builder()
                            .id(1L)
                            .firstName("Mike")
                            .lastName("Updated")
                            .username(USERNAME)
                            .isActive(true)
                            .build())
                    .specialization(specialization)
                    .build();

            TrainerProfileResponse profileResponse = TrainerProfileResponse.builder()
                    .firstName("Mike")
                    .lastName("Updated")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of())
                    .build();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(profileResponse);

            // When
            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(USERNAME, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getLastName()).isEqualTo("Updated");
            assertThat(response.getBody().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(trainerService).updateProfile(USERNAME, updateRequest);
            verify(trainerMapper).toProfileResponse(updatedTrainer);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void updateTrainerProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> trainerController.updateTrainerProfile(USERNAME, updateRequest))
                    .isInstanceOf(AuthenticationException.class);

            verify(trainerService, never()).updateProfile(anyString(), any());
            verify(trainerMapper, never()).toProfileResponse(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainer not found")
        void updateTrainerProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(trainerService.updateProfile(USERNAME, updateRequest))
                    .thenThrow(new NotFoundException("Trainer not found: " + USERNAME));

            // When & Then
            assertThatThrownBy(() -> trainerController.updateTrainerProfile(USERNAME, updateRequest))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainer not found");

            verify(trainerMapper, never()).toProfileResponse(any());
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
                    .firstName("Trainer")
                    .lastName("One")
                    .username("Trainer.One")
                    .build();

            User trainerUser2 = User.builder()
                    .firstName("Trainer")
                    .lastName("Two")
                    .username("Trainer.Two")
                    .build();

            List<Trainer> trainers = List.of(
                    Trainer.builder().user(trainerUser1).specialization(specialization).build(),
                    Trainer.builder().user(trainerUser2).specialization(specialization).build()
            );

            List<TrainerSummaryResponse> summaryResponses = List.of(
                    TrainerSummaryResponse.builder()
                            .username("Trainer.One")
                            .firstName("Trainer")
                            .lastName("One")
                            .specialization(TrainingTypeName.FITNESS)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("Trainer.Two")
                            .firstName("Trainer")
                            .lastName("Two")
                            .specialization(TrainingTypeName.FITNESS)
                            .build()
            );

            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryResponses);

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getUsername()).isEqualTo("Trainer.One");
            assertThat(response.getBody().get(1).getUsername()).isEqualTo("Trainer.Two");

            verify(trainerService).getUnassignedTrainers(TRAINEE_USERNAME);
            verify(trainerMapper).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_NoTrainers_ReturnsEmptyList() {
            // Given
            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(List.of());
            when(trainerMapper.toSummaryResponseList(List.of())).thenReturn(List.of());

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getUnassignedTrainers_TraineeNotFound_ThrowsNotFoundException() {
            // Given
            when(trainerService.getUnassignedTrainers("NonExistent.User"))
                    .thenThrow(new NotFoundException("Trainee not found: NonExistent.User"));

            // When & Then
            assertThatThrownBy(() ->
                    trainerController.getUnassignedTrainers("NonExistent.User"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(trainerMapper, never()).toSummaryResponseList(anyList());
        }

        @Test
        @DisplayName("Should pass correct trainee username to service")
        void getUnassignedTrainers_PassesCorrectUsername() {
            // Given
            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(List.of());
            when(trainerMapper.toSummaryResponseList(List.of())).thenReturn(List.of());

            // When
            trainerController.getUnassignedTrainers(TRAINEE_USERNAME);

            // Then
            verify(trainerService).getUnassignedTrainers(eq(TRAINEE_USERNAME));
        }
    }

    // ==================== GET TRAINER TRAININGS TESTS ====================

    @Nested
    @DisplayName("Get Trainer Trainings Tests")
    class GetTrainerTrainingsTests {

        @Test
        @DisplayName("Should get trainer trainings without filters")
        void getTrainerTrainings_NoFilters_Success() {
            // Given
            List<Training> trainings = List.of(
                    Training.builder()
                            .id(1L)
                            .trainingName("Morning Session")
                            .trainingDate(LocalDate.of(2025, 1, 15))
                            .trainingDurationMinutes(60)
                            .build()
            );

            List<TrainingResponse> trainingResponses = List.of(
                    TrainingResponse.builder()
                            .trainingName("Morning Session")
                            .trainingDate(LocalDate.of(2025, 1, 15))
                            .trainingDuration(60)
                            .build()
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(
                            USERNAME, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().getTrainingName()).isEqualTo("Morning Session");

            verify(trainingService).getTrainerTrainingsByCriteria(
                    USERNAME, null, null, null);
            verify(trainingMapper).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should get trainer trainings with all filters")
        void getTrainerTrainings_WithAllFilters_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.of(2025, 12, 31);
            String traineeName = "John";

            List<Training> trainings = List.of(
                    Training.builder()
                            .id(1L)
                            .trainingName("Filtered Session")
                            .build()
            );

            List<TrainingResponse> trainingResponses = List.of(
                    TrainingResponse.builder()
                            .trainingName("Filtered Session")
                            .build()
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, fromDate, toDate, traineeName))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(
                            USERNAME, fromDate, toDate, traineeName);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);

            verify(trainingService).getTrainerTrainingsByCriteria(
                    USERNAME, fromDate, toDate, traineeName);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTrainerTrainings_NoTrainings_ReturnsEmptyList() {
            // Given
            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, null, null, null))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(List.of())).thenReturn(List.of());

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(
                            USERNAME, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should pass correct parameters to service")
        void getTrainerTrainings_PassesCorrectParameters() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 3, 1);
            LocalDate toDate = LocalDate.of(2025, 3, 31);
            String traineeName = "Jane";

            when(trainingService.getTrainerTrainingsByCriteria(
                    anyString(), any(), any(), anyString()))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(anyList())).thenReturn(List.of());

            // When
            trainerController.getTrainerTrainings(
                    USERNAME, fromDate, toDate, traineeName);

            // Then
            verify(trainingService).getTrainerTrainingsByCriteria(
                    eq(USERNAME),
                    eq(fromDate),
                    eq(toDate),
                    eq(traineeName)
            );
        }

        @Test
        @DisplayName("Should get multiple trainings")
        void getTrainerTrainings_MultipleTrainings_Success() {
            // Given
            List<Training> trainings = List.of(
                    Training.builder().id(1L).trainingName("Session 1").build(),
                    Training.builder().id(2L).trainingName("Session 2").build(),
                    Training.builder().id(3L).trainingName("Session 3").build()
            );

            List<TrainingResponse> trainingResponses = List.of(
                    TrainingResponse.builder().trainingName("Session 1").build(),
                    TrainingResponse.builder().trainingName("Session 2").build(),
                    TrainingResponse.builder().trainingName("Session 3").build()
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(
                            USERNAME, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody().get(0).getTrainingName()).isEqualTo("Session 1");
            assertThat(response.getBody().get(1).getTrainingName()).isEqualTo("Session 2");
            assertThat(response.getBody().get(2).getTrainingName()).isEqualTo("Session 3");
        }

        @Test
        @DisplayName("Should handle partial filters correctly")
        void getTrainerTrainings_PartialFilters_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 6, 1);

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, fromDate, null, null))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(List.of())).thenReturn(List.of());

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(
                            USERNAME, fromDate, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(trainingService).getTrainerTrainingsByCriteria(
                    eq(USERNAME), eq(fromDate), eq(null), eq(null));
        }
    }
}