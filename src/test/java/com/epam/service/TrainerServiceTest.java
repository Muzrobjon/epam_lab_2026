package com.epam.service;

import com.epam.gym.dao.TrainerDAO;
import com.epam.gym.model.Trainer;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.UsernameGenerator;
import com.epam.gym.service.PasswordGenerator;
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

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

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
        // Arrange: Mock the username and password generation
        when(usernameGenerator.generateUsername(any(Trainer.class), any())).thenReturn("sarah.williams");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("generatedPassword123");
        when(trainerDAO.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setUserId(2L);  // Simulate ID assignment after saving
            return t;
        });

        // Act: Create a new trainer profile
        Trainer result = trainerService.createProfile("Sarah", "Williams", "Yoga");

        // Assert: Verify the results
        assertNotNull(result);
        assertEquals("sarah.williams", result.getUserName());
        assertEquals("Sarah", result.getFirstName());
        assertEquals("Williams", result.getLastName());
        assertEquals("Yoga", result.getSpecialization());
        assertTrue(result.isActive());
        assertEquals("generatedPassword123", result.getPassword());

        // Verify the interaction with the mocks
        verify(usernameGenerator).generateUsername(any(Trainer.class), any());
        verify(passwordGenerator).generatePassword(10);
        verify(trainerDAO).save(any(Trainer.class));
    }

    @Test
    void updateProfile_WhenExists_ShouldSave() {
        // Arrange
        when(trainerDAO.exists(1L)).thenReturn(true);
        when(trainerDAO.save(testTrainer)).thenReturn(testTrainer);

        // Act
        Trainer result = trainerService.updateProfile(testTrainer);

        // Assert
        assertEquals(testTrainer, result);
        verify(trainerDAO).exists(1L);
        verify(trainerDAO).save(testTrainer);
    }

    @Test
    void updateProfile_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(trainerDAO.exists(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            trainerService.updateProfile(testTrainer);
        });
        assertEquals("Trainer not found: 1", exception.getMessage());
        verify(trainerDAO, never()).save(any());
    }

    @Test
    void selectProfile_ShouldReturnOptional() {
        // Arrange
        when(trainerDAO.findById(1L)).thenReturn(Optional.of(testTrainer));

        // Act
        Optional<Trainer> result = trainerService.selectProfile(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTrainer, result.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainers() {
        // Arrange
        List<Trainer> trainers = Arrays.asList(testTrainer);
        when(trainerDAO.findAll()).thenReturn(trainers);

        // Act
        List<Trainer> result = trainerService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTrainer, result.get(0));

        // Verify the interaction with the mock
        verify(trainerDAO).findAll();
    }

    @Test
    void generateUsername_ShouldCreateLowercaseConcatenation() {
        // Arrange
        when(trainerDAO.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usernameGenerator.generateUsername(any(Trainer.class), any())).thenReturn("mike.johnson");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("generatedPassword123");

        // Act
        Trainer result = trainerService.createProfile("Mike", "Johnson", "Fitness");

        // Assert
        assertNotNull(result);
        assertEquals("mike.johnson", result.getUserName());
    }
}
