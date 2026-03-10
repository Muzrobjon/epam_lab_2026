package com.epam.gym.storage;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.repository.TrainingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataInitializerTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dataInitializer = new DataInitializer(trainingTypeRepository);
    }

    @Test
    void init_createsMissingTrainingTypes() {
        // Simulate that all types are missing
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(typeName)).thenReturn(Optional.empty());
        }

        dataInitializer.init();

        // Capture all saved TrainingType objects
        ArgumentCaptor<TrainingType> captor = ArgumentCaptor.forClass(TrainingType.class);
        verify(trainingTypeRepository, times(TrainingTypeName.values().length)).save(captor.capture());

        // Assert that each TrainingTypeName was saved
        Set<TrainingTypeName> savedTypes = new HashSet<>();
        for (TrainingType saved : captor.getAllValues()) {
            savedTypes.add(saved.getTrainingTypeName());
            assertNull(saved.getId());
        }
        assertEquals(Set.of(TrainingTypeName.values()), savedTypes);
    }

    @Test
    void init_doesNotCreateExistingTrainingTypes() {
        // Simulate that all types already exist
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(typeName))
                    .thenReturn(Optional.of(new TrainingType(1L, typeName)));
        }

        dataInitializer.init();

        // Verify that save was NOT called for any type
        verify(trainingTypeRepository, never()).save(any(TrainingType.class));
    }
}