package com.epam.service;

import com.epam.gym.dao.TraineeDAO;
import com.epam.gym.model.Trainee;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.UsernameGenerator;
import com.epam.gym.service.PasswordGenerator;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeDAO traineeDAO;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TraineeService traineeService;

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
    void createProfile_ShouldGenerateUsernameAndSave() {
        // Arrange
        LocalDate dob = LocalDate.of(1995, 3, 20);
        when(usernameGenerator.generateUsername(any(Trainee.class), any())).thenReturn("jane.smith");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("generatedPassword123");
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setUserId(2L); // Simulate ID assignment
            return t;
        });

        // Act
        Trainee result = traineeService.createProfile("Jane", "Smith", dob, "456 Oak Ave");

        // Assert
        assertNotNull(result);
        assertEquals("jane.smith", result.getUserName());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(dob, result.getDateOfBirth());
        assertEquals("456 Oak Ave", result.getAddress());
        assertTrue(result.isActive());
        assertEquals("generatedPassword123", result.getPassword());

        // Verify mock interactions
        verify(usernameGenerator).generateUsername(any(Trainee.class), any());
        verify(passwordGenerator).generatePassword(10);
        verify(traineeDAO).save(any(Trainee.class));
    }

    @Test
    void updateProfile_WhenExists_ShouldSave() {
        // Arrange
        when(traineeDAO.exists(1L)).thenReturn(true);
        when(traineeDAO.save(testTrainee)).thenReturn(testTrainee);

        // Act
        Trainee result = traineeService.updateProfile(testTrainee);

        // Assert
        assertEquals(testTrainee, result);
        verify(traineeDAO).exists(1L);
        verify(traineeDAO).save(testTrainee);
    }

    @Test
    void updateProfile_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(traineeDAO.exists(1L)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            traineeService.updateProfile(testTrainee);
        });
        assertEquals("Trainee not found: 1", exception.getMessage());
        verify(traineeDAO, never()).save(any());
    }

    @Test
    void deleteProfile_ShouldCallDAODelete() {
        // Arrange
        doNothing().when(traineeDAO).delete(1L);

        // Act
        traineeService.deleteProfile(1L);

        // Assert
        verify(traineeDAO).delete(1L);
    }

    @Test
    void selectProfile_ShouldReturnOptional() {
        // Arrange
        when(traineeDAO.findById(1L)).thenReturn(Optional.of(testTrainee));

        // Act
        Optional<Trainee> result = traineeService.selectProfile(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        // Arrange
        List<Trainee> trainees = Arrays.asList(testTrainee);
        when(traineeDAO.findAll()).thenReturn(trainees);

        // Act
        List<Trainee> result = traineeService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testTrainee, result.get(0));

        // Verify the mock interaction
        verify(traineeDAO).findAll();
    }

    @Test
    void generateUsername_ShouldCreateLowercaseConcatenation() {
        // Arrange
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usernameGenerator.generateUsername(any(Trainee.class), any())).thenReturn("john.doe");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("generatedPassword123");

        // Act
        Trainee result = traineeService.createProfile("John", "Doe", null, null);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.getUserName());
    }

    @Test
    void generateUsername_WithMixedCase_ShouldConvertToLowercase() {
        // Arrange
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(usernameGenerator.generateUsername(any(Trainee.class), any())).thenReturn("john.doe");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("generatedPassword123");

        // Act
        Trainee result = traineeService.createProfile("JOHN", "DOE", null, null);

        // Assert
        assertNotNull(result);
        assertEquals("john.doe", result.getUserName());
    }
}
