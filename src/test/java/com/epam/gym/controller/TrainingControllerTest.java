package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.dto.response.TrainingTypeResponse;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.mapper.TrainingTypeMapper;
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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrainingController Unit Tests")
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingController trainingController;

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINEE_PASSWORD = "traineePass123";
    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_PASSWORD = "trainerPass123";
    private static final String TRAINING_NAME = "Morning Workout";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 15);
    private static final Integer TRAINING_DURATION = 60;

    @Nested
    @DisplayName("Add Training Tests")
    class AddTrainingTests {

        private AddTrainingRequest addTrainingRequest;

        @BeforeEach
        void setUp() {
            addTrainingRequest = createAddTrainingRequest();
        }

        @Test
        @DisplayName("Should add training successfully and return OK status")
        void addTraining_ValidRequest_ReturnsOkStatus() {
            doNothing().when(trainingService).createTraining(addTrainingRequest);

            ResponseEntity<Void> response = trainingController.addTraining(addTrainingRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNull();

            verify(trainingService).createTraining(addTrainingRequest);
            verifyNoMoreInteractions(trainingService);
            verifyNoInteractions(trainingTypeMapper);
        }

        @Test
        @DisplayName("Should pass correct request to service")
        void addTraining_ValidRequest_PassesCorrectRequestToService() {
            doNothing().when(trainingService).createTraining(any(AddTrainingRequest.class));

            trainingController.addTraining(addTrainingRequest);

            verify(trainingService).createTraining(addTrainingRequest);
        }

        @Test
        @DisplayName("Should add training with all fields populated")
        void addTraining_AllFieldsPopulated_ReturnsOkStatus() {
            AddTrainingRequest fullRequest = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .traineePassword(TRAINEE_PASSWORD)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainerPassword(TRAINER_PASSWORD)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(fullRequest);

            ResponseEntity<Void> response = trainingController.addTraining(fullRequest);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(fullRequest);
        }

        @Test
        @DisplayName("Should add training with minimum duration")
        void addTraining_MinimumDuration_ReturnsOkStatus() {
            AddTrainingRequest requestWithMinDuration = createAddTrainingRequestWithDuration(1);

            doNothing().when(trainingService).createTraining(requestWithMinDuration);

            ResponseEntity<Void> response = trainingController.addTraining(requestWithMinDuration);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(requestWithMinDuration);
        }

        @Test
        @DisplayName("Should add training with long duration")
        void addTraining_LongDuration_ReturnsOkStatus() {
            AddTrainingRequest requestWithLongDuration = createAddTrainingRequestWithDuration(180);

            doNothing().when(trainingService).createTraining(requestWithLongDuration);

            ResponseEntity<Void> response = trainingController.addTraining(requestWithLongDuration);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(requestWithLongDuration);
        }

        @Test
        @DisplayName("Should add training with future date")
        void addTraining_FutureDate_ReturnsOkStatus() {
            AddTrainingRequest requestWithFutureDate = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .traineePassword(TRAINEE_PASSWORD)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainerPassword(TRAINER_PASSWORD)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.now().plusDays(30))
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(requestWithFutureDate);

            ResponseEntity<Void> response = trainingController.addTraining(requestWithFutureDate);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(requestWithFutureDate);
        }

        @Test
        @DisplayName("Should add training with today's date")
        void addTraining_TodayDate_ReturnsOkStatus() {
            AddTrainingRequest requestWithTodayDate = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .traineePassword(TRAINEE_PASSWORD)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainerPassword(TRAINER_PASSWORD)
                    .trainingName(TRAINING_NAME)
                    .trainingDate(LocalDate.now())
                    .trainingDuration(TRAINING_DURATION)
                    .build();

            doNothing().when(trainingService).createTraining(requestWithTodayDate);

            ResponseEntity<Void> response = trainingController.addTraining(requestWithTodayDate);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(requestWithTodayDate);
        }

        @Test
        @DisplayName("Should add training with different trainee and trainer")
        void addTraining_DifferentUsers_ReturnsOkStatus() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername("bob.wilson")
                    .traineePassword("bobPass123")
                    .trainerUsername("charlie.brown")
                    .trainerPassword("charliePass123")
                    .trainingName("Evening Session")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(45)
                    .build();

            doNothing().when(trainingService).createTraining(request);

            ResponseEntity<Void> response = trainingController.addTraining(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(request);
        }

        @Test
        @DisplayName("Should add training with various training names")
        void addTraining_VariousTrainingNames_ReturnsOkStatus() {
            AddTrainingRequest request = AddTrainingRequest.builder()
                    .traineeUsername(TRAINEE_USERNAME)
                    .traineePassword(TRAINEE_PASSWORD)
                    .trainerUsername(TRAINER_USERNAME)
                    .trainerPassword(TRAINER_PASSWORD)
                    .trainingName("Advanced Yoga Session - Level 3")
                    .trainingDate(TRAINING_DATE)
                    .trainingDuration(90)
                    .build();

            doNothing().when(trainingService).createTraining(request);

            ResponseEntity<Void> response = trainingController.addTraining(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(trainingService).createTraining(request);
        }
    }

    @Nested
    @DisplayName("Get Training Types Tests")
    class GetTrainingTypesTests {

        @Test
        @DisplayName("Should get all training types successfully")
        void getTrainingTypes_TypesExist_ReturnsTypesList() {
            List<TrainingType> trainingTypes = List.of(
                    new TrainingType(1L, TrainingTypeName.FITNESS),
                    new TrainingType(2L, TrainingTypeName.YOGA),
                    new TrainingType(3L, TrainingTypeName.CARDIO)
            );

            List<TrainingTypeResponse> trainingTypeResponses = List.of(
                    createTrainingTypeResponse(1L, TrainingTypeName.FITNESS),
                    createTrainingTypeResponse(2L, TrainingTypeName.YOGA),
                    createTrainingTypeResponse(3L, TrainingTypeName.CARDIO)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody().get(0).getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getBody().get(1).getTrainingTypeName()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(response.getBody().get(2).getTrainingTypeName()).isEqualTo(TrainingTypeName.CARDIO);

            verify(trainingService).getAllTrainingTypes();
            verify(trainingTypeMapper).toResponseList(trainingTypes);
            verifyNoMoreInteractions(trainingService, trainingTypeMapper);
        }

        @Test
        @DisplayName("Should return empty list when no training types exist")
        void getTrainingTypes_NoTypesExist_ReturnsEmptyList() {
            when(trainingService.getAllTrainingTypes()).thenReturn(Collections.emptyList());
            when(trainingTypeMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).isEmpty();

            verify(trainingService).getAllTrainingTypes();
            verify(trainingTypeMapper).toResponseList(Collections.emptyList());
        }

        @Test
        @DisplayName("Should return single training type")
        void getTrainingTypes_SingleType_ReturnsSingleTypeList() {
            List<TrainingType> trainingTypes = List.of(
                    new TrainingType(1L, TrainingTypeName.FITNESS)
            );

            List<TrainingTypeResponse> trainingTypeResponses = List.of(
                    createTrainingTypeResponse(1L, TrainingTypeName.FITNESS)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(1);
            assertThat(response.getBody().getFirst().getId()).isEqualTo(1L);
            assertThat(response.getBody().getFirst().getTrainingTypeName()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should return all available training types")
        void getTrainingTypes_AllTypes_ReturnsAllTypes() {
            List<TrainingType> trainingTypes = List.of(
                    new TrainingType(1L, TrainingTypeName.FITNESS),
                    new TrainingType(2L, TrainingTypeName.YOGA),
                    new TrainingType(3L, TrainingTypeName.CARDIO),
                    new TrainingType(4L, TrainingTypeName.STRENGTH),
                    new TrainingType(5L, TrainingTypeName.PILATES),
                    new TrainingType(6L, TrainingTypeName.CROSSFIT),
                    new TrainingType(7L, TrainingTypeName.ZUMBA),
                    new TrainingType(8L, TrainingTypeName.FLEXIBILITY)
            );

            List<TrainingTypeResponse> trainingTypeResponses = List.of(
                    createTrainingTypeResponse(1L, TrainingTypeName.FITNESS),
                    createTrainingTypeResponse(2L, TrainingTypeName.YOGA),
                    createTrainingTypeResponse(3L, TrainingTypeName.CARDIO),
                    createTrainingTypeResponse(4L, TrainingTypeName.STRENGTH),
                    createTrainingTypeResponse(5L, TrainingTypeName.PILATES),
                    createTrainingTypeResponse(6L, TrainingTypeName.CROSSFIT),
                    createTrainingTypeResponse(7L, TrainingTypeName.ZUMBA),
                    createTrainingTypeResponse(8L, TrainingTypeName.FLEXIBILITY)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(8);

            assertThat(response.getBody())
                    .extracting(TrainingTypeResponse::getTrainingTypeName)
                    .containsExactly(
                            TrainingTypeName.FITNESS,
                            TrainingTypeName.YOGA,
                            TrainingTypeName.CARDIO,
                            TrainingTypeName.STRENGTH,
                            TrainingTypeName.PILATES,
                            TrainingTypeName.CROSSFIT,
                            TrainingTypeName.ZUMBA,
                            TrainingTypeName.FLEXIBILITY
                    );
        }

        @Test
        @DisplayName("Should return training types with correct IDs")
        void getTrainingTypes_VerifyIds_ReturnsCorrectIds() {
            List<TrainingType> trainingTypes = List.of(
                    new TrainingType(10L, TrainingTypeName.FITNESS),
                    new TrainingType(20L, TrainingTypeName.YOGA)
            );

            List<TrainingTypeResponse> trainingTypeResponses = List.of(
                    createTrainingTypeResponse(10L, TrainingTypeName.FITNESS),
                    createTrainingTypeResponse(20L, TrainingTypeName.YOGA)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody())
                    .extracting(TrainingTypeResponse::getId)
                    .containsExactly(10L, 20L);
        }

        @Test
        @DisplayName("Should call service and mapper in correct order")
        void getTrainingTypes_VerifyCallOrder_CallsServiceThenMapper() {
            List<TrainingType> trainingTypes = List.of(new TrainingType(1L, TrainingTypeName.FITNESS));
            List<TrainingTypeResponse> responses = List.of(createTrainingTypeResponse(1L, TrainingTypeName.FITNESS));

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(responses);

            trainingController.getTrainingTypes();

            verify(trainingService).getAllTrainingTypes();
            verify(trainingTypeMapper).toResponseList(trainingTypes);
        }

        @Test
        @DisplayName("Should return training types for specific workout categories")
        void getTrainingTypes_SpecificCategories_ReturnsCorrectTypes() {
            List<TrainingType> trainingTypes = List.of(
                    new TrainingType(1L, TrainingTypeName.YOGA),
                    new TrainingType(2L, TrainingTypeName.PILATES),
                    new TrainingType(3L, TrainingTypeName.FLEXIBILITY)
            );

            List<TrainingTypeResponse> trainingTypeResponses = List.of(
                    createTrainingTypeResponse(1L, TrainingTypeName.YOGA),
                    createTrainingTypeResponse(2L, TrainingTypeName.PILATES),
                    createTrainingTypeResponse(3L, TrainingTypeName.FLEXIBILITY)
            );

            when(trainingService.getAllTrainingTypes()).thenReturn(trainingTypes);
            when(trainingTypeMapper.toResponseList(trainingTypes)).thenReturn(trainingTypeResponses);

            ResponseEntity<List<TrainingTypeResponse>> response = trainingController.getTrainingTypes();

            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).hasSize(3);
            assertThat(response.getBody())
                    .extracting(TrainingTypeResponse::getTrainingTypeName)
                    .containsExactly(TrainingTypeName.YOGA, TrainingTypeName.PILATES, TrainingTypeName.FLEXIBILITY);
        }
    }

    // ==================== Helper Methods ====================

    private AddTrainingRequest createAddTrainingRequest() {
        return AddTrainingRequest.builder()
                .traineeUsername(TRAINEE_USERNAME)
                .traineePassword(TRAINEE_PASSWORD)
                .trainerUsername(TRAINER_USERNAME)
                .trainerPassword(TRAINER_PASSWORD)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(TRAINING_DURATION)
                .build();
    }

    private AddTrainingRequest createAddTrainingRequestWithDuration(Integer duration) {
        return AddTrainingRequest.builder()
                .traineeUsername(TRAINEE_USERNAME)
                .traineePassword(TRAINEE_PASSWORD)
                .trainerUsername(TRAINER_USERNAME)
                .trainerPassword(TRAINER_PASSWORD)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDuration(duration)
                .build();
    }

    private TrainingTypeResponse createTrainingTypeResponse(Long id, TrainingTypeName typeName) {
        return TrainingTypeResponse.builder()
                .id(id)
                .trainingTypeName(typeName)
                .build();
    }
}