package com.epam.dao;
import com.epam.gym.dao.TrainingDAO;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingDAOTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainingDAO trainingDAO;

    private Training testTraining;

    @BeforeEach
    void setUp() {
        testTraining = Training.builder()
                .trainingId(1L)
                .traineeId(1L)
                .trainerId(1L)
                .trainingName("Morning Fitness")
                .trainingType(TrainingType.builder().trainingTypeName("Fitness").build())
                .trainingDate(LocalDate.of(2024, 1, 15))
                .trainingDurationMinutes(60)
                .build();
    }

    @Test
    void save_WithNullId_ShouldGenerateIdAndSave() {
        Training newTraining = Training.builder()
                .trainingName("Evening Yoga")
                .build();

        when(storage.generateId("trainings")).thenReturn(2L);
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Training result = trainingDAO.save(newTraining);

        assertNotNull(result.getTrainingId());
        assertEquals(2L, result.getTrainingId());
        verify(storage).generateId("trainings");
        verify(storage).put("trainings", 2L, newTraining);
    }

    @Test
    void save_WithExistingId_ShouldSaveWithoutGeneratingId() {
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Training result = trainingDAO.save(testTraining);

        assertEquals(1L, result.getTrainingId());
        verify(storage, never()).generateId(anyString());
        verify(storage).put("trainings", 1L, testTraining);
    }

    @Test
    void findById_WhenExists_ShouldReturnTraining() {
        when(storage.get("trainings", 1L)).thenReturn(testTraining);

        Optional<Training> result = trainingDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        when(storage.get("trainings", 999L)).thenReturn(null);

        Optional<Training> result = trainingDAO.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        Training training2 = Training.builder()
                .trainingId(2L)
                .trainingName("Evening Yoga")
                .build();

        Map<Long, Object> trainings = new HashMap<>();
        trainings.put(1L, testTraining);
        trainings.put(2L, training2);

        when(storage.getAll("trainings")).thenReturn(trainings);

        List<Training> result = trainingDAO.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findByTraineeId_ShouldReturnMatchingTrainings() {
        Training training2 = Training.builder()
                .trainingId(2L)
                .traineeId(1L)
                .trainingName("Cardio")
                .build();
        Training training3 = Training.builder()
                .trainingId(3L)
                .traineeId(2L)
                .trainingName("Yoga")
                .build();

        Map<Long, Object> trainings = new HashMap<>();
        trainings.put(1L, testTraining);
        trainings.put(2L, training2);
        trainings.put(3L, training3);

        when(storage.getAll("trainings")).thenReturn(trainings);

        List<Training> result = trainingDAO.findByTraineeId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTraineeId().equals(1L)));
    }

    @Test
    void findByTrainerId_ShouldReturnMatchingTrainings() {
        Training training2 = Training.builder()
                .trainingId(2L)
                .trainerId(1L)
                .trainingName("Cardio")
                .build();
        Training training3 = Training.builder()
                .trainingId(3L)
                .trainerId(2L)
                .trainingName("Yoga")
                .build();

        Map<Long, Object> trainings = new HashMap<>();
        trainings.put(1L, testTraining);
        trainings.put(2L, training2);
        trainings.put(3L, training3);

        when(storage.getAll("trainings")).thenReturn(trainings);

        List<Training> result = trainingDAO.findByTrainerId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(t -> t.getTrainerId().equals(1L)));
    }

    @Test
    void exists_WhenExists_ShouldReturnTrue() {
        when(storage.contains("trainings", 1L)).thenReturn(true);

        assertTrue(trainingDAO.exists(1L));
    }

    @Test
    void exists_WhenNotExists_ShouldReturnFalse() {
        when(storage.contains("trainings", 999L)).thenReturn(false);

        assertFalse(trainingDAO.exists(999L));
    }
}
