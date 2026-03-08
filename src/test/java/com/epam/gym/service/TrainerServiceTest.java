package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TrainerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerRepository trainerRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private Validator validator;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerRepository = mock(TrainerRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        validator = mock(Validator.class);

        trainerService = new TrainerService(
                trainerRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Test
    void testCreateProfile_Success() {
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("traineruser");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("password123");
        when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainer result = trainerService.createProfile("Alice", "Smith", TrainingTypeName.CARDIO);
        assertEquals("traineruser", result.getUserName());
        assertEquals("password123", result.getPassword());
        assertEquals("Alice", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals(TrainingTypeName.CARDIO, result.getSpecialization());
        assertTrue(result.getIsActive());
        assertNotNull(result.getId());
    }

    @Test
    void testCreateProfile_ValidationFails() {
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("traineruser");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("password123");
        ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Specialization required");
        Set<ConstraintViolation<Trainer>> violations = Set.of(violation);
        when(validator.validate(any(Trainer.class))).thenReturn(violations);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                trainerService.createProfile("Alice", "Smith", TrainingTypeName.CARDIO));
        assertTrue(ex.getMessage().contains("Specialization required"));
    }

    @Test
    void testAuthenticate_Success() {
        Trainer trainer = Trainer.builder().userName("traineruser").password("pass").build();
        when(trainerRepository.findByUserName("traineruser")).thenReturn(Optional.of(trainer));
        assertDoesNotThrow(() -> trainerService.authenticate("traineruser", "pass"));
    }

    @Test
    void testAuthenticate_NotFound() {
        when(trainerRepository.findByUserName("traineruser")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> trainerService.authenticate("traineruser", "pass"));
    }

    @Test
    void testAuthenticate_WrongPassword() {
        Trainer trainer = Trainer.builder().userName("traineruser").password("pass").build();
        when(trainerRepository.findByUserName("traineruser")).thenReturn(Optional.of(trainer));
        assertThrows(AuthenticationException.class, () -> trainerService.authenticate("traineruser", "wrong"));
    }

    @Test
    void testSelectByUsername_Success() {
        Trainer trainer = Trainer.builder().userName("traineruser").build();
        when(trainerRepository.findByUserName("traineruser")).thenReturn(Optional.of(trainer));
        assertEquals(trainer, trainerService.selectByUsername("traineruser"));
    }

    @Test
    void testSelectByUsername_NotFound() {
        when(trainerRepository.findByUserName("traineruser")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> trainerService.selectByUsername("traineruser"));
    }

    @Test
    void testGetUnassignedTrainers() {
        List<Trainer> trainers = List.of(
                Trainer.builder().userName("t1").build(),
                Trainer.builder().userName("t2").build()
        );
        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1")).thenReturn(trainers);
        List<Trainer> result = trainerService.getUnassignedTrainers("trainee1");
        assertEquals(trainers, result);
    }
}