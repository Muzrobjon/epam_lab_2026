package com.epam.gym.controller;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.GlobalExceptionHandler;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.mapper.UserMapper;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainerController Unit Tests")
class TrainerControllerTest {

    private MockMvc mockMvc;

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

    private ObjectMapper objectMapper;

    private Trainer testTrainer;
    private Trainee testTrainee;
    private TrainingType trainingType;
    private Training testTraining;

    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup Trainer User
        // Test data
        User trainerUser = User.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(TRAINER_USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        // Setup Training Type
        trainingType = new TrainingType(1L, TrainingTypeName.FITNESS);

        // Setup Trainer
        testTrainer = Trainer.builder()
                .id(1L)
                .user(trainerUser)
                .specialization(trainingType)
                .trainees(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        // Setup Trainee User
        User traineeUser = User.builder()
                .id(2L)
                .firstName("John")
                .lastName("Doe")
                .username(TRAINEE_USERNAME)
                .password("traineePass")
                .isActive(true)
                .build();

        // Setup Trainee
        testTrainee = Trainee.builder()
                .id(1L)
                .user(traineeUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main Street")
                .trainers(new ArrayList<>())
                .trainings(new ArrayList<>())
                .build();

        // Setup Training
        testTraining = Training.builder()
                .id(1L)
                .trainee(testTrainee)
                .trainer(testTrainer)
                .trainingName("Morning Workout")
                .trainingType(trainingType)
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDurationMinutes(60)
                .build();
    }

    // ==================== REGISTER TRAINER TESTS ====================

    @Nested
    @DisplayName("POST /api/trainers - Register Trainer")
    class RegisterTrainerTests {

        @Test
        @DisplayName("Should register trainer successfully and return 201 CREATED")
        void registerTrainer_ValidRequest_ReturnsCreated() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            RegistrationResponse response = RegistrationResponse.builder()
                    .username(TRAINER_USERNAME)
                    .password(PASSWORD)
                    .build();

            when(trainerService.createProfile(any(TrainerRegistrationRequest.class))).thenReturn(testTrainer);
            when(userMapper.toRegistrationResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username", is(TRAINER_USERNAME)))
                    .andExpect(jsonPath("$.password", is(PASSWORD)));

            verify(trainerService).createProfile(any(TrainerRegistrationRequest.class));
            verify(userMapper).toRegistrationResponse(testTrainer);
        }

        @Test
        @DisplayName("Should return 400 when firstName is missing")
        void registerTrainer_MissingFirstName_ReturnsBadRequest() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }

        @Test
        @DisplayName("Should return 400 when lastName is missing")
        void registerTrainer_MissingLastName_ReturnsBadRequest() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }

        @Test
        @DisplayName("Should return 400 when specialization is missing")
        void registerTrainer_MissingSpecialization_ReturnsBadRequest() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }

        @Test
        @DisplayName("Should register trainer with YOGA specialization")
        void registerTrainer_YogaSpecialization_ReturnsCreated() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            RegistrationResponse response = RegistrationResponse.builder()
                    .username(TRAINER_USERNAME)
                    .password(PASSWORD)
                    .build();

            when(trainerService.createProfile(any(TrainerRegistrationRequest.class))).thenReturn(testTrainer);
            when(userMapper.toRegistrationResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(trainerService).createProfile(any(TrainerRegistrationRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void registerTrainer_EmptyBody_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }

        @Test
        @DisplayName("Should return 400 when firstName is blank")
        void registerTrainer_BlankFirstName_ReturnsBadRequest() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("   ")
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }

        @Test
        @DisplayName("Should return 400 when lastName is blank")
        void registerTrainer_BlankLastName_ReturnsBadRequest() throws Exception {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName("   ")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).createProfile(any());
        }
    }

    // ==================== GET TRAINER PROFILE TESTS ====================

    @Nested
    @DisplayName("GET /api/trainers/{username} - Get Trainer Profile")
    class GetTrainerProfileTests {

        @Test
        @DisplayName("Should return trainer profile successfully")
        void getTrainerProfile_ValidUsername_ReturnsProfile() throws Exception {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is(FIRST_NAME)))
                    .andExpect(jsonPath("$.lastName", is(LAST_NAME)))
                    .andExpect(jsonPath("$.specialization", is("FITNESS")))
                    .andExpect(jsonPath("$.isActive", is(true)));

            verify(userService).isAuthenticated(TRAINER_USERNAME);
            verify(trainerService).getByUsername(TRAINER_USERNAME);
            verify(trainerMapper).toProfileResponse(testTrainer);
        }

        @Test
        @DisplayName("Should return trainer profile with trainees")
        void getTrainerProfile_WithTrainees_ReturnsProfileWithTrainees() throws Exception {
            TraineeSummaryResponse traineeSummary = TraineeSummaryResponse.builder()
                    .username(TRAINEE_USERNAME)
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of(traineeSummary))
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainees", hasSize(1)))
                    .andExpect(jsonPath("$.trainees[0].username", is(TRAINEE_USERNAME)))
                    .andExpect(jsonPath("$.trainees[0].firstName", is("John")))
                    .andExpect(jsonPath("$.trainees[0].lastName", is("Doe")));

            verify(trainerMapper).toProfileResponse(testTrainer);
        }

        @Test
        @DisplayName("Should return trainer profile with multiple trainees")
        void getTrainerProfile_WithMultipleTrainees_ReturnsProfileWithAllTrainees() throws Exception {
            TraineeSummaryResponse traineeSummary1 = TraineeSummaryResponse.builder()
                    .username(TRAINEE_USERNAME)
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            TraineeSummaryResponse traineeSummary2 = TraineeSummaryResponse.builder()
                    .username("alice.johnson")
                    .firstName("Alice")
                    .lastName("Johnson")
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of(traineeSummary1, traineeSummary2))
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainees", hasSize(2)))
                    .andExpect(jsonPath("$.trainees[0].username", is(TRAINEE_USERNAME)))
                    .andExpect(jsonPath("$.trainees[1].username", is("alice.johnson")));
        }

        @Test
        @DisplayName("Should return 404 when trainer not found")
        void getTrainerProfile_TrainerNotFound_ReturnsNotFound() throws Exception {
            String nonExistentUsername = "nonexistent.trainer";

            doNothing().when(userService).isAuthenticated(nonExistentUsername);
            when(trainerService.getByUsername(nonExistentUsername))
                    .thenThrow(new NotFoundException("Trainer not found: " + nonExistentUsername));

            mockMvc.perform(get("/api/trainers/{username}", nonExistentUsername))
                    .andExpect(status().isNotFound());

            verify(trainerService).getByUsername(nonExistentUsername);
            verify(trainerMapper, never()).toProfileResponse(any());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void getTrainerProfile_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            doThrow(new AuthenticationException("User is not authenticated: " + TRAINER_USERNAME))
                    .when(userService).isAuthenticated(TRAINER_USERNAME);

            mockMvc.perform(get("/api/trainers/{username}", TRAINER_USERNAME))
                    .andExpect(status().isUnauthorized());

            verify(userService).isAuthenticated(TRAINER_USERNAME);
            verify(trainerService, never()).getByUsername(anyString());
        }

        @Test
        @DisplayName("Should return inactive trainer profile")
        void getTrainerProfile_InactiveTrainer_ReturnsProfile() throws Exception {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(false)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.getByUsername(TRAINER_USERNAME)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive", is(false)));
        }
    }

    // ==================== UPDATE TRAINER PROFILE TESTS ====================

    @Nested
    @DisplayName("PUT /api/trainers/{username} - Update Trainer Profile")
    class UpdateTrainerProfileTests {

        @Test
        @DisplayName("Should update trainer profile successfully")
        void updateTrainerProfile_ValidRequest_ReturnsUpdatedProfile() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .isActive(true)
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.updateProfile(eq(TRAINER_USERNAME), any(UpdateTrainerRequest.class)))
                    .thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName", is("UpdatedFirst")))
                    .andExpect(jsonPath("$.lastName", is("UpdatedLast")))
                    .andExpect(jsonPath("$.isActive", is(true)));

            verify(userService).isAuthenticated(TRAINER_USERNAME);
            verify(trainerService).updateProfile(eq(TRAINER_USERNAME), any(UpdateTrainerRequest.class));
            verify(trainerMapper).toProfileResponse(testTrainer);
        }

        @Test
        @DisplayName("Should deactivate trainer")
        void updateTrainerProfile_Deactivate_ReturnsUpdatedProfile() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(false)
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(false)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.updateProfile(eq(TRAINER_USERNAME), any(UpdateTrainerRequest.class)))
                    .thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive", is(false)));
        }

        @Test
        @DisplayName("Should return 400 when username in request is missing")
        void updateTrainerProfile_MissingUsername_ReturnsBadRequest() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).updateProfile(anyString(), any());
        }

        @Test
        @DisplayName("Should return 400 when firstName is missing")
        void updateTrainerProfile_MissingFirstName_ReturnsBadRequest() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).updateProfile(anyString(), any());
        }

        @Test
        @DisplayName("Should return 400 when lastName is missing")
        void updateTrainerProfile_MissingLastName_ReturnsBadRequest() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .isActive(true)
                    .build();

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).updateProfile(anyString(), any());
        }

        @Test
        @DisplayName("Should return 400 when isActive is missing")
        void updateTrainerProfile_MissingIsActive_ReturnsBadRequest() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .build();

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainerService, never()).updateProfile(anyString(), any());
        }

        @Test
        @DisplayName("Should return 404 when trainer not found")
        void updateTrainerProfile_TrainerNotFound_ReturnsNotFound() throws Exception {
            String nonExistentUsername = "nonexistent.trainer";

            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(nonExistentUsername)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doNothing().when(userService).isAuthenticated(nonExistentUsername);
            when(trainerService.updateProfile(eq(nonExistentUsername), any(UpdateTrainerRequest.class)))
                    .thenThrow(new NotFoundException("Trainer not found: " + nonExistentUsername));

            mockMvc.perform(put("/api/trainers/{username}", nonExistentUsername)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void updateTrainerProfile_NotAuthenticated_ReturnsUnauthorized() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .isActive(true)
                    .build();

            doThrow(new AuthenticationException("User is not authenticated"))
                    .when(userService).isAuthenticated(TRAINER_USERNAME);

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(trainerService, never()).updateProfile(anyString(), any());
        }

        @Test
        @DisplayName("Should return updated profile with trainees preserved")
        void updateTrainerProfile_WithTrainees_ReturnsProfileWithTrainees() throws Exception {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(TRAINER_USERNAME)
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .isActive(true)
                    .build();

            TraineeSummaryResponse traineeSummary = TraineeSummaryResponse.builder()
                    .username(TRAINEE_USERNAME)
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName("UpdatedFirst")
                    .lastName("UpdatedLast")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(List.of(traineeSummary))
                    .build();

            doNothing().when(userService).isAuthenticated(TRAINER_USERNAME);
            when(trainerService.updateProfile(eq(TRAINER_USERNAME), any(UpdateTrainerRequest.class)))
                    .thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(put("/api/trainers/{username}", TRAINER_USERNAME)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainees", hasSize(1)))
                    .andExpect(jsonPath("$.trainees[0].username", is(TRAINEE_USERNAME)));
        }
    }

    // ==================== GET UNASSIGNED TRAINERS TESTS ====================

    @Nested
    @DisplayName("GET /api/trainers/{traineeUsername}/trainers/unassigned - Get Unassigned Trainers")
    class GetUnassignedTrainersTests {

        @Test
        @DisplayName("Should return unassigned trainers successfully")
        void getUnassignedTrainers_ValidRequest_ReturnsTrainers() throws Exception {
            List<Trainer> trainers = List.of(testTrainer);

            TrainerSummaryResponse trainerSummary = TrainerSummaryResponse.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            List<TrainerSummaryResponse> summaryList = List.of(trainerSummary);

            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryList);

            mockMvc.perform(get("/api/trainers/{traineeUsername}/trainers/unassigned", TRAINEE_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].username", is(TRAINER_USERNAME)))
                    .andExpect(jsonPath("$[0].firstName", is(FIRST_NAME)))
                    .andExpect(jsonPath("$[0].lastName", is(LAST_NAME)))
                    .andExpect(jsonPath("$[0].specialization", is("FITNESS")));

            verify(trainerService).getUnassignedTrainers(TRAINEE_USERNAME);
            verify(trainerMapper).toSummaryResponseList(trainers);
        }

        @Test
        @DisplayName("Should return multiple unassigned trainers")
        void getUnassignedTrainers_MultipleTrainers_ReturnsAllTrainers() throws Exception {
            User trainer2User = User.builder()
                    .id(3L)
                    .firstName("Bob")
                    .lastName("Johnson")
                    .username("bob.johnson")
                    .isActive(true)
                    .build();

            Trainer trainer2 = Trainer.builder()
                    .id(2L)
                    .user(trainer2User)
                    .specialization(new TrainingType(2L, TrainingTypeName.YOGA))
                    .build();

            List<Trainer> trainers = List.of(testTrainer, trainer2);

            TrainerSummaryResponse summary1 = TrainerSummaryResponse.builder()
                    .username(TRAINER_USERNAME)
                    .firstName(FIRST_NAME)
                    .lastName(LAST_NAME)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            TrainerSummaryResponse summary2 = TrainerSummaryResponse.builder()
                    .username("bob.johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            List<TrainerSummaryResponse> summaryList = List.of(summary1, summary2);

            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(trainers);
            when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(summaryList);

            mockMvc.perform(get("/api/trainers/{traineeUsername}/trainers/unassigned", TRAINEE_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].username", is(TRAINER_USERNAME)))
                    .andExpect(jsonPath("$[1].username", is("bob.johnson")));
        }

        @Test
        @DisplayName("Should return empty list when no unassigned trainers")
        void getUnassignedTrainers_NoTrainers_ReturnsEmptyList() throws Exception {
            when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME)).thenReturn(Collections.emptyList());
            when(trainerMapper.toSummaryResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainers/{traineeUsername}/trainers/unassigned", TRAINEE_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 404 when trainee not found")
        void getUnassignedTrainers_TraineeNotFound_ReturnsNotFound() throws Exception {
            String nonExistentTrainee = "nonexistent.trainee";

            when(trainerService.getUnassignedTrainers(nonExistentTrainee))
                    .thenThrow(new NotFoundException("Trainee not found: " + nonExistentTrainee));

            mockMvc.perform(get("/api/trainers/{traineeUsername}/trainers/unassigned", nonExistentTrainee))
                    .andExpect(status().isNotFound());

            verify(trainerMapper, never()).toSummaryResponseList(anyList());
        }
    }

    // ==================== GET TRAINER TRAININGS TESTS ====================

    @Nested
    @DisplayName("GET /api/trainers/{username}/trainings - Get Trainer Trainings")
    class GetTrainerTrainingsTests {

        @Test
        @DisplayName("Should return trainer trainings without filters")
        void getTrainerTrainings_NoFilters_ReturnsTrainings() throws Exception {
            List<Training> trainings = List.of(testTraining);

            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingType(TrainingTypeName.FITNESS)
                    .trainingDuration(60)
                    .trainerName("Jane Smith")
                    .build();

            List<TrainingResponse> responseList = List.of(trainingResponse);

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(responseList);

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].trainingName", is("Morning Workout")))
                    .andExpect(jsonPath("$[0].trainingDuration", is(60)));

            verify(trainingService).getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, null);
            verify(trainingMapper).toResponseList(trainings);
        }

        @Test
        @DisplayName("Should return trainer trainings with date filters")
        void getTrainerTrainings_WithDateFilters_ReturnsFilteredTrainings() throws Exception {
            LocalDate fromDate = LocalDate.of(2024, 1, 1);
            LocalDate toDate = LocalDate.of(2024, 12, 31);
            List<Training> trainings = List.of(testTraining);

            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingType(TrainingTypeName.FITNESS)
                    .trainingDuration(60)
                    .trainerName("Jane Smith")
                    .build();

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), eq(fromDate), eq(toDate), isNull()))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(List.of(trainingResponse));

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("fromDate", "2024-01-01")
                            .param("toDate", "2024-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(trainingService).getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, fromDate, toDate, null);
        }

        @Test
        @DisplayName("Should return trainer trainings filtered by trainee name")
        void getTrainerTrainings_WithTraineeNameFilter_ReturnsFilteredTrainings() throws Exception {
            String traineeName = "John";
            List<Training> trainings = List.of(testTraining);

            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingType(TrainingTypeName.FITNESS)
                    .trainingDuration(60)
                    .trainerName("Jane Smith")
                    .build();

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), eq(traineeName)))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(List.of(trainingResponse));

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("traineeName", traineeName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(trainingService).getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, traineeName);
        }

        @Test
        @DisplayName("Should return empty list when no trainings found")
        void getTrainerTrainings_NoTrainings_ReturnsEmptyList() throws Exception {
            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return multiple trainings")
        void getTrainerTrainings_MultipleTrainings_ReturnsAllTrainings() throws Exception {
            Training training2 = Training.builder()
                    .id(2L)
                    .trainee(testTrainee)
                    .trainer(testTrainer)
                    .trainingName("Evening Workout")
                    .trainingType(trainingType)
                    .trainingDate(LocalDate.of(2024, 6, 16))
                    .trainingDurationMinutes(45)
                    .build();

            List<Training> trainings = List.of(testTraining, training2);

            TrainingResponse response1 = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingDuration(60)
                    .build();

            TrainingResponse response2 = TrainingResponse.builder()
                    .trainingName("Evening Workout")
                    .trainingDate(LocalDate.of(2024, 6, 16))
                    .trainingDuration(45)
                    .build();

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), isNull()))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(List.of(response1, response2));

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].trainingName", is("Morning Workout")))
                    .andExpect(jsonPath("$[1].trainingName", is("Evening Workout")));
        }

        @Test
        @DisplayName("Should return trainings filtered by fromDate only")
        void getTrainerTrainings_WithFromDateOnly_ReturnsFilteredTrainings() throws Exception {
            LocalDate fromDate = LocalDate.of(2024, 6, 1);
            List<Training> trainings = List.of(testTraining);

            TrainingResponse trainingResponse = TrainingResponse.builder()
                    .trainingName("Morning Workout")
                    .trainingDate(LocalDate.of(2024, 6, 15))
                    .trainingType(TrainingTypeName.FITNESS)
                    .trainingDuration(60)
                    .build();

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), eq(fromDate), isNull(), isNull()))
                    .thenReturn(trainings);
            when(trainingMapper.toResponseList(trainings)).thenReturn(List.of(trainingResponse));

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("fromDate", "2024-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(trainingService).getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, fromDate, null, null);
        }
    }

    // ==================== EDGE CASES ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle username with special characters in path")
        void getTrainerProfile_UsernameWithDot_HandlesCorrectly() throws Exception {
            String usernameWithDot = "john.doe.smith";

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName("John")
                    .lastName("Doe Smith")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(usernameWithDot);
            when(trainerService.getByUsername(usernameWithDot)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", usernameWithDot))
                    .andExpect(status().isOk());

            verify(trainerService).getByUsername(usernameWithDot);
        }

        @Test
        @DisplayName("Should handle very long trainer name")
        void registerTrainer_VeryLongName_HandlesCorrectly() throws Exception {
            String longFirstName = "A".repeat(50);
            String longLastName = "B".repeat(50);

            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(longFirstName)
                    .lastName(longLastName)
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            RegistrationResponse response = RegistrationResponse.builder()
                    .username("a".repeat(50) + "." + "b".repeat(50))
                    .password(PASSWORD)
                    .build();

            when(trainerService.createProfile(any(TrainerRegistrationRequest.class))).thenReturn(testTrainer);
            when(userMapper.toRegistrationResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(post("/api/trainers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should handle fromDate after toDate gracefully")
        void getTrainerTrainings_FromDateAfterToDate_ReturnsEmptyOrError() throws Exception {
            LocalDate fromDate = LocalDate.of(2024, 12, 31);
            LocalDate toDate = LocalDate.of(2024, 1, 1);

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), eq(fromDate), eq(toDate), isNull()))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("fromDate", "2024-12-31")
                            .param("toDate", "2024-01-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should handle numeric username")
        void getTrainerProfile_NumericUsername_HandlesCorrectly() throws Exception {
            String numericUsername = "12345";

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .firstName("Numeric")
                    .lastName("User")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .trainees(Collections.emptyList())
                    .build();

            doNothing().when(userService).isAuthenticated(numericUsername);
            when(trainerService.getByUsername(numericUsername)).thenReturn(testTrainer);
            when(trainerMapper.toProfileResponse(testTrainer)).thenReturn(response);

            mockMvc.perform(get("/api/trainers/{username}", numericUsername))
                    .andExpect(status().isOk());

            verify(trainerService).getByUsername(numericUsername);
        }

        @Test
        @DisplayName("Should handle empty trainee name filter")
        void getTrainerTrainings_EmptyTraineeName_HandlesCorrectly() throws Exception {
            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), eq("")))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("traineeName", ""))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle special characters in trainee name filter")
        void getTrainerTrainings_SpecialCharactersInTraineeName_HandlesCorrectly() throws Exception {
            String specialTraineeName = "John O'Brien";

            when(trainingService.getTrainerTrainingsByCriteria(
                    eq(TRAINER_USERNAME), isNull(), isNull(), eq(specialTraineeName)))
                    .thenReturn(Collections.emptyList());
            when(trainingMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainers/{username}/trainings", TRAINER_USERNAME)
                            .param("traineeName", specialTraineeName))
                    .andExpect(status().isOk());

            verify(trainingService).getTrainerTrainingsByCriteria(
                    TRAINER_USERNAME, null, null, specialTraineeName);
        }
    }
}