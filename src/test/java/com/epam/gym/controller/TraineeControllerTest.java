package com.epam.gym.controller;

import com.epam.gym.dto.request.*;
import com.epam.gym.dto.response.*;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.mapper.TraineeMapper;
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
class TraineeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TraineeController traineeController;

    @BeforeEach
    void setUp() {
        // TODO:
        //  [Optional]
        //  standaloneSetup(...) is okay for narrow controller-unit tests, but it does not reproduce the real MVC configuration.
        //  So by default you can miss things like:
        //        @ControllerAdvice
        //        servlet filters
        //        interceptors
        //        custom argument resolvers
        //        message converters
        //        formatter/conversion setup
        //        validation behavior differences
        //        security chain
        //  Even if all above are tested separately, standaloneSetup does not verify that everything is actually wired
        //  together correctly and how they interact with each other in the real MVC pipeline
        //  Consider using @WebMvcTest(TraineeController.class) to verify actual entire web-layer behavior.
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Register trainee should return 201 with credentials")
    void registerTraineeShouldReturnCreatedWithCredentials() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("123 Main St");

        User user = User.builder()
                .username("john.doe")
                .password("randomPass123")
                .build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(gymFacade.createTrainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St"))
                .thenReturn(trainee);

        mockMvc.perform(post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("randomPass123"));

        verify(gymFacade).createTrainee("John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St");
    }

    // ==================== Profile Retrieval Tests ====================

    @Test
    @DisplayName("Get trainee profile should return profile when authenticated")
    void getTraineeProfileShouldReturnProfileWhenAuthenticated() throws Exception {
        String username = "john.doe";
        String password = "pass123";
        Trainee trainee = createSampleTrainee(username);
        TraineeProfileResponse profileResponse = createSampleProfileResponse();

        doNothing().when(gymFacade).authenticateTrainee(username, password);
        when(gymFacade.getTraineeByUsername(username)).thenReturn(trainee);
        when(traineeMapper.toProfileResponse(trainee)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/trainees/{username}", username)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateTrainee(username, password);
        verify(gymFacade).getTraineeByUsername(username);
        verify(traineeMapper).toProfileResponse(trainee);
    }

    // ==================== Profile Update Tests ====================

    @Test
    @DisplayName("Update trainee profile should succeed when usernames match")
    void updateTraineeProfileShouldSucceedWhenUsernamesMatch() throws Exception {
        String username = "john.doe";
        UpdateTraineeRequest request = createValidUpdateRequest(username);

        Trainee updatedTrainee = createSampleTrainee(username);
        TraineeProfileResponse response = createSampleProfileResponse();

        when(gymFacade.updateTrainee(eq(username), eq("pass123"), any(Trainee.class)))
                .thenReturn(updatedTrainee);
        when(traineeMapper.toProfileResponse(updatedTrainee)).thenReturn(response);

        mockMvc.perform(put("/api/trainees/{username}", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).updateTrainee(eq(username), eq("pass123"), any(Trainee.class));
    }

    @Test
    @DisplayName("Update trainee profile should throw ValidationException when usernames mismatch")
    void updateTraineeProfileShouldThrowWhenUsernamesMismatch() {
        String pathUsername = "john.doe";
        String bodyUsername = "jane.doe";
        UpdateTraineeRequest request = createValidUpdateRequest(bodyUsername); // Mismatched

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/trainees/{username}", pathUsername)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected ValidationException in exception chain");
        assertEquals("Username in path does not match username in request body", rootCause.getMessage());
    }

    // ==================== Delete Profile Tests ====================

    @Test
    @DisplayName("Delete trainee profile should return 204 when successful")
    void deleteTraineeProfileShouldReturnNoContent() throws Exception {
        String username = "john.doe";
        String password = "pass123";

        doNothing().when(gymFacade).deleteTrainee(username, password);

        mockMvc.perform(delete("/api/trainees/{username}", username)
                        .param("password", password))
                .andExpect(status().isNoContent());

        verify(gymFacade).deleteTrainee(username, password);
    }

    // ==================== Unassigned Trainers Tests ====================

    @Test
    @DisplayName("Get unassigned trainers should return list of trainers")
    void getUnassignedTrainersShouldReturnTrainerList() throws Exception {
        String username = "john.doe";
        String password = "pass123";
        List<Trainer> trainers = Collections.singletonList(createSampleTrainer());
        List<TrainerSummaryResponse> responseList = Collections.singletonList(new TrainerSummaryResponse());

        doNothing().when(gymFacade).authenticateTrainee(username, password);
        when(gymFacade.getUnassignedTrainers(username)).thenReturn(trainers);
        when(trainerMapper.toSummaryResponseList(trainers)).thenReturn(responseList);

        mockMvc.perform(get("/api/trainees/{username}/trainers/unassigned", username)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(gymFacade).authenticateTrainee(username, password);
        verify(gymFacade).getUnassignedTrainers(username);
        verify(trainerMapper).toSummaryResponseList(trainers);
    }

    // ==================== Update Trainers List Tests ====================

    @Test
    @DisplayName("Update trainee trainers list should succeed when usernames match")
    void updateTraineeTrainersListShouldSucceedWhenUsernamesMatch() throws Exception {
        String username = "john.doe";
        List<String> trainerUsernames = List.of("trainer1", "trainer2");
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername(username);
        request.setPassword("pass123");
        request.setTrainerUsernames(trainerUsernames);

        Trainee trainee = createSampleTrainee(username);
        List<TrainerSummaryResponse> responseList = Collections.emptyList();

        doNothing().when(gymFacade).updateTraineeTrainersList(username, "pass123", trainerUsernames);
        when(gymFacade.getTraineeByUsername(username)).thenReturn(trainee);
        when(trainerMapper.toSummaryResponseList(anyList())).thenReturn(responseList);

        mockMvc.perform(put("/api/trainees/{username}/trainers", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).updateTraineeTrainersList(username, "pass123", trainerUsernames);
    }

    @Test
    @DisplayName("Update trainers list should throw ValidationException when usernames mismatch")
    void updateTrainersListShouldThrowWhenUsernamesMismatch() {
        String pathUsername = "john.doe";
        String bodyUsername = "jane.doe";
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest();
        request.setTraineeUsername(bodyUsername);
        request.setPassword("pass123");
        request.setTrainerUsernames(List.of("trainer1"));

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/trainees/{username}/trainers", pathUsername)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected ValidationException in exception chain");
        assertEquals("Username in path does not match username in request body", rootCause.getMessage());
    }

    // ==================== Get Trainings Tests ====================

    @Test
    @DisplayName("Get trainee trainings without filters should return all trainings")
    void getTraineeTrainingsWithoutFiltersShouldReturnAll() throws Exception {
        String username = "john.doe";
        String password = "pass123";
        List<Training> trainings = Collections.singletonList(createSampleTraining());
        List<TrainingResponse> responseList = Collections.singletonList(new TrainingResponse());

        when(gymFacade.getTraineeTrainingsByCriteria(username, password, null, null, null, null))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(responseList);

        mockMvc.perform(get("/api/trainees/{username}/trainings", username)
                        .param("password", password))
                .andExpect(status().isOk());

        verify(gymFacade).getTraineeTrainingsByCriteria(username, password, null, null, null, null);
    }

    @Test
    @DisplayName("Get trainee trainings with all filters should return filtered results")
    void getTraineeTrainingsWithFiltersShouldReturnFiltered() throws Exception {
        String username = "john.doe";
        String password = "pass123";
        LocalDate fromDate = LocalDate.of(2024, 1, 1);
        LocalDate toDate = LocalDate.of(2024, 12, 31);
        String trainerName = "Jane Smith";
        TrainingTypeName trainingType = TrainingTypeName.YOGA;

        List<Training> trainings = Collections.emptyList();
        List<TrainingResponse> responseList = Collections.emptyList();

        when(gymFacade.getTraineeTrainingsByCriteria(username, password, fromDate, toDate, trainerName, trainingType))
                .thenReturn(trainings);
        when(trainingMapper.toResponseList(trainings)).thenReturn(responseList);

        mockMvc.perform(get("/api/trainees/{username}/trainings", username)
                        .param("password", password)
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString())
                        .param("trainerName", trainerName)
                        .param("trainingType", trainingType.name()))
                .andExpect(status().isOk());

        verify(gymFacade).getTraineeTrainingsByCriteria(username, password, fromDate, toDate, trainerName, trainingType);
    }

    // ==================== Toggle Status Tests ====================

    @Test
    @DisplayName("Toggle trainee status should succeed when usernames match")
    void toggleTraineeStatusShouldSucceedWhenUsernamesMatch() throws Exception {
        String username = "john.doe";
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(username);
        request.setPassword("pass123");

        doNothing().when(gymFacade).toggleTraineeStatus(username, "pass123");

        mockMvc.perform(patch("/api/trainees/{username}/status", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).toggleTraineeStatus(username, "pass123");
    }

    @Test
    @DisplayName("Toggle status should throw ValidationException when usernames mismatch")
    void toggleStatusShouldThrowWhenUsernamesMismatch() {
        String pathUsername = "john.doe";
        String bodyUsername = "jane.doe";
        ToggleActiveRequest request = new ToggleActiveRequest();
        request.setUsername(bodyUsername);
        request.setPassword("pass123");

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(patch("/api/trainees/{username}/status", pathUsername)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause, "Expected ValidationException in exception chain");
        assertEquals("Username in path does not match username in request body", rootCause.getMessage());
    }

    // ==================== Helper Methods ====================

    private UpdateTraineeRequest createValidUpdateRequest(String username) {
        UpdateTraineeRequest request = new UpdateTraineeRequest();
        request.setUsername(username);
        request.setPassword("pass123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setAddress("123 Main St");
        return request;
    }

    private Trainee createSampleTrainee(String username) {
        User user = User.builder()
                .username(username)
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
        return Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();
    }

    private TraineeProfileResponse createSampleProfileResponse() {
        TraineeProfileResponse response = new TraineeProfileResponse();
        response.setUsername("john.doe");
        response.setFirstName("John");
        response.setLastName("Doe");
        return response;
    }

    private Trainer createSampleTrainer() {
        User user = User.builder()
                .username("trainer1")
                .firstName("Jane")
                .lastName("Smith")
                .build();
        return Trainer.builder().user(user).build();
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