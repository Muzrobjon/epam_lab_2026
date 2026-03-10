package com.epam.gym.service;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private Validator validator;

    @InjectMocks
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trainerService = new TrainerService(
                trainerRepository,
                traineeRepository,
                userRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Test
    void createProfile_createsTrainerAndUser() {
        String firstName = "Jane";
        String lastName = "Smith";
        TrainingTypeName specialization = TrainingTypeName.YOGA;
        String rawPassword = "password123";
        String username = "jane.smith";

        User user = User.builder()
                .id(1L)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .password(rawPassword)
                .username(username)
                .build();

        Trainer trainer = Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(specialization)
                .build();

        when(passwordGenerator.generatePassword()).thenReturn(rawPassword);
        when(usernameGenerator.generateUsername(any(), any())).thenReturn(username);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        Trainer result = trainerService.createProfile(firstName, lastName, specialization);

        assertNotNull(result);
        assertEquals(firstName,
                result.getUser().getFirstName());
        assertEquals(lastName, result.getUser().getLastName());
        assertEquals(username, result.getUser().getUsername());
        assertEquals(rawPassword, result.getUser().getPassword());
        assertEquals(specialization, result.getSpecialization());

        verify(passwordGenerator).generatePassword();
        verify(usernameGenerator).generateUsername(any(), any());
        verify(userRepository).save(any(User.class));
        verify(trainerRepository).save(any(Trainer.class));
        verify(validator).validate(any(Trainer.class));
    }

    @Test
    void selectByUsername_returnsTrainer() {
        String username = "jane.smith";
        User user = User.builder().username(username).build();
        Trainer trainer = Trainer.builder().user(user).build();

        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.selectByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUser().getUsername());
    }

    @Test
    void selectByUsername_notFound_throwsException() {
        String username = "notfound";
        when(trainerRepository.findByUser_Username(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.selectByUsername(username));
    }

    @Test
    void getUnassignedTrainers_returnsList() {
        String traineeUsername = "trainee1";
        User user = User.builder().username(traineeUsername).build();
        Trainee trainee = Trainee.builder().user(user).build();
        Trainer trainer = Trainer.builder().user(User.builder().username("trainer1").build()).build();

        when(traineeRepository.findByUser_Username(traineeUsername)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findUnassignedTrainersByTraineeUsername(traineeUsername))
                .thenReturn(Collections.singletonList(trainer));

        List<Trainer> result = trainerService.getUnassignedTrainers(traineeUsername);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("trainer1", result.getFirst().getUser().getUsername());
    }

    @Test
    void getUnassignedTrainers_traineeNotFound_throwsException() {
        String traineeUsername = "notfound";
        when(traineeRepository.findByUser_Username(traineeUsername)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.getUnassignedTrainers(traineeUsername));
    }

    // Add more tests for updateProfile and other methods as needed
}