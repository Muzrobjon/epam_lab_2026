package com.epam.service;

import com.epam.gym.dao.TrainingDAO;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingDAO trainingDAO;

    @InjectMocks
    private TrainingService trainingService;

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
                .trainingDuration(60)
                .build();
    }

    @Test
    void createTraining_ShouldCreateAndSave() {
        LocalDate date = LocalDate.now();
        TrainingType type = TrainingType.builder().trainingTypeName("Cardio").build();

        when(trainingDAO.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training result = trainingService.createTraining(2L, 3L, "Evening Cardio", type, date, 45);

        assertNotNull(result);
        assertEquals(2L, result.getTraineeId());
        assertEquals(3L, result.getTrainerId());
        assertEquals("Evening Cardio", result.getTrainingName());
        assertEquals(type, result.getTrainingType());
        assertEquals(date, result.getTrainingDate());
        assertEquals(45, result.getTrainingDuration());
        verify(trainingDAO).save(any(Training.class));
    }

    @Test
    void selectProfile_ShouldReturnOptional() {
        when(trainingDAO.findById(1L)).thenReturn(Optional.of(testTraining));

        Optional<Training> result = trainingService.selectProfile(1L);

        assertTrue(result.isPresent());
        assertEquals(testTraining, result.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainings() {
        List<Training> trainings = Arrays.asList(testTraining);
        when(trainingDAO.findAll()).thenReturn(trainings);

        List<Training> result = trainingService.findAll();

        assertEquals(1, result.size());
        assertEquals(testTraining, result.get(0));
    }
}
