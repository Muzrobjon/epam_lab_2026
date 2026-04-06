package com.epam.gym.controller;

import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.dto.request.TraineeRegistrationRequest;
import com.epam.gym.dto.request.UpdateTraineeRequest;
import com.epam.gym.dto.request.UpdateTraineeTrainersRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.service.TraineeService;
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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TraineeController traineeController;

    private static final String USERNAME = "John.Doe";

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(USERNAME)
                .password("encodedPassword")
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

    // ==================== REGISTER TRAINEE TESTS ====================

    @Nested
    @DisplayName("Register Trainee Tests")
    class RegisterTraineeTests {

        @Test
        @DisplayName("Should register trainee successfully")
        void registerTrainee_Success() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setDateOfBirth(LocalDate.of(1995, 5, 15));
            request.setAddress("123 Main St");

            RegistrationResponse registrationResponse = new RegistrationResponse();
            registrationResponse.setUsername(USERNAME);
            registrationResponse.setPassword("rawPassword123");

            when(traineeService.createProfile(request)).thenReturn(registrationResponse);

            // When
            ResponseEntity<RegistrationResponse> response =
                    traineeController.registerTrainee(request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getBody().getPassword()).isEqualTo("rawPassword123");

            verify(traineeService).createProfile(request);
        }

        @Test
        @DisplayName("Should propagate exception when service fails")
        void registerTrainee_ServiceFails_PropagatesException() {
            // Given
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            request.setFirstName("John");
            request.setLastName("Doe");

            when(traineeService.createProfile(request))
                    .thenThrow(new ValidationException("Validation failed"));

            // When & Then
            assertThatThrownBy(() -> traineeController.registerTrainee(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed");
        }
    }

    // ==================== GET TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Get Trainee Profile Tests")
    class GetTraineeProfileTests {

        @Test
        @DisplayName("Should get trainee profile successfully")
        void getTraineeProfile_Success() {
            // Given
            TraineeProfileResponse profileResponse = TraineeProfileResponse.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .address("123 Main St")
                    .isActive(true)
                    .trainers(List.of())
                    .build();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeService.getByUsername(USERNAME)).thenReturn(trainee);
            when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

            // When
            ResponseEntity<TraineeProfileResponse> response =
                    traineeController.getTraineeProfile(USERNAME);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo("John");
            assertThat(response.getBody().getLastName()).isEqualTo("Doe");
            assertThat(response.getBody().getDateOfBirth()).isEqualTo(LocalDate.of(1995, 5, 15));
            assertThat(response.getBody().getAddress()).isEqualTo("123 Main St");
            assertThat(response.getBody().getIsActive()).isTrue();

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeService).getByUsername(USERNAME);
            verify(traineeMapper).toProfileResponse(trainee);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void getTraineeProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied: you can only modify your own resources"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeController.getTraineeProfile(USERNAME))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Access denied");

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeService, never()).getByUsername(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void getTraineeProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeService.getByUsername(USERNAME))
                    .thenThrow(new NotFoundException("Trainee not found: " + USERNAME));

            // When & Then
            assertThatThrownBy(() -> traineeController.getTraineeProfile(USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");

            verify(traineeMapper, never()).toProfileResponse(any());
        }
    }

    // ==================== UPDATE TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Trainee Profile Tests")
    class UpdateTraineeProfileTests {

        private UpdateTraineeRequest updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTraineeRequest();
            updateRequest.setUsername(USERNAME);
            updateRequest.setFirstName("John");
            updateRequest.setLastName("Updated");
            updateRequest.setDateOfBirth(LocalDate.of(1995, 5, 15));
            updateRequest.setAddress("456 New St");
            updateRequest.setIsActive(true);
        }

        @Test
        @DisplayName("Should update trainee profile successfully")
        void updateTraineeProfile_Success() {
            // Given
            Trainee updatedTrainee = Trainee.builder()
                    .id(1L)
                    .user(User.builder()
                            .id(1L)
                            .firstName("John")
                            .lastName("Updated")
                            .username(USERNAME)
                            .isActive(true)
                            .build())
                    .dateOfBirth(LocalDate.of(1995, 5, 15))
                    .address("456 New St")
                    .build();

            TraineeProfileResponse profileResponse = TraineeProfileResponse.builder()
                    .firstName("John")
                    .lastName("Updated")
                    .address("456 New St")
                    .isActive(true)
                    .build();

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            when(traineeService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainee);
            when(traineeMapper.toProfileResponse(updatedTrainee)).thenReturn(profileResponse);

            // When
            ResponseEntity<TraineeProfileResponse> response =
                    traineeController.updateTraineeProfile(USERNAME, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getLastName()).isEqualTo("Updated");
            assertThat(response.getBody().getAddress()).isEqualTo("456 New St");

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeService).updateProfile(USERNAME, updateRequest);
            verify(traineeMapper).toProfileResponse(updatedTrainee);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void updateTraineeProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeController.updateTraineeProfile(USERNAME, updateRequest))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeService, never()).updateProfile(anyString(), any());
        }
    }

    // ==================== DELETE TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Delete Trainee Profile Tests")
    class DeleteTraineeProfileTests {

        @Test
        @DisplayName("Should delete trainee profile successfully")
        void deleteTraineeProfile_Success() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            doNothing().when(traineeService).deleteByUsername(USERNAME);

            // When
            ResponseEntity<Void> response = traineeController.deleteTraineeProfile(USERNAME);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(traineeService).deleteByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void deleteTraineeProfile_NotOwner_ThrowsAuthException() {
            // Given
            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeController.deleteTraineeProfile(USERNAME))
                    .isInstanceOf(AuthenticationException.class);

            verify(traineeService, never()).deleteByUsername(anyString());
        }

        @Test
        @DisplayName("Should throw NotFoundException when trainee not found")
        void deleteTraineeProfile_NotFound_ThrowsNotFoundException() {
            // Given
            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            doThrow(new NotFoundException("Trainee not found: " + USERNAME))
                    .when(traineeService).deleteByUsername(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeController.deleteTraineeProfile(USERNAME))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainee not found");
        }
    }

    // ==================== TOGGLE TRAINEE STATUS TESTS ====================

    @Nested
    @DisplayName("Toggle Trainee Status Tests")
    class ToggleTraineeStatusTests {

        @Test
        @DisplayName("Should activate trainee successfully")
        void toggleTraineeStatus_Activate_Success() {
            // Given
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setIsActive(true);

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, true);

            // When
            ResponseEntity<Void> response =
                    traineeController.toggleTraineeStatus(USERNAME, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(userService).verifyResourceOwnership(USERNAME);
            verify(userService).setActiveStatus(USERNAME, true);
        }

        @Test
        @DisplayName("Should deactivate trainee successfully")
        void toggleTraineeStatus_Deactivate_Success() {
            // Given
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setIsActive(false);

            doNothing().when(userService).verifyResourceOwnership(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, false);

            // When
            ResponseEntity<Void> response =
                    traineeController.toggleTraineeStatus(USERNAME, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(userService).setActiveStatus(USERNAME, false);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void toggleTraineeStatus_NotOwner_ThrowsAuthException() {
            // Given
            ToggleActiveRequest request = new ToggleActiveRequest();
            request.setIsActive(true);

            doThrow(new AuthenticationException("Access denied"))
                    .when(userService).verifyResourceOwnership(USERNAME);

            // When & Then
            assertThatThrownBy(() -> traineeController.toggleTraineeStatus(USERNAME, request))
                    .isInstanceOf(AuthenticationException.class);

            verify(userService, never()).setActiveStatus(anyString(), any());
        }
    }

    // ==================== UPDATE TRAINEE TRAINERS LIST TESTS ====================

    @Nested
    @DisplayName("Update Trainee Trainers List Tests")
    class UpdateTraineeTrainersListTests {

        @Test
        @DisplayName("Should update trainers list successfully")
        void updateTraineeTrainersList_Success() {
            // Given
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
            request.setTraineeUsername(USERNAME);
            request.setTrainerUsernames(List.of("Trainer.One", "Trainer.Two"));

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

            TrainingType specialization = TrainingType.builder()
                    .trainingTypeName(TrainingTypeName.FITNESS)
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

            when(traineeService.updateTrainersList(USERNAME, List.of("Trainer.One", "Trainer.Two")))
                    .thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryResponses);

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    traineeController.updateTraineeTrainersList(USERNAME, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getUsername()).isEqualTo("Trainer.One");
            assertThat(response.getBody().get(1).getUsername()).isEqualTo("Trainer.Two");

            verify(traineeService).updateTrainersList(USERNAME, List.of("Trainer.One", "Trainer.Two"));
            verify(trainerMapper).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should throw ValidationException when path and body usernames don't match")
        void updateTraineeTrainersList_UsernameMismatch_ThrowsValidationException() {
            // Given
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
            request.setTraineeUsername("Different.User");
            request.setTrainerUsernames(List.of("Trainer.One"));

            // When & Then
            assertThatThrownBy(() ->
                    traineeController.updateTraineeTrainersList(USERNAME, request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Username in path does not match username in request body");

            verify(traineeService, never()).updateTrainersList(anyString(), anyList());
            verify(trainerMapper, never()).toSummaryResponseList(anyList());
        }

        @Test
        @DisplayName("Should update with empty trainers list")
        void updateTraineeTrainersList_EmptyList_Success() {
            // Given
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
            request.setTraineeUsername(USERNAME);
            request.setTrainerUsernames(List.of());

            List<Trainer> emptyTrainers = List.of();
            List<TrainerSummaryResponse> emptyResponses = List.of();

            when(traineeService.updateTrainersList(USERNAME, List.of())).thenReturn(emptyTrainers);
            when(trainerMapper.toSummaryResponseList(emptyTrainers)).thenReturn(emptyResponses);

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    traineeController.updateTraineeTrainersList(USERNAME, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should propagate NotFoundException when trainer not found")
        void updateTraineeTrainersList_TrainerNotFound_ThrowsNotFoundException() {
            // Given
            UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
            request.setTraineeUsername(USERNAME);
            request.setTrainerUsernames(List.of("NonExistent.Trainer"));

            when(traineeService.updateTrainersList(USERNAME, List.of("NonExistent.Trainer")))
                    .thenThrow(new NotFoundException("Trainers not found: [NonExistent.Trainer]"));

            // When & Then
            assertThatThrownBy(() ->
                    traineeController.updateTraineeTrainersList(USERNAME, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Trainers not found");
        }
    }

    // ==================== GET TRAINEE TRAININGS TESTS ====================

    @Nested
    @DisplayName("Get Trainee Trainings Tests")
    class GetTraineeTrainingsTests {

        @Test
        @DisplayName("Should get trainee trainings without filters")
        void getTraineeTrainings_NoFilters_Success() {
            // Given
            List<Training> trainings = List.of(
                    Training.builder()
                            .id(1L)
                            .trainingName("Morning Workout")
                            .trainingDate(LocalDate.of(2025, 1, 15))
                            .trainingDurationMinutes(60)
                            .build()
            );

            List<TrainingResponse> trainingResponses = List.of(
                    TrainingResponse.builder()
                            .trainingName("Morning Workout")
                            .trainingDate(LocalDate.of(2025, 1, 15))
                            .trainingDuration(60)
                            .build()
            );

            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, null, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    traineeController.getTraineeTrainings(
                            USERNAME, null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().getTrainingName()).isEqualTo("Morning Workout");

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, null, null, null, null);
            verify(trainingMapper).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should get trainee trainings with all filters")
        void getTraineeTrainings_WithAllFilters_Success() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 1, 1);
            LocalDate toDate = LocalDate.of(2025, 12, 31);
            String trainerName = "Trainer";
            TrainingTypeName trainingType = TrainingTypeName.FITNESS;

            List<Training> trainings = List.of();
            List<TrainingResponse> trainingResponses = List.of();

            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, fromDate, toDate, trainerName, trainingType))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    traineeController.getTraineeTrainings(
                            USERNAME, fromDate, toDate, trainerName, trainingType);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, fromDate, toDate, trainerName, trainingType);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTraineeTrainings_NoTrainings_ReturnsEmptyList() {
            // Given
            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, null, null, null, null))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(List.of())).thenReturn(List.of());

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    traineeController.getTraineeTrainings(
                            USERNAME, null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should pass correct parameters to service")
        void getTraineeTrainings_PassesCorrectParameters() {
            // Given
            LocalDate fromDate = LocalDate.of(2025, 3, 1);
            LocalDate toDate = LocalDate.of(2025, 3, 31);
            String trainerName = "Mike";
            TrainingTypeName trainingType = TrainingTypeName.YOGA;

            when(trainingService.getTraineeTrainingsByCriteria(
                    anyString(), any(), any(), anyString(), any()))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(anyList())).thenReturn(List.of());

            // When
            traineeController.getTraineeTrainings(
                    USERNAME, fromDate, toDate, trainerName, trainingType);

            // Then
            verify(trainingService).getTraineeTrainingsByCriteria(
                    eq(USERNAME),
                    eq(fromDate),
                    eq(toDate),
                    eq(trainerName),
                    eq(trainingType)
            );
        }

        @Test
        @DisplayName("Should get multiple trainings")
        void getTraineeTrainings_MultipleTrainings_Success() {
            // Given
            List<Training> trainings = List.of(
                    Training.builder().id(1L).trainingName("Workout 1").build(),
                    Training.builder().id(2L).trainingName("Workout 2").build(),
                    Training.builder().id(3L).trainingName("Workout 3").build()
            );

            List<TrainingResponse> trainingResponses = List.of(
                    TrainingResponse.builder().trainingName("Workout 1").build(),
                    TrainingResponse.builder().trainingName("Workout 2").build(),
                    TrainingResponse.builder().trainingName("Workout 3").build()
            );

            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, null, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response =
                    traineeController.getTraineeTrainings(
                            USERNAME, null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(3);
        }
    }
}