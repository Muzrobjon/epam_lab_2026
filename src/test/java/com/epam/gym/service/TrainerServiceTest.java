package com.epam.gym.service;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.TrainingTypeRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    private TrainerRepository trainerRepository;
    private TrainingTypeRepository trainingTypeRepository;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private Validator validator;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerRepository = mock(TrainerRepository.class);
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        usernameGenerator = mock(UsernameGenerator.class);
        passwordGenerator = mock(PasswordGenerator.class);
        validator = mock(Validator.class);

        trainerService = new TrainerService(
                trainerRepository,
                trainingTypeRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Test
    void createProfile_shouldCreateTrainer() {
        String firstName = "Alice";
        String lastName = "Smith";
        String specializationName = "Yoga";

        TrainingType specialization = TrainingType.builder().trainingTypeName(specializationName).build();

        when(trainingTypeRepository.findByTrainingTypeName(specializationName))
                .thenReturn(Optional.of(specialization));
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("alicesmith");
        when(passwordGenerator.generatePassword(10)).thenReturn("pass123456");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());

        Trainer created = trainerService.createProfile(firstName, lastName, specializationName);

        assertEquals("alicesmith", created.getUserName());
        assertEquals("pass123456", created.getPassword());
        assertEquals(firstName, created.getFirstName());
        assertEquals(lastName, created.getLastName());
        assertEquals(specialization, created.getSpecialization());
        assertTrue(created.getIsActive());
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void authenticate_shouldThrow_whenTrainerNotFound() {
        when(trainerRepository.findByUserName("unknown")).thenReturn(Optional.empty());
        assertThrows(AuthenticationException.class, () -> trainerService.authenticate("unknown", "pass"));
    }

    @Test
    void authenticate_shouldThrow_whenPasswordWrong() {
        Trainer trainer = Trainer.builder().userName("alice").password("correct").build();
        when(trainerRepository.findByUserName("alice")).thenReturn(Optional.of(trainer));
        assertThrows(AuthenticationException.class, () -> trainerService.authenticate("alice", "wrong"));
    }

    @Test
    void changePassword_shouldUpdatePassword() {
        Trainer trainer = Trainer.builder().userName("alice").password("oldPass").build();
        when(trainerRepository.findByUserName("alice")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.changePassword("alice", "oldPass", "newPass");

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());

        assertEquals("newPass", captor.getValue().getPassword());
    }

    @Test
    void updateProfile_shouldUpdateFieldsAndSpecialization() {
        TrainingType oldSpec = TrainingType.builder().trainingTypeName("Yoga").build();
        TrainingType newSpec = TrainingType.builder().trainingTypeName("Pilates").build();

        Trainer existing = Trainer.builder().userName("alice").password("pass").specialization(oldSpec).build();
        Trainer updatedTrainer = Trainer.builder().firstName("Alicia")
                .specialization(TrainingType.builder().trainingTypeName("Pilates").build())
                .build();

        when(trainerRepository.findByUserName("alice")).thenReturn(Optional.of(existing));
        when(trainingTypeRepository.findByTrainingTypeName("Pilates")).thenReturn(Optional.of(newSpec));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(validator.validate(any(Trainer.class))).thenReturn(Collections.emptySet());

        Trainer result = trainerService.updateProfile("alice", "pass", updatedTrainer);

        assertEquals("Alicia", result.getFirstName());
        assertEquals(newSpec, result.getSpecialization());
    }

    @Test
    void toggleActiveStatus_shouldFlipStatus() {
        Trainer trainer = Trainer.builder().userName("alice").password("pass").isActive(true).build();
        when(trainerRepository.findByUserName("alice")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.toggleActiveStatus("alice", "pass");
        assertFalse(trainer.getIsActive());

        trainerService.toggleActiveStatus("alice", "pass");
        assertTrue(trainer.getIsActive());
    }

    @Test
    void getUnassignedTrainers_shouldReturnList() {
        Trainer t1 = Trainer.builder().userName("t1").build();
        Trainer t2 = Trainer.builder().userName("t2").build();
        List<Trainer> trainers = Arrays.asList(t1, t2);

        when(trainerRepository.findUnassignedTrainersByTraineeUsername("trainee1")).thenReturn(trainers);

        List<Trainer> result = trainerService.getUnassignedTrainers("trainee1");

        assertEquals(2, result.size());
        assertTrue(result.contains(t1));
        assertTrue(result.contains(t2));
    }

    @Test
    void createProfile_shouldThrowValidationException_whenInvalid() {
        TrainingType specialization = TrainingType.builder().trainingTypeName("Yoga").build();
        when(trainingTypeRepository.findByTrainingTypeName("Yoga")).thenReturn(Optional.of(specialization));
        when(usernameGenerator.generateUsername(any(), any())).thenReturn("alice");
        when(passwordGenerator.generatePassword(10)).thenReturn("pass123");

        ConstraintViolation<Trainer> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid name");
        when(validator.validate(any(Trainer.class))).thenReturn(Set.of(violation));

        assertThrows(ValidationException.class,
                () -> trainerService.createProfile("Alice", "Smith", "Yoga"));
    }
}