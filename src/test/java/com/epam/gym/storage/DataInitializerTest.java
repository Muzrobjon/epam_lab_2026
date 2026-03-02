package com.epam.gym.storage;

import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    private TrainingTypeRepository trainingTypeRepository;
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        dataInitializer = new DataInitializer(trainingTypeRepository);
    }

    @Test
    void init_shouldSaveTrainingTypes_whenTheyDoNotExist() {
        // Mock: repository returns empty for all types (none exist)
        when(trainingTypeRepository.findByTrainingTypeName(anyString()))
                .thenReturn(Optional.empty());

        dataInitializer.init();

        // Capture all saved types
        ArgumentCaptor<TrainingType> captor = ArgumentCaptor.forClass(TrainingType.class);
        verify(trainingTypeRepository, times(7)).save(captor.capture());

        // Verify all expected training type names
        assertEquals("Fitness", captor.getAllValues().get(0).getTrainingTypeName());
        assertEquals("Yoga", captor.getAllValues().get(1).getTrainingTypeName());
        assertEquals("Cardio", captor.getAllValues().get(2).getTrainingTypeName());
        assertEquals("Strength", captor.getAllValues().get(3).getTrainingTypeName());
        assertEquals("Pilates", captor.getAllValues().get(4).getTrainingTypeName());
        assertEquals("CrossFit", captor.getAllValues().get(5).getTrainingTypeName());
        assertEquals("Zumba", captor.getAllValues().get(6).getTrainingTypeName());
    }

    @Test
    void init_shouldNotSaveExistingTrainingTypes() {
        // Mock: Yoga already exists
        when(trainingTypeRepository.findByTrainingTypeName("Yoga"))
                .thenReturn(Optional.of(new TrainingType()));

        when(trainingTypeRepository.findByTrainingTypeName(argThat(name -> !"Yoga".equals(name))))
                .thenReturn(Optional.empty());

        dataInitializer.init();

        // Captor should only capture 6 saves (all except Yoga)
        ArgumentCaptor<TrainingType> captor = ArgumentCaptor.forClass(TrainingType.class);
        verify(trainingTypeRepository, times(6)).save(captor.capture());

        // Verify Yoga was not saved
        assert captor.getAllValues().stream()
                .noneMatch(t -> "Yoga".equals(t.getTrainingTypeName()));
    }
}