package com.epam;

import com.epam.gym.dao.TrainerDAO;
import com.epam.gym.model.Trainer;
import com.epam.gym.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerDAOTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TrainerDAO trainerDAO;

    private Trainer testTrainer;

    @BeforeEach
    void setUp() {
        testTrainer = Trainer.builder()
                .userId(1L)
                .firstName("Mike")
                .lastName("Johnson")
                .userName("mike.johnson")
                .isActive(true)
                .specialization("Fitness")
                .build();
    }

    @Test
    void save_WithNullId_ShouldGenerateIdAndSave() {
        Trainer newTrainer = Trainer.builder()
                .firstName("Sarah")
                .lastName("Williams")
                .build();

        when(storage.generateId("trainers")).thenReturn(2L);
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Trainer result = trainerDAO.save(newTrainer);

        assertNotNull(result.getUserId());
        assertEquals(2L, result.getUserId());
        verify(storage).generateId("trainers");
        verify(storage).put("trainers", 2L, newTrainer);
    }

    @Test
    void save_WithExistingId_ShouldSaveWithoutGeneratingId() {
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Trainer result = trainerDAO.save(testTrainer);

        assertEquals(1L, result.getUserId());
        verify(storage, never()).generateId(anyString());
        verify(storage).put("trainers", 1L, testTrainer);
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainer() {
        when(storage.get("trainers", 1L)).thenReturn(testTrainer);

        Optional<Trainer> result = trainerDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        when(storage.get("trainers", 999L)).thenReturn(null);

        Optional<Trainer> result = trainerDAO.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_WhenExists_ShouldReturnTrainer() {
        Map<Long, Object> trainers = new HashMap<>();
        trainers.put(1L, testTrainer);

        when(storage.getAll("trainers")).thenReturn(trainers);

        Optional<Trainer> result = trainerDAO.findByUsername("mike.johnson");

        assertTrue(result.isPresent());
        assertEquals("mike.johnson", result.get().getUserName());
    }

    @Test
    void findByUsername_WhenNotExists_ShouldReturnEmpty() {
        when(storage.getAll("trainers")).thenReturn(new HashMap<>());

        Optional<Trainer> result = trainerDAO.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        Trainer trainer2 = Trainer.builder()
                .userId(2L)
                .userName("sarah.williams")
                .build();

        Map<Long, Object> trainers = new HashMap<>();
        trainers.put(1L, testTrainer);
        trainers.put(2L, trainer2);

        when(storage.getAll("trainers")).thenReturn(trainers);

        List<Trainer> result = trainerDAO.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void exists_WhenExists_ShouldReturnTrue() {
        when(storage.contains("trainers", 1L)).thenReturn(true);

        assertTrue(trainerDAO.exists(1L));
    }

    @Test
    void exists_WhenNotExists_ShouldReturnFalse() {
        when(storage.contains("trainers", 999L)).thenReturn(false);

        assertFalse(trainerDAO.exists(999L));
    }
}
