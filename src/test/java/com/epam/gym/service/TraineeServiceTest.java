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
import org.mockito.ArgumentCaptor;

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
    void createProfile_shouldCreateAndSaveTrainee() {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dob = LocalDate.of(1990, 1, 1);
        String address = "123 Street";

        Trainee trainee = Trainee.builder().build();
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("johndoe");
        when(passwordGenerator.generatePassword(10)).thenReturn("pass123456");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validator.validate(any(Trainee.class))).thenReturn(Collections.emptySet());

        Trainee created = traineeService.createProfile(firstName, lastName, dob, address);

        assertEquals("johndoe", created.getUserName());
        assertEquals("pass123456", created.getPassword());
        assertEquals(firstName, created.getFirstName());
        assertEquals(lastName, created.getLastName());
        assertEquals(dob, created.getDateOfBirth());
        assertEquals(address, created.getAddress());
        assertTrue(created.getIsActive());

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void authenticate_shouldThrowException_whenUserNotFound() {
        when(traineeRepository.findByUserName("unknown")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> traineeService.authenticate("unknown", "pass"));
    }

    @Test
    void authenticate_shouldThrowException_whenPasswordIncorrect() {
        Trainee trainee = Trainee.builder().userName("john").password("correct").build();
        when(traineeRepository.findByUserName("john")).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class, () -> traineeService.authenticate("john", "wrong"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        Trainee trainee = Trainee.builder().userName("john").password("oldPass").build();
        when(traineeRepository.findByUserName("john")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.changePassword("john", "oldPass", "newPass");

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());

        assertEquals("newPass", captor.getValue().getPassword());
    }

    @Test
    void updateTrainersList_shouldUpdateTraineeTrainers() {
        Trainee trainee = Trainee.builder().userName("john").password("pass").trainers(new ArrayList<>()).build();
        Trainer trainer1 = Trainer.builder().userName("trainer1").build();
        Trainer trainer2 = Trainer.builder().userName("trainer2").build();

        when(traineeRepository.findByUserName("john")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserName("trainer1")).thenReturn(Optional.of(trainer1));
        when(trainerRepository.findByUserName("trainer2")).thenReturn(Optional.of(trainer2));

        traineeService.updateTrainersList("john", "pass", Arrays.asList("trainer1", "trainer2"));

        assertEquals(2, trainee.getTrainers().size());
        assertTrue(trainee.getTrainers().contains(trainer1));
        assertTrue(trainee.getTrainers().contains(trainer2));
    }

    @Test
    void createProfile_shouldThrowValidationException_whenInvalid() {
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("johndoe");
        when(passwordGenerator.generatePassword(10)).thenReturn("pass123456");

        ConstraintViolation<Trainee> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid name");
        when(validator.validate(any(Trainee.class))).thenReturn(Set.of(violation));

        assertThrows(ValidationException.class, () ->
                traineeService.createProfile("John", "Doe", LocalDate.now(), "Address"));
    }
}