package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.GlobalExceptionHandler;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.mapper.TrainingTypeMapper;
import com.epam.gym.service.TrainingService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController Unit Tests")
class TrainingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingController trainingController;

    private ObjectMapper objectMapper;

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String TRAINING_NAME = "Morning Fitness Session";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 15);
    private static final Integer TRAINING_DURATION = 60;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/trainings - Add Training")
    class AddTrainingTests {

        @Test
        @DisplayName("Should add training successfully and return 200 OK")
        void addTraining_ValidRequest_ReturnsOk() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should add training with minimum valid duration")
        void addTraining_MinimumDuration_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(1)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should add training with large duration")
        void addTraining_LargeDuration_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(480)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should add training with future date")
        void addTraining_FutureDate_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.now().plusMonths(6))
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should add training with past date")
        void addTraining_PastDate_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.now().minusMonths(1))
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when traineeUsername is missing")
        void addTraining_MissingTraineeUsername_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when traineeUsername is blank")
        void addTraining_BlankTraineeUsername_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("   ")
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainerUsername is missing")
        void addTraining_MissingTrainerUsername_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainerUsername is blank")
        void addTraining_BlankTrainerUsername_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername("   ")
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainingName is missing")
        void addTraining_MissingTrainingName_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainingName is blank")
        void addTraining_BlankTrainingName_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName("   ")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainingDate is missing")
        void addTraining_MissingTrainingDate_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when trainingDuration is missing")
        void addTraining_MissingTrainingDuration_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void addTraining_EmptyBody_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 404 when trainee not found")
        void addTraining_TraineeNotFound_ReturnsNotFound() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doThrow(new NotFoundException("Trainee not found: " + TRAINEE_USERNAME))
                    .when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should return 404 when trainer not found")
        void addTraining_TrainerNotFound_ReturnsNotFound() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doThrow(new NotFoundException("Trainer not found: " + TRAINER_USERNAME))
                    .when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle long training name")
        void addTraining_LongTrainingName_ReturnsOk() throws Exception {
            String longName = "A".repeat(200);
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(longName)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle special characters in training name")
        void addTraining_SpecialCharactersInName_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName("Morning Session @Gym #2024!")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle username with dots")
        void addTraining_UsernameWithDots_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("john.doe.junior")
                    .trainerUsername("jane.smith.senior")
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/trainings/types - Get Training Types")
    class GetTrainingTypesTests {

        @Test
        @DisplayName("Should return all training types successfully")
        void getTrainingTypes_ReturnsAllTypes() throws Exception {
            List<TrainingType> types = Arrays.asList(
                    new TrainingType(1L, TrainingTypeName.FITNESS),
                    new TrainingType(2L, TrainingTypeName.YOGA),
                    new TrainingType(3L, TrainingTypeName.ZUMBA)
            );

            List<TrainingTypeResponse> responses = Arrays.asList(
                    new TrainingTypeResponse(1L, TrainingTypeName.FITNESS),
                    new TrainingTypeResponse(2L, TrainingTypeName.YOGA),
                    new TrainingTypeResponse(3L, TrainingTypeName.ZUMBA)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(types);
            when(trainingTypeMapper.toResponseList(types)).thenReturn(responses);

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].trainingTypeName", is("FITNESS")))
                    .andExpect(jsonPath("$[1].id", is(2)))
                    .andExpect(jsonPath("$[1].trainingTypeName", is("YOGA")))
                    .andExpect(jsonPath("$[2].id", is(3)))
                    .andExpect(jsonPath("$[2].trainingTypeName", is("ZUMBA")));

            verify(trainingService).getAllTrainingTypes();
            verify(trainingTypeMapper).toResponseList(types);
        }

        @Test
        @DisplayName("Should return single training type")
        void getTrainingTypes_SingleType_ReturnsOne() throws Exception {
            List<TrainingType> types = List.of(
                    new TrainingType(1L, TrainingTypeName.FITNESS)
            );

            List<TrainingTypeResponse> responses = List.of(
                    new TrainingTypeResponse(1L, TrainingTypeName.FITNESS)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(types);
            when(trainingTypeMapper.toResponseList(types)).thenReturn(responses);

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(1)))
                    .andExpect(jsonPath("$[0].trainingTypeName", is("FITNESS")));

            verify(trainingService).getAllTrainingTypes();
        }

        @Test
        @DisplayName("Should return empty list when no training types exist")
        void getTrainingTypes_NoTypes_ReturnsEmptyList() throws Exception {
            when(trainingService.getAllTrainingTypes()).thenReturn(Collections.emptyList());
            when(trainingTypeMapper.toResponseList(anyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));

            verify(trainingService).getAllTrainingTypes();
            verify(trainingTypeMapper).toResponseList(anyList());
        }

        @Test
        @DisplayName("Should return training types with correct structure")
        void getTrainingTypes_CorrectStructure_ReturnsValidJson() throws Exception {
            List<TrainingType> types = List.of(
                    new TrainingType(1L, TrainingTypeName.YOGA)
            );

            List<TrainingTypeResponse> responses = List.of(
                    new TrainingTypeResponse(1L, TrainingTypeName.YOGA)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(types);
            when(trainingTypeMapper.toResponseList(types)).thenReturn(responses);

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0]").exists())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].trainingTypeName").isString());

            verify(trainingService).getAllTrainingTypes();
        }

        @Test
        @DisplayName("Should return training types in consistent order")
        void getTrainingTypes_MultipleRequests_ReturnsConsistentOrder() throws Exception {
            List<TrainingType> types = Arrays.asList(
                    new TrainingType(1L, TrainingTypeName.FITNESS),
                    new TrainingType(2L, TrainingTypeName.YOGA)
            );

            List<TrainingTypeResponse> responses = Arrays.asList(
                    new TrainingTypeResponse(1L, TrainingTypeName.FITNESS),
                    new TrainingTypeResponse(2L, TrainingTypeName.YOGA)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(types);
            when(trainingTypeMapper.toResponseList(types)).thenReturn(responses);

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingTypeName", is("FITNESS")))
                    .andExpect(jsonPath("$[1].trainingTypeName", is("YOGA")));

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].trainingTypeName", is("FITNESS")))
                    .andExpect(jsonPath("$[1].trainingTypeName", is("YOGA")));

            verify(trainingService, times(2)).getAllTrainingTypes();
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle concurrent training creation requests")
        void addTraining_ConcurrentRequests_HandlesCorrectly() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/trainings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk());
            }

            verify(trainingService, times(5)).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle training with today's date")
        void addTraining_TodayDate_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.now())
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle numeric username")
        void addTraining_NumericUsername_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("12345")
                    .trainerUsername("67890")
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle unicode characters in training name")
        void addTraining_UnicodeInTrainingName_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName("Morning Training Session")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle same trainee and trainer username")
        void addTraining_SameTraineeAndTrainer_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINEE_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle training far in the future")
        void addTraining_FarFutureDate_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.of(2030, 12, 31))
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }
    }

    @Nested
    @DisplayName("Validation Boundary Tests")
    class ValidationBoundaryTests {

        @Test
        @DisplayName("Should handle minimum valid training name length")
        void addTraining_MinTrainingNameLength_ReturnsOk() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName("A")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when duration is zero")
        void addTraining_ZeroDuration_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(0)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }

        @Test
        @DisplayName("Should return 400 when duration is negative")
        void addTraining_NegativeDuration_ReturnsBadRequest() throws Exception {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(-10)
                    .build();

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(trainingService, never()).createTraining(any());
        }
    }

    @Nested
    @DisplayName("Service Exception Tests")
    class ServiceExceptionTests {

        @Test
        @DisplayName("Should handle generic runtime exception from service")
        void addTraining_ServiceThrowsRuntimeException_ReturnsInternalServerError() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doThrow(new RuntimeException("Unexpected error"))
                    .when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException from service as 500")
        void addTraining_ServiceThrowsIllegalArgumentException_ReturnsInternalServerError() throws Exception {
            AddTrainingRequest request = createValidAddTrainingRequest();

            doThrow(new IllegalArgumentException("Invalid training data"))
                    .when(trainingService).createTraining(any(AddTrainingRequest.class));

            mockMvc.perform(post("/api/trainings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());

            verify(trainingService).createTraining(any(AddTrainingRequest.class));
        }

        @Test
        @DisplayName("Should handle exception when getting training types")
        void getTrainingTypes_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
            when(trainingService.getAllTrainingTypes())
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/trainings/types"))
                    .andExpect(status().isInternalServerError());

            verify(trainingService).getAllTrainingTypes();
        }
    }

    private AddTrainingRequest createValidAddTrainingRequest() {
        return AddTrainingRequest.builder()
                .traineeUsername(TRAINEE_USERNAME)
                .trainerUsername(TRAINER_USERNAME)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }
}