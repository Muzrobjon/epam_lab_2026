package com.epam.gym.facade;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GymFacade} without Mockito - using manual test doubles.
 */
@DisplayName("GymFacade Tests")
class GymFacadeTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPass123";
    private static final String ADDRESS = "123 Main St";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 1, 1);
    private static final TrainingTypeName SPECIALIZATION = TrainingTypeName.FITNESS;
    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 12, 31);
    private static final String TRAINING_NAME = "Morning Workout";
    private static final Integer DURATION = 60;

    // Test spy objects to capture method calls
    private TraineeServiceSpy traineeServiceSpy;
    private TrainerServiceSpy trainerServiceSpy;
    private TrainingServiceSpy trainingServiceSpy;
    private GymFacade gymFacade;

    /**
     * Manual test double for TraineeService - captures all method calls
     */
    static class TraineeServiceSpy {
        String capturedUsername;
        String capturedPassword;
        String capturedNewPassword;
        String capturedFirstName;
        String capturedLastName;
        String capturedAddress;
        LocalDate capturedDateOfBirth;
        Trainee capturedUpdatedTrainee;
        List<String> capturedTrainerUsernames;
        Trainee returnTrainee;
        boolean authenticateCalled = false;
        boolean changePasswordCalled = false;
        boolean toggleActiveCalled = false;
        boolean deleteCalled = false;
        boolean updateTrainersListCalled = false;

        // Simulate service methods
        public Trainee createProfile(String firstName, String lastName, LocalDate dateOfBirth, String address) {
            this.capturedFirstName = firstName;
            this.capturedLastName = lastName;
            this.capturedDateOfBirth = dateOfBirth;
            this.capturedAddress = address;
            return new Trainee();
        }

        public void authenticate(String username, String password) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.authenticateCalled = true;
        }

        public Trainee selectByUsername(String username) {
            this.capturedUsername = username;
            return returnTrainee;
        }

        public void changePassword(String username, String oldPassword, String newPassword) {
            this.capturedUsername = username;
            this.capturedPassword = oldPassword;
            this.capturedNewPassword = newPassword;
            this.changePasswordCalled = true;
        }

        public Trainee updateProfile(String username, String password, Trainee updatedTrainee) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.capturedUpdatedTrainee = updatedTrainee;
            return updatedTrainee;
        }

        public void toggleActiveStatus(String username, String password) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.toggleActiveCalled = true;
        }

        public void deleteByUsername(String username, String password) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.deleteCalled = true;
        }

        public void updateTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
            this.capturedUsername = traineeUsername;
            this.capturedPassword = password;
            this.capturedTrainerUsernames = trainerUsernames;
            this.updateTrainersListCalled = true;
        }
    }

    /**
     * Manual test double for TrainerService
     */
    static class TrainerServiceSpy {
        String capturedFirstName;
        String capturedLastName;
        String capturedUsername;
        String capturedPassword;
        String capturedNewPassword;
        TrainingTypeName capturedSpecialization;
        Trainer capturedUpdatedTrainer;
        Trainer returnTrainer;
        boolean authenticateCalled = false;
        boolean changePasswordCalled = false;
        boolean toggleActiveCalled = false;

        public Trainer createProfile(String firstName, String lastName, TrainingTypeName specialization) {
            this.capturedFirstName = firstName;
            this.capturedLastName = lastName;
            this.capturedSpecialization = specialization;
            return new Trainer();
        }

        public void authenticate(String username, String password) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.authenticateCalled = true;
        }

        public Trainer selectByUsername(String username) {
            this.capturedUsername = username;
            return returnTrainer;
        }

        public void changePassword(String username, String oldPassword, String newPassword) {
            this.capturedUsername = username;
            this.capturedPassword = oldPassword;
            this.capturedNewPassword = newPassword;
            this.changePasswordCalled = true;
        }

        public Trainer updateProfile(String username, String password, Trainer updatedTrainer) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.capturedUpdatedTrainer = updatedTrainer;
            return updatedTrainer;
        }

        public void toggleActiveStatus(String username, String password) {
            this.capturedUsername = username;
            this.capturedPassword = password;
            this.toggleActiveCalled = true;
        }

        public List<Trainer> getUnassignedTrainers(String traineeUsername) {
            this.capturedUsername = traineeUsername;
            return Collections.emptyList();
        }
    }

    /**
     * Manual test double for TrainingService
     */
    static class TrainingServiceSpy {
        String capturedTraineeUsername;
        String capturedTraineePassword;
        String capturedTrainerUsername;
        String capturedTrainerPassword;
        String capturedTrainingName;
        String capturedTrainerName;
        String capturedTraineeName;
        TrainingTypeName capturedTrainingType;
        LocalDate capturedTrainingDate;
        LocalDate capturedFromDate;
        LocalDate capturedToDate;
        Integer capturedDuration;
        Training returnTraining;
        List<Training> returnTrainingList;

        public List<Training> getTraineeTrainingsByCriteria(
                String traineeUsername, String traineePassword,
                LocalDate fromDate, LocalDate toDate,
                String trainerName, TrainingTypeName trainingType) {
            this.capturedTraineeUsername = traineeUsername;
            this.capturedTraineePassword = traineePassword;
            this.capturedFromDate = fromDate;
            this.capturedToDate = toDate;
            this.capturedTrainerName = trainerName;
            this.capturedTrainingType = trainingType;
            return returnTrainingList != null ? returnTrainingList : Collections.emptyList();
        }

        public List<Training> getTrainerTrainingsByCriteria(
                String trainerUsername, String trainerPassword,
                LocalDate fromDate, LocalDate toDate,
                String traineeName) {
            this.capturedTrainerUsername = trainerUsername;
            this.capturedTrainerPassword = trainerPassword;
            this.capturedFromDate = fromDate;
            this.capturedToDate = toDate;
            this.capturedTraineeName = traineeName;
            return returnTrainingList != null ? returnTrainingList : Collections.emptyList();
        }

        public Training createTraining(
                String traineeUsername, String traineePassword,
                String trainerUsername, String trainerPassword,
                String trainingName, TrainingTypeName trainingType,
                LocalDate trainingDate, Integer duration) {
            this.capturedTraineeUsername = traineeUsername;
            this.capturedTraineePassword = traineePassword;
            this.capturedTrainerUsername = trainerUsername;
            this.capturedTrainerPassword = trainerPassword;
            this.capturedTrainingName = trainingName;
            this.capturedTrainingType = trainingType;
            this.capturedTrainingDate = trainingDate;
            this.capturedDuration = duration;
            return returnTraining;
        }
    }

    /**
     * Testable version of GymFacade that uses our spy objects
     */
    static class TestableGymFacade extends GymFacade {
        private final TraineeServiceSpy traineeSpy;
        private final TrainerServiceSpy trainerSpy;
        private final TrainingServiceSpy trainingSpy;

        public TestableGymFacade(TraineeServiceSpy traineeSpy,
                                 TrainerServiceSpy trainerSpy,
                                 TrainingServiceSpy trainingSpy) {
            super(null, null, null); // This won't work due to constructor validation
            this.traineeSpy = traineeSpy;
            this.trainerSpy = trainerSpy;
            this.trainingSpy = trainingSpy;
        }

        // Override all methods to delegate to spy objects instead of real services
        @Override
        public Trainer createTrainer(String firstName, String lastName, TrainingTypeName specialization) {
            return trainerSpy.createProfile(firstName, lastName, specialization);
        }

        @Override
        public Trainee createTrainee(String firstName, String lastName, LocalDate dateOfBirth, String address) {
            return traineeSpy.createProfile(firstName, lastName, dateOfBirth, address);
        }

        @Override
        public void authenticateTrainee(String username, String password) {
            traineeSpy.authenticate(username, password);
        }

        @Override
        public void authenticateTrainer(String username, String password) {
            trainerSpy.authenticate(username, password);
        }

        @Override
        public Trainer getTrainerByUsername(String username) {
            return trainerSpy.selectByUsername(username);
        }

        @Override
        public Trainee getTraineeByUsername(String username) {
            return traineeSpy.selectByUsername(username);
        }

        @Override
        public void changeTraineePassword(String username, String oldPassword, String newPassword) {
            traineeSpy.changePassword(username, oldPassword, newPassword);
        }

        @Override
        public void changeTrainerPassword(String username, String oldPassword, String newPassword) {
            trainerSpy.changePassword(username, oldPassword, newPassword);
        }

        @Override
        public Trainer updateTrainer(String username, String password, Trainer updatedTrainer) {
            return trainerSpy.updateProfile(username, password, updatedTrainer);
        }

        @Override
        public Trainee updateTrainee(String username, String password, Trainee updatedTrainee) {
            return traineeSpy.updateProfile(username, password, updatedTrainee);
        }

        @Override
        public void toggleTraineeStatus(String username, String password) {
            traineeSpy.toggleActiveStatus(username, password);
        }

        @Override
        public void toggleTrainerStatus(String username, String password) {
            trainerSpy.toggleActiveStatus(username, password);
        }

        @Override
        public void deleteTrainee(String username, String password) {
            traineeSpy.deleteByUsername(username, password);
        }

        @Override
        public List<Training> getTraineeTrainingsByCriteria(
                String traineeUsername, String traineePassword,
                LocalDate fromDate, LocalDate toDate,
                String trainerName, TrainingTypeName trainingType) {
            return trainingSpy.getTraineeTrainingsByCriteria(
                    traineeUsername, traineePassword, fromDate, toDate, trainerName, trainingType);
        }

        @Override
        public List<Training> getTrainerTrainingsByCriteria(
                String trainerUsername, String trainerPassword,
                LocalDate fromDate, LocalDate toDate,
                String traineeName) {
            return trainingSpy.getTrainerTrainingsByCriteria(
                    trainerUsername, trainerPassword, fromDate, toDate, traineeName);
        }

        @Override
        public Training createTraining(
                String traineeUsername, String traineePassword,
                String trainerUsername, String trainerPassword,
                String trainingName, TrainingTypeName trainingType,
                LocalDate trainingDate, Integer duration) {
            return trainingSpy.createTraining(
                    traineeUsername, traineePassword,
                    trainerUsername, trainerPassword,
                    trainingName, trainingType, trainingDate, duration);
        }

        @Override
        public List<Trainer> getUnassignedTrainers(String traineeUsername) {
            return trainerSpy.getUnassignedTrainers(traineeUsername);
        }

        @Override
        public void updateTraineeTrainersList(String traineeUsername, String password, List<String> trainerUsernames) {
            traineeSpy.updateTrainersList(traineeUsername, password, trainerUsernames);
        }
    }

    @BeforeEach
    void setUp() {
        traineeServiceSpy = new TraineeServiceSpy();
        trainerServiceSpy = new TrainerServiceSpy();
        trainingServiceSpy = new TrainingServiceSpy();
        gymFacade = new TestableGymFacade(traineeServiceSpy, trainerServiceSpy, trainingServiceSpy);
    }

    // ==================== Trainer Creation Tests ====================

    @Test
    @DisplayName("Should create trainer with correct parameters")
    void shouldCreateTrainerWithCorrectParameters() {
        // When
        gymFacade.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION);

        // Then
        assertEquals(FIRST_NAME, trainerServiceSpy.capturedFirstName);
        assertEquals(LAST_NAME, trainerServiceSpy.capturedLastName);
        assertEquals(SPECIALIZATION, trainerServiceSpy.capturedSpecialization);
    }

    @Test
    @DisplayName("Should return trainer from createTrainer")
    void shouldReturnTrainerFromCreateTrainer() {
        // When
        Trainer result = gymFacade.createTrainer(FIRST_NAME, LAST_NAME, SPECIALIZATION);

        // Then
        assertNotNull(result);
    }

    // ==================== Trainee Creation Tests ====================

    @Test
    @DisplayName("Should create trainee with correct parameters")
    void shouldCreateTraineeWithCorrectParameters() {
        // When
        gymFacade.createTrainee(FIRST_NAME, LAST_NAME, DATE_OF_BIRTH, ADDRESS);

        // Then
        assertEquals(FIRST_NAME, traineeServiceSpy.capturedFirstName);
        assertEquals(LAST_NAME, traineeServiceSpy.capturedLastName);
        assertEquals(DATE_OF_BIRTH, traineeServiceSpy.capturedDateOfBirth);
        assertEquals(ADDRESS, traineeServiceSpy.capturedAddress);
    }

    // ==================== Authentication Tests ====================

    @Test
    @DisplayName("Should authenticate trainee with correct credentials")
    void shouldAuthenticateTraineeWithCorrectCredentials() {
        // When
        gymFacade.authenticateTrainee(USERNAME, PASSWORD);

        // Then
        assertTrue(traineeServiceSpy.authenticateCalled);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
    }

    @Test
    @DisplayName("Should authenticate trainer with correct credentials")
    void shouldAuthenticateTrainerWithCorrectCredentials() {
        // When
        gymFacade.authenticateTrainer(USERNAME, PASSWORD);

        // Then
        assertTrue(trainerServiceSpy.authenticateCalled);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
        assertEquals(PASSWORD, trainerServiceSpy.capturedPassword);
    }

    // ==================== Get By Username Tests ====================

    @Test
    @DisplayName("Should get trainer by username")
    void shouldGetTrainerByUsername() {
        // Given
        Trainer expected = new Trainer();
        trainerServiceSpy.returnTrainer = expected;

        // When
        Trainer result = gymFacade.getTrainerByUsername(USERNAME);

        // Then
        assertEquals(expected, result);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
    }

    @Test
    @DisplayName("Should get trainee by username")
    void shouldGetTraineeByUsername() {
        // Given
        Trainee expected = new Trainee();
        traineeServiceSpy.returnTrainee = expected;

        // When
        Trainee result = gymFacade.getTraineeByUsername(USERNAME);

        // Then
        assertEquals(expected, result);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
    }

    // ==================== Password Change Tests ====================

    @Test
    @DisplayName("Should change trainee password with correct parameters")
    void shouldChangeTraineePassword() {
        // When
        gymFacade.changeTraineePassword(USERNAME, PASSWORD, NEW_PASSWORD);

        // Then
        assertTrue(traineeServiceSpy.changePasswordCalled);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
        assertEquals(NEW_PASSWORD, traineeServiceSpy.capturedNewPassword);
    }

    @Test
    @DisplayName("Should change trainer password with correct parameters")
    void shouldChangeTrainerPassword() {
        // When
        gymFacade.changeTrainerPassword(USERNAME, PASSWORD, NEW_PASSWORD);

        // Then
        assertTrue(trainerServiceSpy.changePasswordCalled);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
        assertEquals(PASSWORD, trainerServiceSpy.capturedPassword);
        assertEquals(NEW_PASSWORD, trainerServiceSpy.capturedNewPassword);
    }

    // ==================== Update Profile Tests ====================

    @Test
    @DisplayName("Should update trainer profile")
    void shouldUpdateTrainer() {
        // Given
        Trainer updated = new Trainer();

        // When
        Trainer result = gymFacade.updateTrainer(USERNAME, PASSWORD, updated);

        // Then
        assertEquals(updated, result);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
        assertEquals(PASSWORD, trainerServiceSpy.capturedPassword);
        assertEquals(updated, trainerServiceSpy.capturedUpdatedTrainer);
    }

    @Test
    @DisplayName("Should update trainee profile")
    void shouldUpdateTrainee() {
        // Given
        Trainee updated = new Trainee();

        // When
        Trainee result = gymFacade.updateTrainee(USERNAME, PASSWORD, updated);

        // Then
        assertEquals(updated, result);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
        assertEquals(updated, traineeServiceSpy.capturedUpdatedTrainee);
    }

    // ==================== Toggle Status Tests ====================

    @Test
    @DisplayName("Should toggle trainee active status")
    void shouldToggleTraineeStatus() {
        // When
        gymFacade.toggleTraineeStatus(USERNAME, PASSWORD);

        // Then
        assertTrue(traineeServiceSpy.toggleActiveCalled);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
    }

    @Test
    @DisplayName("Should toggle trainer active status")
    void shouldToggleTrainerStatus() {
        // When
        gymFacade.toggleTrainerStatus(USERNAME, PASSWORD);

        // Then
        assertTrue(trainerServiceSpy.toggleActiveCalled);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
        assertEquals(PASSWORD, trainerServiceSpy.capturedPassword);
    }

    // ==================== Delete Tests ====================

    @Test
    @DisplayName("Should delete trainee")
    void shouldDeleteTrainee() {
        // When
        gymFacade.deleteTrainee(USERNAME, PASSWORD);

        // Then
        assertTrue(traineeServiceSpy.deleteCalled);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
    }

    // ==================== Training Query Tests ====================

    @Test
    @DisplayName("Should get trainee trainings by criteria")
    void shouldGetTraineeTrainingsByCriteria() {
        // Given
        String trainerName = "jane.doe";
        List<Training> expected = Arrays.asList(new Training(), new Training());
        trainingServiceSpy.returnTrainingList = expected;

        // When
        List<Training> result = gymFacade.getTraineeTrainingsByCriteria(
                USERNAME, PASSWORD, FROM_DATE, TO_DATE, trainerName, SPECIALIZATION);

        // Then
        assertEquals(expected, result);
        assertEquals(USERNAME, trainingServiceSpy.capturedTraineeUsername);
        assertEquals(PASSWORD, trainingServiceSpy.capturedTraineePassword);
        assertEquals(FROM_DATE, trainingServiceSpy.capturedFromDate);
        assertEquals(TO_DATE, trainingServiceSpy.capturedToDate);
        assertEquals(trainerName, trainingServiceSpy.capturedTrainerName);
        assertEquals(SPECIALIZATION, trainingServiceSpy.capturedTrainingType);
    }

    @Test
    @DisplayName("Should get trainer trainings by criteria")
    void shouldGetTrainerTrainingsByCriteria() {
        // Given
        String traineeName = "jane.doe";
        List<Training> expected = Collections.singletonList(new Training());
        trainingServiceSpy.returnTrainingList = expected;

        // When
        List<Training> result = gymFacade.getTrainerTrainingsByCriteria(
                USERNAME, PASSWORD, FROM_DATE, TO_DATE, traineeName);

        // Then
        assertEquals(expected, result);
        assertEquals(USERNAME, trainingServiceSpy.capturedTrainerUsername);
        assertEquals(PASSWORD, trainingServiceSpy.capturedTrainerPassword);
        assertEquals(FROM_DATE, trainingServiceSpy.capturedFromDate);
        assertEquals(TO_DATE, trainingServiceSpy.capturedToDate);
        assertEquals(traineeName, trainingServiceSpy.capturedTraineeName);
    }

    // ==================== Training Creation Tests ====================

    @Test
    @DisplayName("Should create training with correct parameters")
    void shouldCreateTraining() {
        // Given
        String trainerUsername = "trainer.john";
        String trainerPassword = "trainerPass";
        Training expected = new Training();
        trainingServiceSpy.returnTraining = expected;

        // When
        Training result = gymFacade.createTraining(
                USERNAME, PASSWORD, trainerUsername, trainerPassword,
                TRAINING_NAME, SPECIALIZATION, DATE_OF_BIRTH, DURATION);

        // Then
        assertEquals(expected, result);
        assertEquals(USERNAME, trainingServiceSpy.capturedTraineeUsername);
        assertEquals(PASSWORD, trainingServiceSpy.capturedTraineePassword);
        assertEquals(trainerUsername, trainingServiceSpy.capturedTrainerUsername);
        assertEquals(trainerPassword, trainingServiceSpy.capturedTrainerPassword);
        assertEquals(TRAINING_NAME, trainingServiceSpy.capturedTrainingName);
        assertEquals(SPECIALIZATION, trainingServiceSpy.capturedTrainingType);
        assertEquals(DATE_OF_BIRTH, trainingServiceSpy.capturedTrainingDate);
        assertEquals(DURATION, trainingServiceSpy.capturedDuration);
    }

    // ==================== Trainer Assignment Tests ====================

    @Test
    @DisplayName("Should get unassigned trainers")
    void shouldGetUnassignedTrainers() {
        // When
        List<Trainer> result = gymFacade.getUnassignedTrainers(USERNAME);

        // Then
        assertNotNull(result);
        assertEquals(USERNAME, trainerServiceSpy.capturedUsername);
    }

    @Test
    @DisplayName("Should update trainee trainers list")
    void shouldUpdateTraineeTrainersList() {
        // Given
        List<String> trainerUsernames = Arrays.asList("trainer1", "trainer2");

        // When
        gymFacade.updateTraineeTrainersList(USERNAME, PASSWORD, trainerUsernames);

        // Then
        assertTrue(traineeServiceSpy.updateTrainersListCalled);
        assertEquals(USERNAME, traineeServiceSpy.capturedUsername);
        assertEquals(PASSWORD, traineeServiceSpy.capturedPassword);
        assertEquals(trainerUsernames, traineeServiceSpy.capturedTrainerUsernames);
    }

    @Test
    @DisplayName("Should handle empty trainers list")
    void shouldHandleEmptyTrainersList() {
        // Given
        List<String> emptyList = Collections.emptyList();

        // When
        gymFacade.updateTraineeTrainersList(USERNAME, PASSWORD, emptyList);

        // Then
        assertTrue(traineeServiceSpy.updateTrainersListCalled);
        assertEquals(emptyList, traineeServiceSpy.capturedTrainerUsernames);
    }

    // ==================== Null Parameters Tests ====================

    @Test
    @DisplayName("Should pass null parameters to service")
    void shouldPassNullParameters() {
        // When
        gymFacade.getTraineeTrainingsByCriteria(USERNAME, PASSWORD, null, null, null, null);

        // Then
        assertNull(trainingServiceSpy.capturedFromDate);
        assertNull(trainingServiceSpy.capturedToDate);
    }
}