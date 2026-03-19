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
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeController Tests")
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

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private TraineeController traineeController;

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @Nested
    @DisplayName("Register Trainee Tests")
    class RegisterTraineeTests {

        private TraineeRegistrationRequest registrationRequest;
        private Trainee trainee;
        private RegistrationResponse registrationResponse;

        @BeforeEach
        void setUp() {
            registrationRequest = new TraineeRegistrationRequest();
            registrationRequest.setFirstName(FIRST_NAME);
            registrationRequest.setLastName(LAST_NAME);
            registrationRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
            registrationRequest.setAddress("123 Main St");

            User user = new User();
            user.setUsername(USERNAME);
            user.setPassword(PASSWORD);
            user.setFirstName(FIRST_NAME);
            user.setLastName(LAST_NAME);

            trainee = new Trainee();
            trainee.setUser(user);

            registrationResponse = new RegistrationResponse();
            registrationResponse.setUsername(USERNAME);
            registrationResponse.setPassword(PASSWORD);
        }

        @Test
        @DisplayName("Should register trainee successfully")
        void registerTrainee_Success() {
            // Given
            when(traineeService.createProfile(registrationRequest)).thenReturn(trainee);
            when(userMapper.toRegistrationResponse(trainee)).thenReturn(registrationResponse);

            // When
            ResponseEntity<RegistrationResponse> response = traineeController.registerTrainee(registrationRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getBody().getPassword()).isEqualTo(PASSWORD);

            verify(traineeService).createProfile(registrationRequest);
            verify(userMapper).toRegistrationResponse(trainee);
        }

        @Test
        @DisplayName("Should call service with correct request")
        void registerTrainee_CallsServiceWithCorrectRequest() {
            // Given
            when(traineeService.createProfile(any(TraineeRegistrationRequest.class))).thenReturn(trainee);
            when(userMapper.toRegistrationResponse(any(Trainee.class))).thenReturn(registrationResponse);

            // When
            traineeController.registerTrainee(registrationRequest);

            // Then
            verify(traineeService).createProfile(registrationRequest);
        }
    }

    @Nested
    @DisplayName("Get Trainee Profile Tests")
    class GetTraineeProfileTests {

        private Trainee trainee;
        private TraineeProfileResponse profileResponse;

        @BeforeEach
        void setUp() {
            User user = new User();
            user.setUsername(USERNAME);
            user.setFirstName(FIRST_NAME);
            user.setLastName(LAST_NAME);
            user.setIsActive(true);

            trainee = new Trainee();
            trainee.setUser(user);
            trainee.setDateOfBirth(LocalDate.of(1990, 1, 15));
            trainee.setAddress("123 Main St");

            profileResponse = new TraineeProfileResponse();
            profileResponse.setUsername(USERNAME);
            profileResponse.setFirstName(FIRST_NAME);
            profileResponse.setLastName(LAST_NAME);
            profileResponse.setIsActive(true);
        }

        @Test
        @DisplayName("Should get trainee profile successfully")
        void getTraineeProfile_Success() {
            // Given
            doNothing().when(traineeService).authenticate(USERNAME, PASSWORD);
            when(traineeService.getByUsername(USERNAME)).thenReturn(trainee);
            when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

            // When
            ResponseEntity<TraineeProfileResponse> response = traineeController.getTraineeProfile(USERNAME, PASSWORD);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo(USERNAME);
            assertThat(response.getBody().getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getBody().getLastName()).isEqualTo(LAST_NAME);

            verify(traineeService).authenticate(USERNAME, PASSWORD);
            verify(traineeService).getByUsername(USERNAME);
            verify(traineeMapper).toProfileResponse(trainee);
        }

        @Test
        @DisplayName("Should authenticate before fetching profile")
        void getTraineeProfile_AuthenticatesFirst() {
            // Given
            doNothing().when(traineeService).authenticate(USERNAME, PASSWORD);
            when(traineeService.getByUsername(USERNAME)).thenReturn(trainee);
            when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

            // When
            traineeController.getTraineeProfile(USERNAME, PASSWORD);

            // Then
            var inOrder = inOrder(traineeService);
            inOrder.verify(traineeService).authenticate(USERNAME, PASSWORD);
            inOrder.verify(traineeService).getByUsername(USERNAME);
        }
    }

    @Nested
    @DisplayName("Update Trainee Profile Tests")
    class UpdateTraineeProfileTests {

        private UpdateTraineeRequest updateRequest;
        private Trainee updatedTrainee;
        private TraineeProfileResponse profileResponse;

        @BeforeEach
        void setUp() {
            updateRequest = new UpdateTraineeRequest();
            updateRequest.setUsername(USERNAME);
            updateRequest.setFirstName("UpdatedJohn");
            updateRequest.setLastName("UpdatedDoe");
            updateRequest.setIsActive(true);

            User user = new User();
            user.setUsername(USERNAME);
            user.setFirstName("UpdatedJohn");
            user.setLastName("UpdatedDoe");
            user.setIsActive(true);

            updatedTrainee = new Trainee();
            updatedTrainee.setUser(user);

            profileResponse = new TraineeProfileResponse();
            profileResponse.setUsername(USERNAME);
            profileResponse.setFirstName("UpdatedJohn");
            profileResponse.setLastName("UpdatedDoe");
        }

        @Test
        @DisplayName("Should update trainee profile successfully")
        void updateTraineeProfile_Success() {
            // Given
            when(traineeService.updateProfile(USERNAME, updateRequest)).thenReturn(updatedTrainee);
            when(traineeMapper.toProfileResponse(updatedTrainee)).thenReturn(profileResponse);

            // When
            ResponseEntity<TraineeProfileResponse> response = traineeController.updateTraineeProfile(USERNAME, updateRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo("UpdatedJohn");

            verify(traineeService).updateProfile(USERNAME, updateRequest);
            verify(traineeMapper).toProfileResponse(updatedTrainee);
        }

        @Test
        @DisplayName("Should throw ValidationException when username mismatch")
        void updateTraineeProfile_UsernameMismatch_ThrowsException() {
            // Given
            String differentUsername = "different.user";

            // When/Then
            assertThatThrownBy(() -> traineeController.updateTraineeProfile(differentUsername, updateRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Username in path does not match username in request body");

            verify(traineeService, never()).updateProfile(any(), any());
        }
    }

    @Nested
    @DisplayName("Delete Trainee Profile Tests")
    class DeleteTraineeProfileTests {

        @Test
        @DisplayName("Should delete trainee profile successfully")
        void deleteTraineeProfile_Success() {
            // Given
            doNothing().when(traineeService).deleteByUsername(USERNAME, PASSWORD);

            // When
            ResponseEntity<Void> response = traineeController.deleteTraineeProfile(USERNAME, PASSWORD);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(response.getBody()).isNull();

            verify(traineeService).deleteByUsername(USERNAME, PASSWORD);
        }

        @Test
        @DisplayName("Should call service with correct parameters")
        void deleteTraineeProfile_CallsServiceWithCorrectParams() {
            // Given
            doNothing().when(traineeService).deleteByUsername(USERNAME, PASSWORD);

            // When
            traineeController.deleteTraineeProfile(USERNAME, PASSWORD);

            // Then
            verify(traineeService).deleteByUsername(USERNAME, PASSWORD);
        }
    }

    @Nested
    @DisplayName("Toggle Trainee Status Tests")
    class ToggleTraineeStatusTests {

        private ToggleActiveRequest toggleRequest;

        @BeforeEach
        void setUp() {
            toggleRequest = new ToggleActiveRequest();
            toggleRequest.setUsername(USERNAME);
            toggleRequest.setPassword(PASSWORD);
            toggleRequest.setIsActive(false);
        }

        @Test
        @DisplayName("Should toggle trainee status successfully")
        void toggleTraineeStatus_Success() {
            // Given
            doNothing().when(userService).setActiveStatus(USERNAME, PASSWORD, false);

            // When
            ResponseEntity<Void> response = traineeController.toggleTraineeStatus(USERNAME, toggleRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(userService).setActiveStatus(USERNAME, PASSWORD, false);
        }

        @Test
        @DisplayName("Should activate trainee successfully")
        void toggleTraineeStatus_Activate_Success() {
            // Given
            toggleRequest.setIsActive(true);
            doNothing().when(userService).setActiveStatus(USERNAME, PASSWORD, true);

            // When
            ResponseEntity<Void> response = traineeController.toggleTraineeStatus(USERNAME, toggleRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            verify(userService).setActiveStatus(USERNAME, PASSWORD, true);
        }

        @Test
        @DisplayName("Should throw ValidationException when username mismatch")
        void toggleTraineeStatus_UsernameMismatch_ThrowsException() {
            // Given
            String differentUsername = "different.user";

            // When/Then
            assertThatThrownBy(() -> traineeController.toggleTraineeStatus(differentUsername, toggleRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Username in path does not match username in request body");

            verify(userService, never()).setActiveStatus(any(), any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("Update Trainee Trainers List Tests")
    class UpdateTraineeTrainersListTests {

        private UpdateTraineeTrainersRequest updateTrainersRequest;
        private List<Trainer> trainers;
        private List<TrainerSummaryResponse> trainerSummaryResponses;

        @BeforeEach
        void setUp() {
            updateTrainersRequest = new UpdateTraineeTrainersRequest();
            updateTrainersRequest.setTraineeUsername(USERNAME);
            updateTrainersRequest.setPassword(PASSWORD);
            updateTrainersRequest.setTrainerUsernames(Arrays.asList("trainer1", "trainer2"));

            User trainerUser1 = new User();
            trainerUser1.setUsername("trainer1");
            trainerUser1.setFirstName("Trainer");
            trainerUser1.setLastName("One");

            User trainerUser2 = new User();
            trainerUser2.setUsername("trainer2");
            trainerUser2.setFirstName("Trainer");
            trainerUser2.setLastName("Two");

            Trainer trainer1 = new Trainer();
            trainer1.setUser(trainerUser1);

            Trainer trainer2 = new Trainer();
            trainer2.setUser(trainerUser2);

            trainers = Arrays.asList(trainer1, trainer2);

            TrainerSummaryResponse summary1 = new TrainerSummaryResponse();
            summary1.setUsername("trainer1");

            TrainerSummaryResponse summary2 = new TrainerSummaryResponse();
            summary2.setUsername("trainer2");

            trainerSummaryResponses = Arrays.asList(summary1, summary2);
        }

        @Test
        @DisplayName("Should update trainers list successfully")
        void updateTraineeTrainersList_Success() {
            // Given
            when(traineeService.updateTrainersList(USERNAME, PASSWORD, Arrays.asList("trainer1", "trainer2")))
                    .thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(trainerSummaryResponses);

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    traineeController.updateTraineeTrainersList(USERNAME, updateTrainersRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);

            verify(traineeService).updateTrainersList(USERNAME, PASSWORD, Arrays.asList("trainer1", "trainer2"));
            verify(trainerMapper).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should throw ValidationException when username mismatch")
        void updateTraineeTrainersList_UsernameMismatch_ThrowsException() {
            // Given
            String differentUsername = "different.user";

            // When/Then
            assertThatThrownBy(() -> traineeController.updateTraineeTrainersList(differentUsername, updateTrainersRequest))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Username in path does not match username in request body");

            verify(traineeService, never()).updateTrainersList(any(), any(), any());
        }

        @Test
        @DisplayName("Should return empty list when no trainers assigned")
        void updateTraineeTrainersList_EmptyList_Success() {
            // Given
            updateTrainersRequest.setTrainerUsernames(Collections.emptyList());
            when(traineeService.updateTrainersList(USERNAME, PASSWORD, Collections.emptyList()))
                    .thenReturn(Collections.emptyList());
            when(trainerMapper.toSummaryResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            // When
            ResponseEntity<List<TrainerSummaryResponse>> response =
                    traineeController.updateTraineeTrainersList(USERNAME, updateTrainersRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Trainee Trainings Tests")
    class GetTraineeTrainingsTests {

        private List<Training> trainings;
        private List<TrainingResponse> trainingResponses;
        private LocalDate fromDate;
        private LocalDate toDate;

        @BeforeEach
        void setUp() {
            fromDate = LocalDate.of(2024, 1, 1);
            toDate = LocalDate.of(2024, 12, 31);

            Training training1 = new Training();
            training1.setTrainingName("Training 1");

            Training training2 = new Training();
            training2.setTrainingName("Training 2");

            trainings = Arrays.asList(training1, training2);

            TrainingResponse response1 = new TrainingResponse();
            response1.setTrainingName("Training 1");

            TrainingResponse response2 = new TrainingResponse();
            response2.setTrainingName("Training 2");

            trainingResponses = Arrays.asList(response1, response2);
        }

        @Test
        @DisplayName("Should get trainee trainings with all filters")
        void getTraineeTrainings_WithAllFilters_Success() {
            // Given
            String trainerName = "trainer1";
            TrainingTypeName trainingType = TrainingTypeName.FITNESS;

            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, trainerName, trainingType))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response = traineeController.getTraineeTrainings(
                    USERNAME, PASSWORD, fromDate, toDate, trainerName, trainingType);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, trainerName, trainingType);
            verify(trainingMapper).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should get trainee trainings without filters")
        void getTraineeTrainings_WithoutFilters_Success() {
            // Given
            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response = traineeController.getTraineeTrainings(
                    USERNAME, PASSWORD, null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null, null);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTraineeTrainings_NoTrainings_ReturnsEmptyList() {
            // Given
            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null, null))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            // When
            ResponseEntity<List<TrainingResponse>> response = traineeController.getTraineeTrainings(
                    USERNAME, PASSWORD, null, null, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should get trainee trainings with date filters only")
        void getTraineeTrainings_WithDateFiltersOnly_Success() {
            // Given
            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response = traineeController.getTraineeTrainings(
                    USERNAME, PASSWORD, fromDate, toDate, null, null);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, fromDate, toDate, null, null);
        }

        @Test
        @DisplayName("Should get trainee trainings with training type filter only")
        void getTraineeTrainings_WithTrainingTypeOnly_Success() {
            // Given
            TrainingTypeName trainingType = TrainingTypeName.YOGA;

            when(trainingService.getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null, trainingType))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(trainingResponses);

            // When
            ResponseEntity<List<TrainingResponse>> response = traineeController.getTraineeTrainings(
                    USERNAME, PASSWORD, null, null, null, trainingType);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);

            verify(trainingService).getTraineeTrainingsByCriteria(
                    USERNAME, PASSWORD, null, null, null, trainingType);
        }
    }
}