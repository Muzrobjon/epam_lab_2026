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
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Mock
    private UserService userService;

    @InjectMocks
    private TrainerController trainerController;

    private Trainer testTrainer;
    private TrainingType testTrainingType;
    private TrainerRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        testTrainingType = TrainingType.builder()
                .id(1L)
                .trainingTypeName(TrainingTypeName.FITNESS)
                .build();

        testTrainer = Trainer.builder()
                .id(1L)
                .user(testUser)
                .specialization(testTrainingType)
                .build();

        registrationRequest = createTrainerRegistrationRequest("John", "Doe");
    }

    private TrainerRegistrationRequest createTrainerRegistrationRequest(String firstName, String lastName) {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSpecialization(TrainingTypeName.FITNESS);
        return request;
    }

    @Nested
    @DisplayName("Register Trainer Tests")
    class RegisterTrainerTests {

        @Test
        @DisplayName("Should register trainer successfully and return CREATED status")
        void registerTrainer_Success() {
            RegistrationResponse expectedResponse = RegistrationResponse.builder()
                    .username("john.doe")
                    .password("password123")
                    .build();

            when(trainerService.createProfile(any(TrainerRegistrationRequest.class)))
                    .thenReturn(testTrainer);
            when(userMapper.toRegistrationResponse(any(Trainer.class)))
                    .thenReturn(expectedResponse);

            ResponseEntity<RegistrationResponse> response =
                    trainerController.registerTrainer(registrationRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getUsername()).isEqualTo("john.doe");
            assertThat(response.getBody().getPassword()).isEqualTo("password123");

            verify(trainerService, times(1)).createProfile(registrationRequest);
            verify(userMapper, times(1)).toRegistrationResponse(testTrainer);
        }

        @Test
        @DisplayName("Should call service with correct request parameters")
        void registerTrainer_VerifyServiceCall() {
            TrainerRegistrationRequest request = createTrainerRegistrationRequest("Jane", "Smith");

            when(trainerService.createProfile(any(TrainerRegistrationRequest.class)))
                    .thenReturn(testTrainer);
            when(userMapper.toRegistrationResponse(any(Trainer.class)))
                    .thenReturn(new RegistrationResponse());

            trainerController.registerTrainer(request);

            verify(trainerService).createProfile(eq(request));
        }
    }

    @Nested
    @DisplayName("Get Trainer Profile Tests")
    class GetTrainerProfileTests {

        @Test
        @DisplayName("Should return trainer profile successfully")
        void getTrainerProfile_Success() {
            String username = "john.doe";
            TrainerProfileResponse expectedResponse = TrainerProfileResponse.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(username);
            when(trainerService.getByUsername(username)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(any(Trainer.class))).thenReturn(expectedResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.getTrainerProfile(username);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo("John");
            assertThat(response.getBody().getLastName()).isEqualTo("Doe");
            assertThat(response.getBody().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getBody().getIsActive()).isTrue();

            verify(userService, times(1)).isAuthenticated(username);
            verify(trainerService, times(1)).getByUsername(username);
            verify(trainerMapper, times(1)).toProfileResponse(testTrainer);
        }

        @Test
        @DisplayName("Should verify authentication before fetching profile")
        void getTrainerProfile_VerifyAuthenticationOrder() {
            String username = "john.doe";

            doNothing().when(userService).isAuthenticated(username);
            when(trainerService.getByUsername(username)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(any(Trainer.class)))
                    .thenReturn(new TrainerProfileResponse());

            trainerController.getTrainerProfile(username);

            verify(userService).isAuthenticated(username);
            verify(trainerService).getByUsername(username);
        }
    }

    @Nested
    @DisplayName("Update Trainer Profile Tests")
    class UpdateTrainerProfileTests {

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateTrainerProfile_Success() {
            String username = "john.doe";
            UpdateTrainerRequest request = createUpdateTrainerRequest(
                    "John Updated", "Doe Updated");

            Trainer updatedTrainer = Trainer.builder()
                    .id(1L)
                    .user(User.builder()
                            .firstName("John Updated")
                            .lastName("Doe Updated")
                            .username(username)
                            .isActive(true)
                            .build())
                    .specialization(testTrainingType)
                    .build();

            TrainerProfileResponse expectedResponse = TrainerProfileResponse.builder()
                    .firstName("John Updated")
                    .lastName("Doe Updated")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(username);
            when(trainerService.updateProfile(eq(username), any(UpdateTrainerRequest.class)))
                    .thenReturn(updatedTrainer);
            when(trainerMapper.toProfileResponse(any(Trainer.class))).thenReturn(expectedResponse);

            ResponseEntity<TrainerProfileResponse> response =
                    trainerController.updateTrainerProfile(username, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getFirstName()).isEqualTo("John Updated");
            assertThat(response.getBody().getLastName()).isEqualTo("Doe Updated");

            verify(userService, times(1)).isAuthenticated(username);
            verify(trainerService, times(1)).updateProfile(username, request);
            verify(trainerMapper, times(1)).toProfileResponse(updatedTrainer);
        }

        @Test
        @DisplayName("Should verify authentication before updating profile")
        void updateTrainerProfile_VerifyAuthentication() {
            String username = "john.doe";
            UpdateTrainerRequest request = createUpdateTrainerRequest(
                    "John", "Doe");

            doNothing().when(userService).isAuthenticated(username);
            when(trainerService.updateProfile(anyString(), any(UpdateTrainerRequest.class)))
                    .thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(any(Trainer.class)))
                    .thenReturn(new TrainerProfileResponse());

            trainerController.updateTrainerProfile(username, request);

            verify(userService).isAuthenticated(username);
        }

        private UpdateTrainerRequest createUpdateTrainerRequest(
                String firstName, String lastName) {
            UpdateTrainerRequest request = new UpdateTrainerRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setIsActive(true);
            return request;
        }
    }

    @Nested
    @DisplayName("Get Unassigned Trainers Tests")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should return list of unassigned trainers")
        void getUnassignedTrainers_Success() {
            String traineeUsername = "trainee.user";

            Trainer trainer1 = Trainer.builder()
                    .id(1L)
                    .user(User.builder()
                            .firstName("John")
                            .lastName("Doe")
                            .username("john.doe")
                            .build())
                    .specialization(testTrainingType)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(2L)
                    .user(User.builder()
                            .firstName("Jane")
                            .lastName("Smith")
                            .username("jane.smith")
                            .build())
                    .specialization(testTrainingType)
                    .build();

            List<Trainer> trainers = Arrays.asList(trainer1, trainer2);

            List<TrainerSummaryResponse> expectedResponse = Arrays.asList(
                    TrainerSummaryResponse.builder()
                            .username("john.doe")
                            .firstName("John")
                            .lastName("Doe")
                            .specialization(TrainingTypeName.FITNESS)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("jane.smith")
                            .firstName("Jane")
                            .lastName("Smith")
                            .specialization(TrainingTypeName.FITNESS)
                            .build()
            );

            when(trainerService.getUnassignedTrainers(traineeUsername)).thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(expectedResponse);

            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(traineeUsername);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getUsername()).isEqualTo("john.doe");
            assertThat(response.getBody().get(1).getUsername()).isEqualTo("jane.smith");

            verify(trainerService, times(1)).getUnassignedTrainers(traineeUsername);
            verify(trainerMapper, times(1)).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_EmptyList() {
            String traineeUsername = "trainee.user";

            when(trainerService.getUnassignedTrainers(traineeUsername))
                    .thenReturn(Collections.emptyList());
            when(trainerMapper.toSummaryResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainerSummaryResponse>> response =
                    trainerController.getUnassignedTrainers(traineeUsername);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Trainer Trainings Tests")
    class GetTrainerTrainingsTests {

        @Test
        @DisplayName("Should return trainer trainings with all filters")
        void getTrainerTrainings_WithAllFilters() {
            String username = "john.doe";
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 12, 31);
            String traineeName = "Jane";

            Training training1 = Training.builder()
                    .id(1L)
                    .trainingName("Morning Session")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingDurationMinutes(60)  // ✅ FIXED: was trainingDuration(60)
                    .build();

            Training training2 = Training.builder()
                    .id(2L)
                    .trainingName("Evening Session")
                    .trainingDate(LocalDate.of(2024, 6, 16))
                    .trainingDurationMinutes(90)  // ✅ FIXED: was trainingDuration(90)
                    .build();

            List<Training> trainings = Arrays.asList(training1, training2);

            List<TrainingResponse> expectedResponse = Arrays.asList(
                    TrainingResponse.builder()
                            .trainingName("Morning Session")
                            .trainingDate(LocalDate.of(2024, 6, 15))
                            .trainingDuration(60)
                            .build(),
                    TrainingResponse.builder()
                            .trainingName("Evening Session")
                            .trainingDate(LocalDate.of(2024, 6, 16))
                            .trainingDuration(90)
                            .build()
            );

            when(trainingService.getTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(expectedResponse);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(username, fromDate, toDate, traineeName);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(2);
            assertThat(response.getBody().get(0).getTrainingName()).isEqualTo("Morning Session");
            assertThat(response.getBody().get(1).getTrainingName()).isEqualTo("Evening Session");

            verify(trainingService, times(1))
                    .getTrainerTrainingsByCriteria(username, fromDate, toDate, traineeName);
            verify(trainingMapper, times(1)).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should return trainer trainings without filters")
        void getTrainerTrainings_WithoutFilters() {
            String username = "john.doe";

            Training training = Training.builder()
                    .id(1L)
                    .trainingName("Session")
                    .trainingDate(LocalDate.now())
                    .trainingDurationMinutes(60)  // ✅ FIXED: was trainingDuration(60)
                    .build();

            List<Training> trainings = Collections.singletonList(training);
            List<TrainingResponse> expectedResponse = Collections.singletonList(
                    TrainingResponse.builder()
                            .trainingName("Session")
                            .trainingDate(LocalDate.now())
                            .trainingDuration(60)
                            .build()
            );

            when(trainingService.getTrainerTrainingsByCriteria(username, null, null, null))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(expectedResponse);

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(username, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);

            verify(trainingService).getTrainerTrainingsByCriteria(username, null, null, null);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTrainerTrainings_EmptyList() {
            String username = "john.doe";

            when(trainingService.getTrainerTrainingsByCriteria(username, null, null, null))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(username, null, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();
        }

        @Test
        @DisplayName("Should return trainings with partial filters - only fromDate")
        void getTrainerTrainings_WithPartialFilters_OnlyFromDate() {
            String username = "john.doe";
            LocalDate fromDate = LocalDate.of(2024, 1, 1);

            when(trainingService.getTrainerTrainingsByCriteria(username, fromDate, null, null))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(username, fromDate, null, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).getTrainerTrainingsByCriteria(username, fromDate, null, null);
        }

        @Test
        @DisplayName("Should return trainings with partial filters - only traineeName")
        void getTrainerTrainings_WithPartialFilters_OnlyTraineeName() {
            String username = "john.doe";
            String traineeName = "Jane";

            when(trainingService.getTrainerTrainingsByCriteria(username, null, null, traineeName))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(Collections.emptyList()))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainingResponse>> response =
                    trainerController.getTrainerTrainings(username, null, null, traineeName);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).getTrainerTrainingsByCriteria(username, null, null, traineeName);
        }
    }
}