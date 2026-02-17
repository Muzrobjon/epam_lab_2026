package com.epam;

import com.epam.gym.dao.TraineeDAO;
import com.epam.gym.model.Trainee;
import com.epam.gym.service.TraineeService;
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
        LocalDate dob = LocalDate.of(1995, 3, 20);
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setUserId(2L); // Simulate ID assignment
            return t;
        });

        Trainee result = traineeService.createProfile("Jane", "Smith", dob, "456 Oak Ave");

        assertNotNull(result);
        assertEquals("jane.smith", result.getUserName());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(dob, result.getDateOfBirth());
        assertEquals("456 Oak Ave", result.getAddress());
        assertTrue(result.isActive());
        verify(traineeDAO).save(any(Trainee.class));
    }

    @Test
    void updateProfile_WhenExists_ShouldSave() {
        when(traineeDAO.exists(1L)).thenReturn(true);
        when(traineeDAO.save(testTrainee)).thenReturn(testTrainee);

        Trainee result = traineeService.updateProfile(testTrainee);

        assertEquals(testTrainee, result);
        verify(traineeDAO).exists(1L);
        verify(traineeDAO).save(testTrainee);
    }

    @Test
    void updateProfile_WhenNotExists_ShouldThrowException() {
        when(traineeDAO.exists(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            traineeService.updateProfile(testTrainee);
        });

        assertEquals("Trainee not found: 1", exception.getMessage());
        verify(traineeDAO, never()).save(any());
    }

    @Test
    void deleteProfile_ShouldCallDAODelete() {
        doNothing().when(traineeDAO).delete(1L);

        traineeService.deleteProfile(1L);

        verify(traineeDAO).delete(1L);
    }

    @Test
    void selectProfile_ShouldReturnOptional() {
        when(traineeDAO.findById(1L)).thenReturn(Optional.of(testTrainee));

        Optional<Trainee> result = traineeService.selectProfile(1L);

        assertTrue(result.isPresent());
        assertEquals(testTrainee, result.get());
    }

    @Test
    void findAll_ShouldReturnAllTrainees() {
        List<Trainee> trainees = Arrays.asList(testTrainee);
        when(traineeDAO.findAll()).thenReturn(trainees);

        List<Trainee> result = traineeService.findAll();

        assertEquals(1, result.size());
        assertEquals(testTrainee, result.get(0));
    }

    @Test
    void generateUsername_ShouldCreateLowercaseConcatenation() {
        // Mock the DAO to return the saved trainee
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.createProfile("John", "Doe", null, null);

        assertNotNull(result);
        assertEquals("john.doe", result.getUserName());
    }

    @Test
    void generateUsername_WithMixedCase_ShouldConvertToLowercase() {
        // Mock the DAO to return the saved trainee
        when(traineeDAO.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.createProfile("JOHN", "DOE", null, null);

        assertNotNull(result);
        assertEquals("john.doe", result.getUserName());
    }
}

