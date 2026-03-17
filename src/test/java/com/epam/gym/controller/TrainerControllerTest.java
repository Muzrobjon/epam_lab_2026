package com.epam.gym.controller;

import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.dto.request.TrainerRegistrationRequest;
import com.epam.gym.dto.request.UpdateTrainerRequest;
import com.epam.gym.dto.response.TrainerProfileResponse;
import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.mapper.TrainerMapper;
import com.epam.gym.mapper.TrainingMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Register trainer should return 201 with credentials")
    void registerTrainerShouldReturnCreatedWithCredentials() throws Exception {
        // Given
        TrainerRegistrationRequest request = new TrainerRegistrationRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setSpecialization(TrainingTypeName.YOGA);

        User user = User.builder()
                .username("jane.smith")
                .password("randomPass123")
                .build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(gymFacade.createTrainer("Jane", "Smith", TrainingTypeName.YOGA))
                .thenReturn(trainer);

        // When & Then
        mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("jane.smith"))
                .andExpect(jsonPath("$.password").value("randomPass123"));

        verify(gymFacade).createTrainer("Jane", "Smith", TrainingTypeName.YOGA);
    }

    // ==================== Profile Retrieval Tests ====================

    @Test
    @DisplayName("Get trainer profile should return profile when authenticated")
    void getTrainerProfileShouldReturnProfileWhenAuthenticated() throws Exception {
        // Given
        String username = "jane.smith";
        String password = "pass123";
        Trainer trainer = createSampleTrainer(username);
        TrainerProfileResponse profileResponse = createSampleProfileResponse();

        doNothing().when(gymFacade).authenticateTrainer(username, password);
        when(gymFacade.getTrainerByUsername(username)).thenReturn(trainer);
        when(trainerMapper.toProfileResponse(trainer)).thenReturn(profileResponse);

        // When & Then
        mockMvc.perform(get("/api/trainers/{username}", username)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateTrainer(username, password);
        verify(gymFacade).getTrainerByUsername(username);
        verify(trainerMapper).toProfileResponse(trainer);
    }

    // ==================== Profile Update Tests ====================

    @Test
    @DisplayName("Update trainer profile should succeed when usernames match")
    void updateTrainerProfileShouldSucceedWhenUsernamesMatch() throws Exception {
        // Given
        String username = "jane.smith";
        UpdateTrainerRequest request = createValidUpdateRequest(username);

        Trainer updatedTrainer = createSampleTrainer(username);
        TrainerProfileResponse response = createSampleProfileResponse();

        when(gymFacade.updateTrainer(eq(username), eq("pass123"), any(Trainer.class)))
                .thenReturn(updatedTrainer);
        when(trainerMapper.toProfileResponse(updatedTrainer)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/trainers/{username}", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).updateTrainer(eq(username), eq("pass123"), any(Trainer.class));
    }

    @Test
    @DisplayName("Update trainer profile should throw ValidationException when usernames mismatch")
    void updateTrainerProfileShouldThrowWhenUsernamesMismatch() {
        // Given
        String pathUsername = "jane.smith";
        String bodyUsername = "john.doe";
        UpdateTrainerRequest request = createValidUpdateRequest(bodyUsername);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/trainers/{username}", pathUsername)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected ValidationException in exception chain");
        assertEquals("Username in path does not match username in request body", rootCause.getMessage());
    }

    // ==================== Get Trainings Tests ====================

    @Test
    @DisplayName("Get trainer trainings without filters should return all trainings")
    void getTrainerTrainingsWithoutFiltersShouldReturnAll() throws Exception {
        // Given
        String username = "jane.smith";
        String password = "pass123";
        List<Training> trainings = Collections.singletonList(createSampleTraining());
        List<TrainingResponse> responseList = Collections.singletonList(new TrainingResponse());

        when(gymFacade.getTrainerTrainingsByCriteria(username, password, null, null, null))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(responseList);

        // When & Then
        mockMvc.perform(get("/api/trainers/{username}/trainings", username)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(gymFacade).getTrainerTrainingsByCriteria(username, password, null, null, null);
    }

    @Test
    @DisplayName("Get trainer trainings with all filters should return filtered results")
    void getTrainerTrainingsWithFiltersShouldReturnFiltered() throws Exception {
        // Given
        String username = "jane.smith";
        String password = "pass123";
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String traineeName = "John Doe";

        List<Training> trainings = Collections.emptyList();
        List<TrainingResponse> responseList = Collections.emptyList();

        when(gymFacade.getTrainerTrainingsByCriteria(username, password, fromDate, toDate, traineeName))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(responseList);

        // When & Then
        mockMvc.perform(get("/api/trainers/{username}/trainings", username)
                        .param("password", password)
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString())
                        .param("traineeName", traineeName))
                .andExpect(status().isOk());

        verify(gymFacade).getTrainerTrainingsByCriteria(username, password, fromDate, toDate, traineeName);
    }

    // ==================== Toggle Status Tests ====================

    @Test
    @DisplayName("Toggle trainer status should succeed when usernames match")
    void toggleTrainerStatusShouldSucceedWhenUsernamesMatch() throws Exception {
        // Given
        String username = "jane.smith";
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(username);
        request.setPassword("pass123");

        doNothing().when(gymFacade).toggleTrainerStatus(username, "pass123");

        // When & Then
        mockMvc.perform(patch("/api/trainers/{username}/status", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).toggleTrainerStatus(username, "pass123");
    }

    @Test
    @DisplayName("Toggle status should throw ValidationException when usernames mismatch")
    void toggleStatusShouldThrowWhenUsernamesMismatch() {
        // Given
        String pathUsername = "jane.smith";
        String bodyUsername = "john.doe";
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(bodyUsername);
        request.setPassword("pass123");

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch("/api/trainers/{username}/status", pathUsername)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected ValidationException in exception chain");
        assertEquals("Username in path does not match username in request body", rootCause.getMessage());
    }

    // ==================== Helper Methods ====================

    private UpdateTrainerRequest createValidUpdateRequest(String username) {
        UpdateTrainerRequest request = new UpdateTrainerRequest();
        request.setUsername(username);
        request.setPassword("pass123");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setIsActive(true);
        return request;
    }

    private Trainer createSampleTrainer(String username) {
        User user = User.builder()
                .username(username)
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();
        return Trainer.builder()
                .user(user)
                .build();
    }

    private TrainerProfileResponse createSampleProfileResponse() {
        TrainerProfileResponse response = new TrainerProfileResponse();
        response.setUsername("jane.smith");
        response.setFirstName("Jane");
        response.setLastName("Smith");
        return response;
    }

    private Training createSampleTraining() {
        return Training.builder()
                .trainingName("Morning Yoga")
                .trainingDate(LocalDate.now())
                .trainingDurationMinutes(60)
                .build();
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (ValidationException.class.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}