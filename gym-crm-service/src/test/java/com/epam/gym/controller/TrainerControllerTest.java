package com.epam.gym.controller;

import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.RegistrationResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.security.JwtProvider;
import com.epam.gym.security.LoginAttemptService;
import com.epam.gym.security.TokenBlacklistService;
import com.epam.gym.security.UserDetailsServiceImpl;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@AutoConfigureMockMvc(addFilters = false)
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private LoginAttemptService loginAttemptService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private TrainerService trainerService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private TrainerMapper trainerMapper;

    @MockitoBean
    private TrainingMapper trainingMapper;

    @MockitoBean
    private UserService userService;

    private static final String USERNAME = "jane.smith";
    private static final String TRAINEE_USERNAME = "john.doe";

    private Trainer trainer;
    private TrainerProfileResponse profileResponse;
    private RegistrationResponse registrationResponse;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();

        profileResponse = TrainerProfileResponse.builder()
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .trainees(List.of())
                .build();

        registrationResponse = RegistrationResponse.builder()
                .username(USERNAME)
                .password("generatedPass123")
                .build();
    }

    // ==================== POST /api/trainers ====================

    @Test
    void registerTrainer_ValidRequest_ShouldReturnCreated() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        when(trainerService.createProfile(any(TrainerRegistrationRequest.class)))
                .thenReturn(registrationResponse);

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.password").value("generatedPass123"));

        verify(trainerService).createProfile(any(TrainerRegistrationRequest.class));
    }


    @Test
    void registerTrainer_MissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        // firstName and lastName intentionally missing

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void registerTrainer_ServiceThrowsException_ShouldReturnError() throws Exception {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        when(trainerService.createProfile(any(TrainerRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Registration failed"));

        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ==================== GET /api/trainers/{username} ====================

    @Test
    void getTrainerProfile_ExistingUser_ShouldReturnProfile() throws Exception {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerService.getByUsername(USERNAME)).thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/trainers/{username}", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).isAuthenticated(USERNAME);
        verify(trainerService).getByUsername(USERNAME);
        verify(trainerMapper).toProfileResponse(trainer);
    }

    @Test
    void getTrainerProfile_ServiceThrowsException_ShouldReturnError() throws Exception {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerService.getByUsername(USERNAME))
                .thenThrow(new RuntimeException("Trainer not found"));

        mockMvc.perform(get("/api/trainers/{username}", USERNAME))
                .andExpect(status().isInternalServerError());

        verify(userService).isAuthenticated(USERNAME);
        verify(trainerMapper, never()).toProfileResponse(any());
    }

    // ==================== PUT /api/trainers/{username} ====================

    @Test
    void updateTrainerProfile_ValidRequest_ShouldReturnUpdatedProfile() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(USERNAME);
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        TrainerProfileResponse updatedResponse = TrainerProfileResponse.builder()
                .firstName("Jane")
                .lastName("Updated")
                .isActive(true)
                .trainees(List.of())
                .build();

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerService.updateProfile(eq(USERNAME), any(UpdateTrainerRequest.class)))
                .thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/trainers/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(userService).isAuthenticated(USERNAME);
        verify(trainerService).updateProfile(eq(USERNAME), any(UpdateTrainerRequest.class));
    }

    @Test
    void updateTrainerProfile_MissingUsername_ShouldReturnBadRequest() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        mockMvc.perform(put("/api/trainers/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    @Test
    void updateTrainerProfile_ServiceThrowsException_ShouldReturnError() throws Exception {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(USERNAME);
        request.setFirstName("Jane");
        request.setLastName("Updated");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(trainerService.updateProfile(eq(USERNAME), any(UpdateTrainerRequest.class)))
                .thenThrow(new RuntimeException("Trainer not found"));

        mockMvc.perform(put("/api/trainers/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ==================== GET /api/trainers/unassigned ====================

    @Test
    void getUnassignedTrainers_ValidTrainee_ShouldReturnList() throws Exception {
        TrainerSummaryResponse summary1 = TrainerSummaryResponse.builder()
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .build();

        TrainerSummaryResponse summary2 = TrainerSummaryResponse.builder()
                .username("trainer2")
                .firstName("Trainer")
                .lastName("Two")
                .build();

        List<Trainer> trainers = List.of(new Trainer(), new Trainer());

        when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(trainers);
        when(trainerMapper.toSummaryResponseList(trainers))
                .thenReturn(List.of(summary1, summary2));

        mockMvc.perform(get("/api/trainers/unassigned")
                        .param("trainee", TRAINEE_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("trainer1"))
                .andExpect(jsonPath("$[1].username").value("trainer2"));

        verify(trainerService).getUnassignedTrainers(TRAINEE_USERNAME);
        verify(trainerMapper).toSummaryResponseList(trainers);
    }

    @Test
    void getUnassignedTrainers_EmptyResult_ShouldReturnEmptyList() throws Exception {
        when(trainerService.getUnassignedTrainers(TRAINEE_USERNAME))
                .thenReturn(List.of());
        when(trainerMapper.toSummaryResponseList(List.of()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/unassigned")
                        .param("trainee", TRAINEE_USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(trainerService).getUnassignedTrainers(TRAINEE_USERNAME);
    }

    @Test
    void getUnassignedTrainers_MissingTraineeParam_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/trainers/unassigned"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
    }

    // ==================== GET /api/trainers/{username}/trainings ====================

    @Test
    void getTrainerTrainings_NoFilters_ShouldReturnAllTrainings() throws Exception {
        TrainingResponse trainingResponse = TrainingResponse.builder()
                .trainingName("Morning Session")
                .trainingDate(LocalDate.of(2024, 3, 10))
                .trainingDuration(90)
                .build();

        List<Training> trainings = List.of(new Training());

        when(trainingService.getTrainerTrainingsByCriteria(
                eq(USERNAME), isNull(), isNull(), isNull()))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings))
                .thenReturn(List.of(trainingResponse));

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Morning Session"))
                .andExpect(jsonPath("$[0].trainingDuration").value(90));

        verify(trainingService).getTrainerTrainingsByCriteria(
                USERNAME, null, null, null);
    }

    @Test
    void getTrainerTrainings_WithAllFilters_ShouldPassFiltersToService() throws Exception {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String traineeName = "john.doe";

        TrainingResponse trainingResponse = TrainingResponse.builder()
                .trainingName("Filtered Session")
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(60)
                .build();

        List<Training> trainings = List.of(new Training());

        when(trainingService.getTrainerTrainingsByCriteria(
                eq(USERNAME), eq(fromDate), eq(toDate), eq(traineeName)))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings))
                .thenReturn(List.of(trainingResponse));

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME)
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-12-31")
                        .param("traineeName", traineeName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Filtered Session"));

        verify(trainingService).getTrainerTrainingsByCriteria(
                USERNAME, fromDate, toDate, traineeName);
    }

    @Test
    void getTrainerTrainings_WithDateRangeOnly_ShouldReturnFilteredTrainings() throws Exception {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 6, 30);

        List<Training> trainings = List.of(new Training(), new Training());
        List<TrainingResponse> trainingResponses = List.of(
                TrainingResponse.builder()
                        .trainingName("Session A")
                        .trainingDate(LocalDate.of(2024, 2, 10))
                        .trainingDuration(45)
                        .build(),
                TrainingResponse.builder()
                        .trainingName("Session B")
                        .trainingDate(LocalDate.of(2024, 5, 20))
                        .trainingDuration(60)
                        .build()
        );

        when(trainingService.getTrainerTrainingsByCriteria(
                eq(USERNAME), eq(fromDate), eq(toDate), isNull()))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings))
                .thenReturn(trainingResponses);

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME)
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trainingName").value("Session A"))
                .andExpect(jsonPath("$[1].trainingName").value("Session B"));
    }

    @Test
    void getTrainerTrainings_EmptyResult_ShouldReturnEmptyList() throws Exception {
        when(trainingService.getTrainerTrainingsByCriteria(
                eq(USERNAME), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toResponseList(List.of()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainers/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}