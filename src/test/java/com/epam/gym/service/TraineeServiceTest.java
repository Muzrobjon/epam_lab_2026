package com.epam.gym.service;

import com.epam.gym.exception.NotFoundException;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.User;
import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UsernameGenerator usernameGenerator;
    @Mock
    private PasswordGenerator passwordGenerator;
    @Mock
    private Validator validator;

    @InjectMocks
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        traineeService = new TraineeService(
                traineeRepository,
                trainerRepository,
                userRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );
    }

    @Test
    void createProfile_createsTraineeAndUser() {
        String firstName = "John";
        String lastName = "Doe";
        LocalDate dob = LocalDate.of(2000, 1, 1);
        String address = "123 Main St";
        String rawPassword = "password123";
        String username = "john.doe";

        User user = User.builder()
                .id(1L)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .password(rawPassword)
                .username(username)
                .build();

        Trainee trainee = Trainee.builder()
                .id(1L)
                .user(user)
                .dateOfBirth(dob)
                .address(address)
                .build();

        when(passwordGenerator.generatePassword()).thenReturn(rawPassword);
        when(usernameGenerator.generateUsername(any(), any())).thenReturn(username);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(traineeRepository.save(any(Trainee.class))).thenReturn(trainee);

        Trainee result = traineeService.createProfile(firstName, lastName, dob, address);

        assertNotNull(result);
        assertEquals(firstName, result.getUser().getFirstName());
        assertEquals(lastName, result.getUser().getLastName());
        assertEquals(username, result.getUser().getUsername());
        assertEquals(rawPassword, result.getUser().getPassword());
        assertEquals(dob, result.getDateOfBirth());
        assertEquals(address, result.getAddress());

        verify(passwordGenerator).generatePassword();
        verify(usernameGenerator).generateUsername(any(), any());
        verify(userRepository).save(any(User.class));
        verify(traineeRepository).save(any(Trainee.class));
        verify(validator).validate(any(Trainee.class
        ));
    }

    @Test
    void selectByUsername_returnsTrainee() {
        String username = "john.doe";
        User user = User.builder().username(username).build();
        Trainee trainee = Trainee.builder().user(user).build();

        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.selectByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUser().getUsername());
    }

    @Test
    void selectByUsername_notFound_throwsException() {
        String username = "notfound";
        when(traineeRepository.findByUser_Username(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> traineeService.selectByUsername(username));
    }

    // Add more tests for updateProfile, deleteByUsername, updateTrainersList as needed
}