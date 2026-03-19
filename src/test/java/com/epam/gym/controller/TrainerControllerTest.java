package com.epam.gym.controller;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController Unit Tests")
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
    private UserMapper userMapper;

    @InjectMocks
    private TrainerController trainerController;

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "securePassword123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final Long TRAINER_ID = 1L;
    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("Register Trainer Tests")
    class RegisterTrainerTests {

        private TrainerRegistrationRequest registrationRequest;
        private Trainer trainer;
        private RegistrationResponse registrationResponse;

        @BeforeEach
        void setUp() {
            registrationRequest = createRegistrationRequest();
            trainer = createTrainerWithUser();
            registrationResponse = createRegistrationResponse();
        }

        @Test
        @DisplayName("Should register trainer successfully and return CREATED status")
        void registerTrainer_ValidRequest_ReturnsCreatedStatus() {
            when(trainerService.createProfile(registrationRequest)).thenReturn(trainer);
            when(userMapper.toRegistrationResponse(trainer)).thenReturn(registrationResponse);

            ResponseEntity<RegistrationResponse> response = trainerController.registerTrainer(registrationRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getBody().getPassword()).isEqualTo(PASSWORD);

            verify(trainerService).createProfile(registrationRequest);
            verify(userMapper).toRegistrationResponse(trainer);
            verifyNoMoreInteractions(trainerService, userMapper);
        }

        @Test
        @DisplayName("Should pass correct request to service")
        void registerTrainer_ValidRequest_PassesCorrectRequestToService() {
            when(trainerService.createProfile(any(TrainerRegistrationRequest.class))).thenReturn(trainer);
            when(userMapper.toRegistrationResponse(any(Trainer.class))).thenReturn(registrationResponse);

            trainerController.registerTrainer(registrationRequest);

            verify(trainerService).createProfile(registrationRequest);
        }

        @Test
        @DisplayName("Should return response with generated username and password")
        void registerTrainer_ValidRequest_ReturnsGeneratedCredentials() {
            String generatedUsername = "john.doe1";
            String generatedPassword = "randomPass123";

            RegistrationResponse expectedResponse = RegistrationResponse.builder()
                    .username(generatedUsername)
                    .password(generatedPassword)
                    .build();

            when(trainerService.createProfile(registrationRequest)).thenReturn(trainer);
            when(userMapper.toRegistrationResponse(trainer)).thenReturn(expectedResponse);

            ResponseEntity<RegistrationResponse> response = trainerController.registerTrainer(registrationRequest);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(generatedUsername);
            assertThat(response.getBody().getPassword()).isEqualTo(generatedPassword);
        }
    }

    @Nested
    @DisplayName("Get Trainer Profile Tests")
    class GetTrainerProfileTests {

        private Trainer trainer;
        private TrainerProfileResponse profileResponse;

        @BeforeEach
        void setUp() {
            trainer = createTrainerWithUser();
            profileResponse = createProfileResponse();
        }

        @Test
        @DisplayName("Should get trainer profile successfully")
        void getTrainerProfile_ValidCredentials_ReturnsProfile() {
            doNothing().when(trainerService).authenticate(USERNAME, PASSWORD);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(USERNAME, PASSWORD);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getBody().getLastName()).isEqualTo(LAST_NAME);
            assertThat(response.getBody().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getBody().getIsActive()).isTrue();

            verify(trainerService).authenticate(USERNAME, PASSWORD);
            verify(trainerService).getByUsername(USERNAME);
            verify(trainerMapper).toProfileResponse(trainer);
        }

        @Test
        @DisplayName("Should authenticate before fetching profile")
        void getTrainerProfile_ValidCredentials_AuthenticatesFirst() {
            doNothing().when(trainerService).authenticate(USERNAME, PASSWORD);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

            trainerController.getTrainerProfile(USERNAME, PASSWORD);

            verify(trainerService).authenticate(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("Should return profile with trainees list")
        void getTrainerProfile_TrainerWithTrainees_ReturnsProfileWithTrainees() {
            TraineeSummaryResponse trainee = TraineeSummaryResponse.builder()
                    .username("trainee1")
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            TrainerProfileResponse responseWithTrainees = TrainerProfileResponse.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of(trainee))
                    .build();

            doNothing().when(trainerService).authenticate(USERNAME, PASSWORD);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(responseWithTrainees);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(USERNAME, PASSWORD);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainees()).hasSize(1);
            assertThat(response.getBody().getTrainees().getFirst().getUsername()).isEqualTo("trainee1");
            assertThat(response.getBody().getTrainees().getFirst().getFirstName()).isEqualTo("Jane");
            assertThat(response.getBody().getTrainees().getFirst().getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should return profile with empty trainees list")
        void getTrainerProfile_NoTrainees_ReturnsEmptyTraineesList() {
            doNothing().when(trainerService).authenticate(USERNAME, PASSWORD);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(USERNAME, PASSWORD);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should return profile with multiple trainees")
        void getTrainerProfile_MultipleTrainees_ReturnsAllTrainees() {
            List<TraineeSummaryResponse> trainees = List.of(
                    TraineeSummaryResponse.builder()
                            .username("trainee1")
                            .firstName("Jane")
                            .lastName("Smith")
                            .build(),
                    TraineeSummaryResponse.builder()
                            .username("trainee2")
                            .firstName("Bob")
                            .lastName("Wilson")
                            .build()
            );

            TrainerProfileResponse responseWithTrainees = TrainerProfileResponse.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(trainees)
                    .build();

            doNothing().when(trainerService).authenticate(USERNAME, PASSWORD);
            when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
            when(trainerMapper.toProfileResponse(trainer)).thenReturn(responseWithTrainees);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(USERNAME, PASSWORD);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTrainees()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Update Trainer Profile Tests")
    class UpdateTrainerProfileTests {

        private UpdateTrainerRequest updateRequest;
        private Trainer updatedTrainer;
        private TrainerProfileResponse profileResponse;

        @BeforeEach
        void setUp() {
            updateRequest = createUpdateRequest(USERNAME);
            updatedTrainer = createTrainerWithUser();
            profileResponse = createProfileResponse();
        }

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateTrainerProfile_ValidRequest_ReturnsUpdatedProfile() {
            when(trainerService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(profileResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(USERNAME, updateRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getBody().getLastName()).isEqualTo(LAST_NAME);

            verify(trainerService).updateProfile(USERNAME, updateRequest);
            verify(trainerMapper).toProfileResponse(updatedTrainer);
        }

        @Test
        @DisplayName("Should throw ValidationException when path username doesn't match request username")
        void updateTrainerProfile_UsernameMismatch_ThrowsValidationException() {
            String pathUsername = "john.doe";
            UpdateTrainerRequest requestWithDifferentUsername = createUpdateRequest("different.user");

            assertThatThrownBy(() ->
                    trainerController.updateTrainerProfile(pathUsername, requestWithDifferentUsername))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Username in path does not match username in request body");

            verify(trainerService, never()).updateProfile(any(), any());
            verifyNoInteractions(trainerMapper);
        }

        @Test
        @DisplayName("Should not call service when validation fails")
        void updateTrainerProfile_ValidationFails_ServiceNotCalled() {
            UpdateTrainerRequest mismatchedRequest = createUpdateRequest("other.user");

            assertThatThrownBy(() ->
                    trainerController.updateTrainerProfile(USERNAME, mismatchedRequest))
                    .isInstanceOf(ValidationException.class);

            verifyNoInteractions(trainerMapper);
        }

        @Test
        @DisplayName("Should update trainer active status to false")
        void updateTrainerProfile_ChangeActiveStatusToFalse_ReturnsUpdatedProfile() {
            updateRequest.setIsActive(false);

            TrainerProfileResponse inactiveResponse = TrainerProfileResponse.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(false)
                    .trainees(Collections.emptyList())
                    .build();

            when(trainerService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(inactiveResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(USERNAME, updateRequest);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update trainer first name")
        void updateTrainerProfile_ChangeFirstName_ReturnsUpdatedProfile() {
            String newFirstName = "Johnny";
            updateRequest.setFirstName(newFirstName);

            TrainerProfileResponse updatedResponse = TrainerProfileResponse.builder()
                    .username(USERNAME)
                    .firstName(newFirstName)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            when(trainerService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(updatedResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(USERNAME, updateRequest);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo(newFirstName);
        }

        @Test
        @DisplayName("Should update trainer last name")
        void updateTrainerProfile_ChangeLastName_ReturnsUpdatedProfile() {
            String newLastName = "Smith";
            updateRequest.setLastName(newLastName);

            TrainerProfileResponse updatedResponse = TrainerProfileResponse.builder()
                    .username(USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(newLastName)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            when(trainerService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(updatedResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(USERNAME, updateRequest);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getLastName()).isEqualTo(newLastName);
        }
    }

    @Nested
    @DisplayName("Get Unassigned Trainers Tests")
    class GetUnassignedTrainersTests {

        private static final String TRAINEE_USERNAME = "trainee.user";
        private static final String TRAINEE_PASSWORD = "traineePass123";

        @Test
        @DisplayName("Should get unassigned trainers successfully")
        void getUnassignedTrainers_ValidRequest_ReturnsTrainersList() {
            List<Trainer> trainers = List.of(
                    createTrainerWithUsername("trainer1", "Alice", "Johnson", TrainingTypeName.FITNESS),
                    createTrainerWithUsername("trainer2", "Bob", "Williams", TrainingTypeName.YOGA)
            );

            List<TrainerSummaryResponse> summaryResponses = List.of(
                    createSummaryResponse("trainer1", "Alice", "Johnson", TrainingTypeName.FITNESS),
                    createSummaryResponse("trainer2", "Bob", "Williams", TrainingTypeName.YOGA)
            );

            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD))
                    .thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryResponses);

            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getUsername()).isEqualTo("trainer1");
            assertThat(response.getBody().get(1).getUsername()).isEqualTo("trainer2");

            verify(trainerService).getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD);
            verify(trainerMapper).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers available")
        void getUnassignedTrainers_NoTrainersAvailable_ReturnsEmptyList() {
            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD))
                    .thenReturn(Collections.emptyList());
            when(trainerMapper.toSummaryResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should return trainers with different specializations")
        void getUnassignedTrainers_MultipleSpecializations_ReturnsAllTrainers() {
            List<Trainer> trainers = List.of(
                    createTrainerWithUsername("trainer1", "Alice", "Johnson", TrainingTypeName.FITNESS),
                    createTrainerWithUsername("trainer2", "Bob", "Williams", TrainingTypeName.YOGA),
                    createTrainerWithUsername("trainer3", "Charlie", "Brown", TrainingTypeName.CARDIO)
            );

            List<TrainerSummaryResponse> summaryResponses = List.of(
                    createSummaryResponse("trainer1", "Alice", "Johnson", TrainingTypeName.FITNESS),
                    createSummaryResponse("trainer2", "Bob", "Williams", TrainingTypeName.YOGA),
                    createSummaryResponse("trainer3", "Charlie", "Brown", TrainingTypeName.CARDIO)
            );

            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD))
                    .thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryResponses);

            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(TRAINEE_USERNAME, TRAINEE_PASSWORD);

            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody())
                    .extracting(TrainerSummaryResponse::getSpecialization)
                    .containsExactly(TrainingTypeName.FITNESS, TrainingTypeName.YOGA, TrainingTypeName.CARDIO);
        }
    }

    @Nested
    @DisplayName("Get Trainer Trainings Tests")
    class GetTrainerTrainingsTests {

        private LocalDate fromDate;
        private LocalDate toDate;
        private String traineeName;

        @BeforeEach
        void setUp() {
            fromDate = LocalDate.of(2024, 1, 1);
            toDate = LocalDate.of(2024, 12, 31);
            traineeName = "Jane Doe";
        }

        @Test
        @DisplayName("Should get trainer trainings with all filters")
        void getTrainerTrainings_AllFilters_ReturnsFilteredTrainings() {
            List<Training> trainings = List.of(
                    createTraining(1L, "Morning Workout", LocalDate.of(2024, 6, 15), 60),
                    createTraining(2L, "Evening Session", LocalDate.of(2024, 6, 16), 45)
            );

            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Morning Workout", LocalDate.of(2024, 6, 15), 60, TrainingTypeName.FITNESS),
                    createTrainingResponse("Evening Session", LocalDate.of(2024, 6, 16), 45, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, traineeName))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, fromDate, toDate, traineeName);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getTrainingName()).isEqualTo("Morning Workout");
            assertThat(response.getBody().get(1).getTrainingName()).isEqualTo("Evening Session");

            verify(trainingService).getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, traineeName);
            verify(trainingMapper).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should get trainer trainings without optional filters")
        void getTrainerTrainings_NoFilters_ReturnsAllTrainings() {
            List<Training> trainings = List.of(createTraining(1L, "Training 1", LocalDate.now(), 60));
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Training 1", LocalDate.now(), 60, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);

            verify(trainingService).getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTrainerTrainings_NoTrainingsFound_ReturnsEmptyList() {
            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, traineeName))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, fromDate, toDate, traineeName);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should get trainings with only fromDate filter")
        void getTrainerTrainings_OnlyFromDate_ReturnsFilteredTrainings() {
            List<Training> trainings = List.of(createTraining(1L, "Training", LocalDate.of(2024, 6, 15), 60));
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Training", LocalDate.of(2024, 6, 15), 60, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(USERNAME), eq(PASSWORD), eq(fromDate), eq(null), eq(null)))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, fromDate, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);

            verify(trainingService).getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, null, null);
        }

        @Test
        @DisplayName("Should get trainings with only toDate filter")
        void getTrainerTrainings_OnlyToDate_ReturnsFilteredTrainings() {
            List<Training> trainings = List.of(createTraining(1L, "Training", LocalDate.of(2024, 6, 15), 60));
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Training", LocalDate.of(2024, 6, 15), 60, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(USERNAME), eq(PASSWORD), eq(null), eq(toDate), eq(null)))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, null, toDate, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Should get trainings with only traineeName filter")
        void getTrainerTrainings_OnlyTraineeName_ReturnsFilteredTrainings() {
            List<Training> trainings = List.of(createTraining(1L, "Training", LocalDate.of(2024, 6, 15), 60));
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Training", LocalDate.of(2024, 6, 15), 60, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(USERNAME), eq(PASSWORD), eq(null), eq(null), eq(traineeName)))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, null, null, traineeName);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
        }

        @Test
        @DisplayName("Should return trainings with correct training type")
        void getTrainerTrainings_WithTrainingType_ReturnsCorrectType() {
            List<Training> trainings = List.of(createTraining(1L, "Yoga Session", LocalDate.now(), 60));
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("Yoga Session", LocalDate.now(), 60, TrainingTypeName.YOGA)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, null, null, null);

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirst().getTrainingType()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should get trainings with date range filter")
        void getTrainerTrainings_DateRange_ReturnsFilteredTrainings() {
            LocalDate startDate = LocalDate.of(2024, 6, 1);
            LocalDate endDate = LocalDate.of(2024, 6, 30);

            List<Training> trainings = List.of(
                    createTraining(1L, "June Training", LocalDate.of(2024, 6, 15), 60)
            );
            List<TrainingResponse> trainingResponses = List.of(
                    createTrainingResponse("June Training", LocalDate.of(2024, 6, 15), 60, TrainingTypeName.FITNESS)
            );

            when(trainingService.getTrainerTrainingsByCriteria(
                    USERNAME, PASSWORD, startDate, endDate, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(USERNAME, PASSWORD, startDate, endDate, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().getTrainingDate()).isEqualTo(LocalDate.of(2024, 6, 15));
        }
    }

    // ==================== Helper Methods ====================

    private TrainerRegistrationRequest createRegistrationRequest() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(FIRST_NAME);
        request.setLastName(LAST_NAME);
        // Set specialization if your TrainerRegistrationRequest has it
        // request.setSpecialization(...);
        return request;
    }

    private Trainer createTrainerWithUser() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .build();

        TrainingType trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(trainingType)
                .trainings(new ArrayList<>())
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithUsername(String username, String firstName, String lastName,
                                              TrainingTypeName specialization) {
        User user = User.builder()
                .id(System.currentTimeMillis())
                .username(username)
                .password(PASSWORD)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        TrainingType trainingType = new TrainingType(1L, specialization);

        return Trainer.builder()
                .id(System.currentTimeMillis())
                .user(user)
                .specialization(trainingType)
                .trainings(new ArrayList<>())
                .trainees(new ArrayList<>())
                .build();
    }

    private RegistrationResponse createRegistrationResponse() {
        return RegistrationResponse.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    private TrainerProfileResponse createProfileResponse() {
        return TrainerProfileResponse.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .specialization(TrainingTypeName.FITNESS)
                .isActive(true)
                .trainees(Collections.emptyList())
                .build();
    }

    private UpdateTrainerRequest createUpdateRequest(String username) {
        return UpdateTrainerRequest.builder()
                .username(username)
                .password(TrainerControllerTest.PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(true)
                .build();
    }

    private TrainerSummaryResponse createSummaryResponse(String username, String firstName,
                                                         String lastName, TrainingTypeName specialization) {
        return TrainerSummaryResponse.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .specialization(specialization)
                .build();
    }

    private Training createTraining(Long id, String name, LocalDate date, Integer duration) {
        Trainer trainer = createTrainerWithUser();
        TrainingType trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        return Training.builder()
                .id(id)
                .trainer(trainer)
                .trainingName(name)
                .trainingType(trainingType)
                .trainingDate(date)
                .trainingDurationMinutes(duration)
                .build();
    }

    private TrainingResponse createTrainingResponse(String name, LocalDate date,
                                                    Integer duration, TrainingTypeName type) {
        return TrainingResponse.builder()
                .trainingName(name)
                .trainingDate(date)
                .trainingType(type)
                .trainingDuration(duration)
                .trainerName(FIRST_NAME + " " + LAST_NAME)
                .traineeName("Jane Doe")
                .build();
    }
}