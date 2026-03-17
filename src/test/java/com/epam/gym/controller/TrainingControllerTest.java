package com.epam.gym.controller;

import com.epam.gym.dto.request.AddTrainingRequest;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.repository.TrainingTypeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
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
@MockitoSettings(strictness = Strictness.LENIENT)  // Allow lenient stubbing for helper methods
class TrainingControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GymFacade gymFacade;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== Add Training Tests ====================

    @Test
    @DisplayName("Add training should succeed with valid request")
    void addTrainingShouldSucceedWithValidRequest() throws Exception {
        AddTrainingRequest request = createValidAddTrainingRequest();
        Trainer trainer = createSampleTrainer(TrainingTypeName.YOGA);

        when(gymFacade.getTrainerByUsername("trainer.jane")).thenReturn(trainer);
        // createTraining returns a Training object, not void
        when(gymFacade.createTraining(
                "trainee.john", "traineePass123", "trainer.jane", "trainerPass456",
                "Morning Yoga Session", TrainingTypeName.YOGA, LocalDate.of(2024, 6, 15), 60))
                .thenReturn(new Training());

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).getTrainerByUsername("trainer.jane");
        verify(gymFacade).createTraining(
                "trainee.john", "traineePass123", "trainer.jane", "trainerPass456",
                "Morning Yoga Session", TrainingTypeName.YOGA, LocalDate.of(2024, 6, 15), 60);
    }

    @Test
    @DisplayName("Add training should use trainer's specialization as training type")
    void addTrainingShouldUseTrainerSpecialization() throws Exception {
        AddTrainingRequest request = createValidAddTrainingRequest();
        Trainer trainer = createSampleTrainer(TrainingTypeName.FITNESS);

        when(gymFacade.getTrainerByUsername("trainer.jane")).thenReturn(trainer);
        // createTraining returns a Training object
        when(gymFacade.createTraining(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), eq(TrainingTypeName.FITNESS), any(), anyInt()))
                .thenReturn(new Training());

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).createTraining(
                "trainee.john", "traineePass123", "trainer.jane", "trainerPass456",
                "Morning Yoga Session", TrainingTypeName.FITNESS, LocalDate.of(2024, 6, 15), 60);
    }

    @Test
    @DisplayName("Add training should return 400 when request is invalid")
    void addTrainingShouldReturnBadRequestWhenInvalid() throws Exception {
        AddTrainingRequest invalidRequest = new AddTrainingRequest();

        mockMvc.perform(post("/api/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(gymFacade, never()).getTrainerByUsername(anyString());
        verify(gymFacade, never()).createTraining(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Add training should propagate exception when trainer not found")
    void addTrainingShouldPropagateExceptionWhenTrainerNotFound() {
        AddTrainingRequest request = createValidAddTrainingRequest();

        when(gymFacade.getTrainerByUsername("trainer.jane"))
                .thenThrow(new RuntimeException("Trainer not found"));

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/trainings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause);
        assertEquals("Trainer not found", rootCause.getMessage());

        verify(gymFacade).getTrainerByUsername("trainer.jane");
        verify(gymFacade, never()).createTraining(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Add training should propagate exception when create training fails")
    void addTrainingShouldPropagateExceptionWhenCreateFails() {
        AddTrainingRequest request = createValidAddTrainingRequest();
        Trainer trainer = createSampleTrainer(TrainingTypeName.YOGA);

        when(gymFacade.getTrainerByUsername("trainer.jane")).thenReturn(trainer);
        when(gymFacade.createTraining(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Trainee authentication failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/api/trainings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        Throwable rootCause = findRootCause(exception);
        assertNotNull(rootCause);
        assertEquals("Trainee authentication failed", rootCause.getMessage());
    }

    // ==================== Get Training Types Tests ====================

    @Test
    @DisplayName("Get training types should return list of all types")
    void getTrainingTypesShouldReturnListOfAllTypes() throws Exception {
        TrainingType type1 = createTrainingTypeMock(1L, TrainingTypeName.YOGA);
        TrainingType type2 = createTrainingTypeMock(2L, TrainingTypeName.FITNESS);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type1, type2));

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].trainingTypeName").value("YOGA"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].trainingTypeName").value("FITNESS"));

        verify(trainingTypeRepository).findAll();
    }

    @Test
    @DisplayName("Get training types should return empty list when no types exist")
    void getTrainingTypesShouldReturnEmptyListWhenNoTypes() throws Exception {
        when(trainingTypeRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().json("[]"));

        verify(trainingTypeRepository).findAll();
    }

    @Test
    @DisplayName("Get training types should map entity to response correctly")
    void getTrainingTypesShouldMapEntityToResponseCorrectly() throws Exception {
        TrainingType type = createTrainingTypeMock(5L, TrainingTypeName.CARDIO);

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type));

        mockMvc.perform(get("/api/trainings/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].trainingTypeName").value("CARDIO"));
    }

    // ==================== Helper Methods ====================

    private AddTrainingRequest createValidAddTrainingRequest() {
        AddTrainingRequest request = new AddTrainingRequest();
        request.setTraineeUsername("trainee.john");
        request.setTraineePassword("traineePass123");
        request.setTrainerUsername("trainer.jane");
        request.setTrainerPassword("trainerPass456");
        request.setTrainingName("Morning Yoga Session");
        request.setTrainingDate(LocalDate.of(2024, 6, 15));
        request.setTrainingDuration(60);
        return request;
    }

    private Trainer createSampleTrainer(TrainingTypeName specialization) {
        // For trainer specialization, we only need getTrainingTypeName()
        TrainingType trainingType = mock(TrainingType.class);
        when(trainingType.getTrainingTypeName()).thenReturn(specialization);
        // Note: getId() is NOT stubbed here - this avoids unnecessary stubbing

        com.epam.gym.entity.User user = com.epam.gym.entity.User.builder()
                .username("trainer.jane")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(trainingType)
                .build();
    }

    /**
     * Creates a mocked TrainingType with specific ID and name for repository tests.
     * Both getId() and getTrainingTypeName() are stubbed here because they're both used
     * in the controller's stream mapping to TrainingTypeResponse.
     */
    private TrainingType createTrainingTypeMock(Long id, TrainingTypeName name) {
        TrainingType mock = mock(TrainingType.class);
        when(mock.getId()).thenReturn(id);
        when(mock.getTrainingTypeName()).thenReturn(name);
        return mock;
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (RuntimeException.class.isInstance(current)) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }
}