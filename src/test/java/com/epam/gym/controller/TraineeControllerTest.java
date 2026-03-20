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
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TraineeController Tests")
class TraineeControllerTest {

    private MockMvc mockMvc;

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

    private ObjectMapper objectMapper;

    private Trainee testTrainee;
    private Trainer testTrainer;
    private TrainingType trainingType;
    private TraineeProfileResponse profileResponse;
    private RegistrationResponse registrationResponse;

    private static final String USERNAME = "john.doe";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main St";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Create User
        User testUser = User.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        // Create TrainingType (no builder, use constructor)
        trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        // Create Trainee
        testTrainee = Trainee.builder()
                .id(1L)
                .user(testUser)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();

        // Create Trainer User
        User trainerUser = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .username("jane.smith")
                .password("trainerPass")
                .isActive(true)
                .build();

        // Create Trainer
        testTrainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .build();

        // Create Response DTOs
        profileResponse = TraineeProfileResponse.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .isActive(true)
                .trainers(new ArrayList<>())
                .build();

        registrationResponse = RegistrationResponse.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .build();
    }

    // ==================== REGISTER TRAINEE TESTS ====================

    @Nested
    @DisplayName("Register Trainee Tests")
    class RegisterTraineeTests {

        @Test
        @DisplayName("Should return 201 CREATED when registration is successful")
        void registerTrainee_WithValidRequest_ReturnsCreated() throws Exception {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .dateOfBirth(DATE_OF_BIRTH)
                    .address(ADDRESS)
                    .build();

            when(traineeService.createProfile(any(TraineeRegistrationRequest.class))).thenReturn(testTrainee);
            when(userMapper.toRegistrationResponse(any(Trainee.class))).thenReturn(registrationResponse);

            // Act & Assert
            mockMvc.perform(post("/api/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.password").value(PASSWORD));

            verify(traineeService).createProfile(any(TraineeRegistrationRequest.class));
            verify(userMapper).toRegistrationResponse(any(Trainee.class));
        }

        @Test
        @DisplayName("Should return 201 CREATED without optional fields")
        void registerTrainee_WithoutOptionalFields_ReturnsCreated() throws Exception {
            // Arrange
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            when(traineeService.createProfile(any(TraineeRegistrationRequest.class))).thenReturn(testTrainee);
            when(userMapper.toRegistrationResponse(any(Trainee.class))).thenReturn(registrationResponse);

            // Act & Assert
            mockMvc.perform(post("/api/trainees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(traineeService).createProfile(any(TraineeRegistrationRequest.class));
        }
    }

    // ==================== GET TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Get Trainee Profile Tests")
    class GetTraineeProfileTests {

        @Test
        @DisplayName("Should return 200 OK with trainee profile")
        void getTraineeProfile_WithValidUsername_ReturnsProfile() throws Exception {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeService.getByUsername(USERNAME)).thenReturn(testTrainee);
            when(traineeMapper.toProfileResponse(any(Trainee.class))).thenReturn(profileResponse);

            // Act & Assert
            mockMvc.perform(get("/api/trainees/{username}", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.firstName").value(FIRST_NAME))
                    .andExpect(jsonPath("$.lastName").value(LAST_NAME))
                    .andExpect(jsonPath("$.isActive").value(true));

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeService).getByUsername(USERNAME);
            verify(traineeMapper).toProfileResponse(any(Trainee.class));
        }
    }

    // ==================== UPDATE TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Update Trainee Profile Tests")
    class UpdateTraineeProfileTests {

        @Test
        @DisplayName("Should return 200 OK when update is successful")
        void updateTraineeProfile_WithValidRequest_ReturnsUpdatedProfile() throws Exception {
            // Arrange
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(USERNAME)
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .isActive(true)
                    .dateOfBirth(DATE_OF_BIRTH)
                    .address("New Address")
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            when(traineeService.updateProfile(eq(USERNAME), any(UpdateTraineeRequest.class)))
                    .thenReturn(testTrainee);
            when(traineeMapper.toProfileResponse(any(Trainee.class))).thenReturn(profileResponse);

            // Act & Assert
            mockMvc.perform(put("/api/trainees/{username}", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeService).updateProfile(eq(USERNAME), any(UpdateTraineeRequest.class));
        }
    }

    // ==================== DELETE TRAINEE PROFILE TESTS ====================

    @Nested
    @DisplayName("Delete Trainee Profile Tests")
    class DeleteTraineeProfileTests {

        @Test
        @DisplayName("Should return 204 NO CONTENT when delete is successful")
        void deleteTraineeProfile_WithValidUsername_ReturnsNoContent() throws Exception {
            // Arrange
            doNothing().when(userService).isAuthenticated(USERNAME);
            doNothing().when(traineeService).deleteByUsername(USERNAME);

            // Act & Assert
            mockMvc.perform(delete("/api/trainees/{username}", USERNAME))
                    .andExpect(status().isNoContent());

            verify(userService).isAuthenticated(USERNAME);
            verify(traineeService).deleteByUsername(USERNAME);
        }
    }

    // ==================== TOGGLE TRAINEE STATUS TESTS ====================

    @Nested
    @DisplayName("Toggle Trainee Status Tests")
    class ToggleTraineeStatusTests {

        @Test
        @DisplayName("Should return 200 OK when activating trainee")
        void toggleTraineeStatus_Activate_ReturnsOk() throws Exception {
            // Arrange - ToggleActiveRequest has username and isActive fields
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(USERNAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, true);

            // Act & Assert
            mockMvc.perform(patch("/api/trainees/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).isAuthenticated(USERNAME);
            verify(userService).setActiveStatus(USERNAME, true);
        }

        @Test
        @DisplayName("Should return 200 OK when deactivating trainee")
        void toggleTraineeStatus_Deactivate_ReturnsOk() throws Exception {
            // Arrange
            ToggleActiveRequest request = ToggleActiveRequest.builder()
                    .username(USERNAME)
                    .isActive(false)
                    .build();

            doNothing().when(userService).isAuthenticated(USERNAME);
            doNothing().when(userService).setActiveStatus(USERNAME, false);

            // Act & Assert
            mockMvc.perform(patch("/api/trainees/{username}/status", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(userService).isAuthenticated(USERNAME);
            verify(userService).setActiveStatus(USERNAME, false);
        }
    }

    // ==================== UPDATE TRAINEE TRAINERS LIST TESTS ====================

    @Nested
    @DisplayName("Update Trainee Trainers List Tests")
    class UpdateTraineeTrainersListTests {

        @Test
        @DisplayName("Should return 200 OK with updated trainers list")
        void updateTraineeTrainersList_WithValidRequest_ReturnsTrainersList() throws Exception {
            // Arrange
            UpdateTraineeTrainersRequest request = UpdateTraineeTrainersRequest.builder()
                    .traineeUsername(USERNAME)
                    .trainerUsernames(List.of("jane.smith"))
                    .build();

            List<Trainer> trainers = List.of(testTrainer);

            TrainerSummaryResponse trainerSummary = TrainerSummaryResponse.builder()
                    .username("jane.smith")
                    .firstName("Jane")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            List<TrainerSummaryResponse> trainerResponses = List.of(trainerSummary);

            when(traineeService.updateTrainersList(eq(USERNAME), anyList())).thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(anyList())).thenReturn(trainerResponses);

            // Act & Assert
            mockMvc.perform(put("/api/trainees/{username}/trainers", USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].username").value("jane.smith"))
                    .andExpect(jsonPath("$[0].firstName").value("Jane"))
                    .andExpect(jsonPath("$[0].lastName").value("Smith"));

            verify(traineeService).updateTrainersList(eq(USERNAME), anyList());
            verify(trainerMapper).toSummaryResponseList(anyList());
        }
    }

    // ==================== GET TRAINEE TRAININGS TESTS ====================

    @Nested
    @DisplayName("Get Trainee Trainings Tests")
    class GetTraineeTrainingsTests {

        @Test
        @DisplayName("Should return 200 OK with trainings list")
        void getTraineeTrainings_WithValidRequest_ReturnsTrainingsList() throws Exception {
            // Arrange
            Training training = Training.builder()
                    .id(1L)
                    .trainee(testTrainee)
                    .trainer(testTrainer)
                    .trainingName("Morning Workout")
                    .trainingType(trainingType)
                    .trainingDate(LocalDate.now())
                    .trainingDurationMinutes(60)
                    .build();

            List<Training> trainings = List.of(training);

            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.now())
                    .trainingType(TrainingTypeName.FITNESS)
                    .trainingDuration(60)
                    .trainerName("Jane Smith")
                    .traineeName("John Doe")
                    .build();

            List<TrainingResponse> trainingResponses = List.of(trainingResponse);

            when(trainingService.getTraineeTrainingsByCriteria(
                    eq(USERNAME), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(anyList())).thenReturn(trainingResponses);

            // Act & Assert
            mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingName").value("Morning Workout"))
                    .andExpect(jsonPath("$[0].trainingDuration").value(60));

            verify(trainingService).getTraineeTrainingsByCriteria(
                    eq(USERNAME), isNull(), isNull(), isNull(), isNull());
            verify(trainingMapper).toResponseList(anyList());
        }

        @Test
        @DisplayName("Should return 200 OK with filters applied")
        void getTraineeTrainings_WithFilters_ReturnsFilteredTrainingsList() throws Exception {
            // Arrange
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 12, 31);
            String trainerName = "Jane";

            when(trainingService.getTraineeTrainingsByCriteria(
                    eq(USERNAME), eq(fromDate), eq(toDate), eq(trainerName), eq(TrainingTypeName.FITNESS)))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(anyList())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME)
                            .param("fromDate", "2024-01-01")
                            .param("toDate", "2024-12-31")
                            .param("trainerName", trainerName)
                            .param("trainingType", "FITNESS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(trainingService).getTraineeTrainingsByCriteria(
                    eq(USERNAME), eq(fromDate), eq(toDate), eq(trainerName), eq(TrainingTypeName.FITNESS));
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTraineeTrainings_NoTrainings_ReturnsEmptyList() throws Exception {
            // Arrange
            when(trainingService.getTraineeTrainingsByCriteria(
                    eq(USERNAME), isNull(), isNull(), isNull(), isNull()))
                    .thenReturn(List.of());
            when(trainingMapper.toResponseList(anyList())).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }
}