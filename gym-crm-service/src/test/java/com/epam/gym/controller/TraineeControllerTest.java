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
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.mapper.TraineeMapper;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.epam.gym.security.JwtProvider;
import com.epam.gym.security.LoginAttemptService;
import com.epam.gym.security.TokenBlacklistService;
import com.epam.gym.security.UserDetailsServiceImpl;
import com.epam.gym.service.TraineeService;
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

@WebMvcTest(TraineeController.class)
@AutoConfigureMockMvc(addFilters = false)
class TraineeControllerTest {

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
    private TraineeService traineeService;

    @MockitoBean
    private TrainingService trainingService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TraineeMapper traineeMapper;

    @MockitoBean
    private TrainerMapper trainerMapper;

    @MockitoBean
    private TrainingMapper trainingMapper;

    private static final String USERNAME = "john.doe";

    private Trainee trainee;
    private TraineeProfileResponse profileResponse;
    private RegistrationResponse registrationResponse;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();

        profileResponse = TraineeProfileResponse.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .isActive(true)
                .trainers(List.of())
                .build();

        registrationResponse = RegistrationResponse.builder()
                .username(USERNAME)
                .password("generatedPass123")
                .build();
    }

    // ==================== POST /api/trainees ====================

    @Test
    void registerTrainee_ValidRequest_ShouldReturnCreated() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("123 Main St");

        when(traineeService.createProfile(any(TraineeRegistrationRequest.class)))
                .thenReturn(registrationResponse);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andExpect(jsonPath("$.password").value("generatedPass123"));

        verify(traineeService).createProfile(any(TraineeRegistrationRequest.class));
    }

    @Test
    void registerTrainee_MissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        // firstName and lastName intentionally missing

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    // ==================== GET /api/trainees/{username} ====================

    @Test
    void getTraineeProfile_ExistingUser_ShouldReturnProfile() throws Exception {
        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeService.getByUsername(USERNAME)).thenReturn(trainee);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/trainees/{username}", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.isActive").value(true));

        verify(userService).isAuthenticated(USERNAME);
        verify(traineeService).getByUsername(USERNAME);
        verify(traineeMapper).toProfileResponse(trainee);
    }

    // ==================== PUT /api/trainees/{username} ====================

    @Test
    void updateTraineeProfile_ValidRequest_ShouldReturnUpdatedProfile() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Updated");
        request.setIsActive(true);

        TraineeProfileResponse updatedResponse = TraineeProfileResponse.builder()
                .firstName("John")
                .lastName("Updated")
                .isActive(true)
                .trainers(List.of())
                .build();

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeService.updateProfile(eq(USERNAME), any(UpdateTraineeRequest.class)))
                .thenReturn(trainee);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/trainees/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Updated"));

        verify(userService).isAuthenticated(USERNAME);
        verify(traineeService).updateProfile(eq(USERNAME), any(UpdateTraineeRequest.class));
    }

    @Test
    void updateTraineeProfile_ServiceThrowsException_ShouldReturnErrorStatus() throws Exception {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        // ✅ Set username to pass @Valid, then let service fail
        request.setUsername(USERNAME);
        request.setFirstName("John");
        request.setLastName("Updated");
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        when(traineeService.updateProfile(eq(USERNAME), any(UpdateTraineeRequest.class)))
                .thenThrow(new RuntimeException("Trainee not found"));

        mockMvc.perform(put("/api/trainees/{username}", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ==================== DELETE /api/trainees/{username} ====================

    @Test
    void deleteTraineeProfile_ExistingUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).isAuthenticated(USERNAME);
        doNothing().when(traineeService).deleteByUsername(USERNAME);

        mockMvc.perform(delete("/api/trainees/{username}", USERNAME))
                .andExpect(status().isNoContent());

        verify(userService).isAuthenticated(USERNAME);
        verify(traineeService).deleteByUsername(USERNAME);
    }

    @Test
    void deleteTraineeProfile_ServiceThrowsException_ShouldReturnErrorStatus() throws Exception {
        doNothing().when(userService).isAuthenticated(USERNAME);
        doThrow(new RuntimeException("Trainee not found"))
                .when(traineeService).deleteByUsername(USERNAME);

        mockMvc.perform(delete("/api/trainees/{username}", USERNAME))
                .andExpect(status().isInternalServerError());
    }

    // ==================== PATCH /api/trainees/{username}/status ====================

    @Test
    void toggleTraineeStatus_Activate_ShouldReturnOk() throws Exception {
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(USERNAME);
        request.setIsActive(true);

        doNothing().when(userService).isAuthenticated(USERNAME);
        doNothing().when(userService).setActiveStatus(USERNAME, true);

        mockMvc.perform(patch("/api/trainees/{username}/status", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).isAuthenticated(USERNAME);
        verify(userService).setActiveStatus(USERNAME, true);
    }

    @Test
    void toggleTraineeStatus_Deactivate_ShouldReturnOk() throws Exception {
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(USERNAME);
        request.setIsActive(false);

        doNothing().when(userService).isAuthenticated(USERNAME);
        doNothing().when(userService).setActiveStatus(USERNAME, false);

        mockMvc.perform(patch("/api/trainees/{username}/status", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).setActiveStatus(USERNAME, false);
    }

    @Test
    void toggleTraineeStatus_MissingUsername_ShouldReturnBadRequest() throws Exception {
        ToggleActiveRequest request = new ToggleActiveRequest();
        // username intentionally missing to trigger @NotBlank
        request.setIsActive(true);

        mockMvc.perform(patch("/api/trainees/{username}/status", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Username is required")
                ));

        verifyNoInteractions(userService);
    }

    // ==================== PUT /api/trainees/{username}/trainers ====================

    @Test
    void updateTraineeTrainersList_ValidRequest_ShouldReturnTrainerList() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername(USERNAME);
        request.setTrainerUsernames(List.of("trainer1", "trainer2"));

        TrainerSummaryResponse trainerSummary1 = TrainerSummaryResponse.builder()
                .username("trainer1")
                .firstName("Trainer")
                .lastName("One")
                .build();

        TrainerSummaryResponse trainerSummary2 = TrainerSummaryResponse.builder()
                .username("trainer2")
                .firstName("Trainer")
                .lastName("Two")
                .build();

        List<Trainer> trainers = List.of(new Trainer(), new Trainer());

        when(traineeService.updateTrainersList(eq(USERNAME), anyList()))
                .thenReturn(trainers);
        when(trainerMapper.toSummaryResponseList(trainers))
                .thenReturn(List.of(trainerSummary1, trainerSummary2));

        mockMvc.perform(put("/api/trainees/{username}/trainers", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("trainer1"))
                .andExpect(jsonPath("$[1].username").value("trainer2"));

        verify(traineeService).updateTrainersList(USERNAME, List.of("trainer1", "trainer2"));
    }

    @Test
    void updateTraineeTrainersList_UsernameMismatch_ShouldReturnBadRequest() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername("different.user");
        request.setTrainerUsernames(List.of("trainer1"));

        mockMvc.perform(put("/api/trainees/{username}/trainers", USERNAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
    }

    // ==================== GET /api/trainees/{username}/trainings ====================

    @Test
    void getTraineeTrainings_NoFilters_ShouldReturnAllTrainings() throws Exception {
        TrainingResponse trainingResponse = TrainingResponse.builder()
                .trainingName("Morning Yoga")
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDuration(60)
                .build();

        List<Training> trainings = List.of(new Training());

        when(trainingService.getTraineeTrainingsByCriteria(
                eq(USERNAME), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings))
                .thenReturn(List.of(trainingResponse));

        mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60));

        verify(trainingService).getTraineeTrainingsByCriteria(
                USERNAME, null, null, null, null);
    }

    @Test
    void getTraineeTrainings_WithAllFilters_ShouldPassFiltersToService() throws Exception {
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String trainerName = "trainer1";
        TrainingTypeName trainingType = TrainingTypeName.YOGA;

        List<Training> trainings = List.of(new Training());
        TrainingResponse trainingResponse = TrainingResponse.builder()
                .trainingName("Yoga Session")
                .trainingDate(LocalDate.of(2024, 6, 15))
                .trainingDuration(45)
                .build();

        when(trainingService.getTraineeTrainingsByCriteria(
                eq(USERNAME), eq(fromDate), eq(toDate), eq(trainerName), eq(trainingType)))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings))
                .thenReturn(List.of(trainingResponse));

        mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME)
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-12-31")
                        .param("trainerName", trainerName)
                        .param("trainingType", trainingType.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingName").value("Yoga Session"));

        verify(trainingService).getTraineeTrainingsByCriteria(
                USERNAME, fromDate, toDate, trainerName, trainingType);
    }

    @Test
    void getTraineeTrainings_EmptyResult_ShouldReturnEmptyList() throws Exception {
        when(trainingService.getTraineeTrainingsByCriteria(
                eq(USERNAME), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(trainingMapper.toResponseList(List.of()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/trainees/{username}/trainings", USERNAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}