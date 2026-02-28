package com.epam.dao;

import com.epam.gym.dao.TraineeDAO;
import com.epam.gym.model.Trainee;
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
class TraineeDAOTest {

    @Mock
    private Storage storage;

    @InjectMocks
    private TraineeDAO traineeDAO;

    private Trainee testTrainee;

    @BeforeEach
    void setUp() {
        testTrainee = Trainee.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .userName("john.doe")
                .isActive(true)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .address("123 Main St")
                .build();
    }

    @Test
    void save_WithNullId_ShouldGenerateIdAndSave() {
        Trainee newTrainee = Trainee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(storage.generateId("trainees")).thenReturn(2L);
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Trainee result = traineeDAO.save(newTrainee);

        assertNotNull(result.getUserId());
        assertEquals(2L, result.getUserId());
        verify(storage).generateId("trainees");
        verify(storage).put("trainees", 2L, newTrainee);
    }

    @Test
    void save_WithExistingId_ShouldSaveWithoutGeneratingId() {
        doNothing().when(storage).put(anyString(), anyLong(), any());

        Trainee result = traineeDAO.save(testTrainee);

        assertEquals(1L, result.getUserId());
        verify(storage, never()).generateId(anyString());
        verify(storage).put("trainees", 1L, testTrainee);
    }

    @Test
    void findById_WhenExists_ShouldReturnTrainee() {
        when(storage.get("trainees", 1L)).thenReturn(testTrainee);

        Optional<Trainee> result = traineeDAO.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        when(storage.get("trainees", 999L)).thenReturn(null);

        Optional<Trainee> result = traineeDAO.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_WhenExists_ShouldReturnTrainee() {
        Map<Long, Object> trainees = new HashMap<>();
        trainees.put(1L, testTrainee);

        when(storage.getAll("trainees")).thenReturn(trainees);

        Optional<Trainee> result = traineeDAO.findByUsername("john.doe");

        assertTrue(result.isPresent());
        assertEquals("john.doe", result.get().getUserName());
    }

    @Test
    void findByUsername_WhenNotExists_ShouldReturnEmpty() {
        when(storage.getAll("trainees")).thenReturn(new HashMap<>());

        Optional<Trainee> result = traineeDAO.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        Trainee trainee2 = Trainee.builder()
                .userId(2L)
                .userName("jane.smith")
                .build();

        Map<Long, Object> trainees = new HashMap<>();
        trainees.put(1L, testTrainee);
        trainees.put(2L, trainee2);

        when(storage.getAll("trainees")).thenReturn(trainees);

        List<Trainee> result = traineeDAO.findAll();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getUserId().equals(1L)));
        assertTrue(result.stream().anyMatch(t -> t.getUserId().equals(2L)));
    }

    @Test
    void delete_ShouldRemoveFromStorage() {
        // remove() returns Object, not void - use when().thenReturn()
        when(storage.remove(anyString(), anyLong())).thenReturn(testTrainee);

        traineeDAO.delete(1L);

        verify(storage).remove("trainees", 1L);
    }

    @Test
    void exists_WhenExists_ShouldReturnTrue() {
        when(storage.contains("trainees", 1L)).thenReturn(true);

        assertTrue(traineeDAO.exists(1L));
    }

    @Test
    void exists_WhenNotExists_ShouldReturnFalse() {
        when(storage.contains("trainees", 999L)).thenReturn(false);

        assertFalse(traineeDAO.exists(999L));
    }
}
