package com.epam.service;

import com.epam.gym.dao.TrainerDAO;
import com.epam.gym.model.Trainer;
import com.epam.gym.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerDAO trainerDAO;

    @InjectMocks
    private TrainerService trainerService;

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
    void createProfile_ShouldGenerateUsernameAndSave() {
        when(trainerDAO.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setUserId(2L);
            return t;
        });

        Trainer result = trainerService.createProfile("Sarah", "Williams", "Yoga");

        assertNotNull(result);
        assertEquals("sarah.williams", result.getUserName());
        assertEquals("Sarah", result.getFirstName());
        assertEquals("Williams", result.getLastName());
        assertEquals("Yoga", result.getSpecialization());
        assertTrue(result.isActive());
        verify(trainerDAO).save(any(Trainer.class));
    }

    @Test
    void updateProfile_WhenExists_ShouldSave() {
        when(trainerDAO.exists(1L)).thenReturn(true);
        when(trainerDAO.save(testTrainer)).thenReturn(testTrainer);

        Trainer result = trainerService.updateProfile(testTrainer);

        assertEquals(testTrainer, result);
        verify(trainerDAO).exists(1L);
        verify(trainerDAO).save(testTrainer);
    }

    @Test
    void updateProfile_WhenNotExists_ShouldThrowException() {
        when(trainerDAO.exists(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            trainerService.updateProfile(testTrainer);
        });

        assertEquals("Trainer not found: 1", exception.getMessage());
        verify(trainerDAO, never()).save(any());
    }

    @Test
    void selectProfile_ShouldReturnOptional() {
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(testTrainer));

        Optional<Trainer> result = trainerService.selectProfile(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        List<Trainer> trainers = Arrays.asList(testTrainer);
        when(trainerDAO.findAll()).thenReturn(trainers);

        List<Trainer> result = trainerService.findAll();

        assertEquals(1, result.size());
        assertEquals(testTrainer, result.get(0));
    }

    @Test
    void generateUsername_ShouldCreateLowercaseConcatenation() {
        // Mock the DAO to return the saved trainer
        when(trainerDAO.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = trainerService.createProfile("Mike", "Johnson", "Fitness");

        assertNotNull(result);
        assertEquals("mike.johnson", result.getUserName());
    }
}

