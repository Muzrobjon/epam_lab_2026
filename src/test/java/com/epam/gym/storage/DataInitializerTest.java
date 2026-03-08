package com.epam.gym.storage;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
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
    void testInit_SavesAllMissingTrainingTypes() {
        // Simulate all types are missing
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(typeName)).thenReturn(Optional.empty());
        }

        dataInitializer.init();

        // Should save each type once (use argThat to check the typeName)
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            verify(trainingTypeRepository).save(argThat(t -> t.getTrainingTypeName() == typeName));
        }
    }

    @Test
    void testInit_DoesNotSaveExistingTypes() {
        // Simulate all types already exist
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(typeName))
                    .thenReturn(Optional.of(new TrainingType(1L, typeName)));
        }

        dataInitializer.init();

        // Should not save any type
        verify(trainingTypeRepository, never()).save(any());
    }
}