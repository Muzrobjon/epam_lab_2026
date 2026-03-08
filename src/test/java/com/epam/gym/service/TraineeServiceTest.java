package com.epam.gym.service;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private Validator validator;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        validator = mock(Validator.class);

        traineeService = new TraineeService(
                traineeRepository,
                trainerRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Test
    void testCreateProfile_Success() {
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("johndoe");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("password123");
        when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        Trainee result = traineeService.createProfile("John", "Doe", LocalDate.of(1990, 1, 1), "Address");
        assertEquals("johndoe", result.getUserName());
        assertEquals("password123", result.getPassword());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Address", result.getAddress());
        assertTrue(result.getIsActive());
        assertNotNull(result.getId());
    }

    @Test
    void testCreateProfile_ValidationFails() {
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("johndoe");
        when(passwordGenerator.generatePassword(anyInt())).thenReturn("password123");
        ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("First name required");
        Set<ConstraintViolation<Trainee>> violations = Set.of(violation);
        when(validator.validate(any(Trainee.class))).thenReturn(violations);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                traineeService.createProfile("John", "Doe", LocalDate.of(1990, 1, 1), "Address"));
        assertTrue(ex.getMessage().contains("First name required"));
    }

    @Test
    void testAuthenticate_Success() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("pass").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        assertDoesNotThrow(() -> traineeService.authenticate("johndoe", "pass"));
    }

    @Test
    void testAuthenticate_NotFound() {
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> traineeService.authenticate("johndoe", "pass"));
    }

    @Test
    void testAuthenticate_WrongPassword() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("pass").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        assertThrows(AuthenticationException.class, () -> traineeService.authenticate("johndoe", "wrong"));
    }

    @Test
    void testSelectByUsername_Success() {
        Trainee trainee = Trainee.builder().userName("johndoe").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        assertEquals(trainee, traineeService.selectByUsername("johndoe"));
    }

    @Test
    void testSelectByUsername_NotFound() {
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> traineeService.selectByUsername("johndoe"));
    }

    @Test
    void testChangePassword_Success() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("old").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // authenticate will pass
        assertDoesNotThrow(() -> traineeService.changePassword("johndoe", "old", "new"));
        verify(traineeRepository, times(1)).save(any(Trainee.class));
    }

    @Test
    void testUpdateProfile_Success() {
        Trainee existing = Trainee.builder()
                .userName("johndoe")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Old Address")
                .password("pass") // Set password to avoid NPE
                .build();
        Trainee updated = Trainee.builder()
                .firstName("Johnny")
                .address("New Address")
                .build();

        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());

        Trainee result = traineeService.updateProfile("johndoe", "pass", updated);
        assertEquals("Johnny", result.getFirstName());
        assertEquals("New Address", result.getAddress());
    }

    @Test
    void testToggleActiveStatus() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("pass").isActive(true).build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertDoesNotThrow(() -> traineeService.toggleActiveStatus("johndoe", "pass"));
        assertFalse(trainee.getIsActive());
    }

    @Test
    void testDeleteByUsername() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("pass").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        doNothing().when(traineeRepository).delete(any(Trainee.class));
        assertDoesNotThrow(() -> traineeService.deleteByUsername("johndoe", "pass"));
        verify(traineeRepository, times(1)).delete(trainee);
    }

    @Test
    void testUpdateTrainersList() {
        Trainee trainee = Trainee.builder().userName("johndoe").password("pass").trainers(new ArrayList<>()).build();
        Trainer trainer = Trainer.builder().userName("trainer1").build();
        when(traineeRepository.findByUserName("johndoe")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserName("trainer1")).thenReturn(Optional.of(trainer));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertDoesNotThrow(() -> traineeService.updateTrainersList("johndoe", "pass", List.of("trainer1")));
        assertEquals(1, trainee.getTrainers().size());
        assertEquals(trainer, trainee.getTrainers().get(0));
    }

    @Test
    void testSelectProfileById() {
        Trainee trainee = Trainee.builder().id(1L).build();
        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        assertEquals(trainee, traineeService.selectProfile(1L));
    }

    @Test
    void testFindAll() {
        List<Trainee> trainees = List.of(
                Trainee.builder().userName("a").build(),
                Trainee.builder().userName("b").build()
        );
        when(traineeRepository.findAll()).thenReturn(trainees);
        assertEquals(trainees, traineeService.findAll());
    }
}