package com.epam.gym.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Trainee} entity.
 * Pure JUnit 5 - no Mockito, no ByteBuddy dependencies.
 */
@DisplayName("Trainee Entity Tests")
class TraineeTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main Street, City, Country";
    private static final String ADDRESS_2 = "456 Oak Avenue, Town, State";

    // ==================== Constructor & Builder Tests ====================

    @Test
    @DisplayName("Should create trainee using no-args constructor")
    void shouldCreateTraineeUsingNoArgsConstructor() {
        // When
        Trainee trainee = new Trainee();

        // Then
        assertNotNull(trainee);
        assertNull(trainee.getId());
        assertNull(trainee.getUser());
        assertNull(trainee.getDateOfBirth());
        assertNull(trainee.getAddress());
        assertNotNull(trainee.getTrainings());
        assertTrue(trainee.getTrainings().isEmpty());
        assertNotNull(trainee.getTrainers());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    @Test
    @DisplayName("Should create trainee using all-args constructor")
    void shouldCreateTraineeUsingAllArgsConstructor() {
        // Given
        User user = createTestUser();
        List<Training> trainings = new ArrayList<>();
        List<Trainer> trainers = new ArrayList<>();

        // When
        Trainee trainee = new Trainee(ID, user, DATE_OF_BIRTH, ADDRESS, trainings, trainers);

        // Then
        assertEquals(ID, trainee.getId());
        assertEquals(user, trainee.getUser());
        assertEquals(DATE_OF_BIRTH, trainee.getDateOfBirth());
        assertEquals(ADDRESS, trainee.getAddress());
        assertEquals(trainings, trainee.getTrainings());
        assertEquals(trainers, trainee.getTrainers());
    }

    @Test
    @DisplayName("Should create trainee using builder")
    void shouldCreateTraineeUsingBuilder() {
        // Given
        User user = createTestUser();

        // When
        Trainee trainee = Trainee.builder()
                .id(ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();

        // Then
        assertEquals(ID, trainee.getId());
        assertEquals(user, trainee.getUser());
        assertEquals(DATE_OF_BIRTH, trainee.getDateOfBirth());
        assertEquals(ADDRESS, trainee.getAddress());
        assertNotNull(trainee.getTrainings());
        assertNotNull(trainee.getTrainers());
    }

    @Test
    @DisplayName("Should create trainee with builder default collections")
    void shouldCreateTraineeWithBuilderDefaultCollections() {
        // When
        Trainee trainee = Trainee.builder()
                .id(ID)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();

        // Then
        assertNotNull(trainee.getTrainings());
        assertNotNull(trainee.getTrainers());
        assertTrue(trainee.getTrainings().isEmpty());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    // ==================== Getter & Setter Tests ====================

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setId(ID);

        // Then
        assertEquals(ID, trainee.getId());
    }

    @Test
    @DisplayName("Should set and get user")
    void shouldSetAndGetUser() {
        // Given
        Trainee trainee = new Trainee();
        User user = createTestUser();

        // When
        trainee.setUser(user);

        // Then
        assertEquals(user, trainee.getUser());
    }

    @Test
    @DisplayName("Should set and get date of birth")
    void shouldSetAndGetDateOfBirth() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setDateOfBirth(DATE_OF_BIRTH);

        // Then
        assertEquals(DATE_OF_BIRTH, trainee.getDateOfBirth());
    }

    @Test
    @DisplayName("Should set and get address")
    void shouldSetAndGetAddress() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setAddress(ADDRESS);

        // Then
        assertEquals(ADDRESS, trainee.getAddress());
    }

    @Test
    @DisplayName("Should set and get trainings list")
    void shouldSetAndGetTrainingsList() {
        // Given
        Trainee trainee = new Trainee();
        List<Training> trainings = new ArrayList<>();
        trainings.add(new Training());

        // When
        trainee.setTrainings(trainings);

        // Then
        assertEquals(trainings, trainee.getTrainings());
        assertEquals(1, trainee.getTrainings().size());
    }

    @Test
    @DisplayName("Should set and get trainers list")
    void shouldSetAndGetTrainersList() {
        // Given
        Trainee trainee = new Trainee();
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(new Trainer());

        // When
        trainee.setTrainers(trainers);

        // Then
        assertEquals(trainers, trainee.getTrainers());
        assertEquals(1, trainee.getTrainers().size());
    }

    // ==================== Collection Manipulation Tests ====================

    @Test
    @DisplayName("Should add training to trainings list")
    void shouldAddTrainingToTrainingsList() {
        // Given
        Trainee trainee = new Trainee();
        Training training = createTestTraining();

        // When
        trainee.getTrainings().add(training);

        // Then
        assertEquals(1, trainee.getTrainings().size());
        assertTrue(trainee.getTrainings().contains(training));
    }

    @Test
    @DisplayName("Should add trainer to trainers list")
    void shouldAddTrainerToTrainersList() {
        // Given
        Trainee trainee = new Trainee();
        Trainer trainer = createTestTrainer();

        // When
        trainee.getTrainers().add(trainer);

        // Then
        assertEquals(1, trainee.getTrainers().size());
        assertTrue(trainee.getTrainers().contains(trainer));
    }

    @Test
    @DisplayName("Should remove training from trainings list")
    void shouldRemoveTrainingFromTrainingsList() {
        // Given
        Trainee trainee = new Trainee();
        Training training = createTestTraining();
        trainee.getTrainings().add(training);

        // When
        trainee.getTrainings().remove(training);

        // Then
        assertTrue(trainee.getTrainings().isEmpty());
    }

    @Test
    @DisplayName("Should remove trainer from trainers list")
    void shouldRemoveTrainerFromTrainersList() {
        // Given
        Trainee trainee = new Trainee();
        Trainer trainer = createTestTrainer();
        trainee.getTrainers().add(trainer);

        // When
        trainee.getTrainers().remove(trainer);

        // Then
        assertTrue(trainee.getTrainers().isEmpty());
    }

    // ==================== Equals & HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // Then
        assertEquals(trainee, trainee);
    }

    @Test
    @DisplayName("Should be equal to another trainee with same id")
    void shouldBeEqualToAnotherTraineeWithSameId() {
        // Given
        Trainee trainee1 = createTestTrainee(ID, ADDRESS);
        Trainee trainee2 = createTestTrainee(ID, ADDRESS_2);

        // Then
        assertEquals(trainee1, trainee2);
    }

    @Test
    @DisplayName("Should not be equal to trainee with different id")
    void shouldNotBeEqualToTraineeWithDifferentId() {
        // Given
        Trainee trainee1 = createTestTrainee(ID, ADDRESS);
        Trainee trainee2 = createTestTrainee(ID_2, ADDRESS);

        // Then
        assertNotEquals(trainee1, trainee2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // Then
        assertNotEquals(null, trainee);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // Then
        assertNotEquals("not a trainee", trainee);
    }

    @Test
    @DisplayName("Should return same hash code for equal objects")
    void shouldReturnSameHashCodeForEqualObjects() {
        // Given
        Trainee trainee1 = createTestTrainee(ID, ADDRESS);
        Trainee trainee2 = createTestTrainee(ID, ADDRESS_2);

        // Then
        assertEquals(trainee1.hashCode(), trainee2.hashCode());
    }

    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);
        int hashCode1 = trainee.hashCode();
        int hashCode2 = trainee.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // When
        String result = trainee.toString();

        // Then
        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should include date of birth in toString")
    void shouldIncludeDateOfBirthInToString() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // When
        String result = trainee.toString();

        // Then
        assertTrue(result.contains("dateOfBirth=" + DATE_OF_BIRTH));
    }

    @Test
    @DisplayName("Should include address in toString")
    void shouldIncludeAddressInToString() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);

        // When
        String result = trainee.toString();

        // Then
        assertTrue(result.contains("address='" + ADDRESS + "'"));
    }

    @Test
    @DisplayName("Should not include user in toString")
    void shouldNotIncludeUserInToString() {
        // Given
        User user = createTestUser();
        Trainee trainee = Trainee.builder()
                .id(ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .build();

        // When
        String result = trainee.toString();

        // Then
        assertFalse(result.contains("user="));
    }

    @Test
    @DisplayName("Should not include collections in toString")
    void shouldNotIncludeCollectionsInToString() {
        // Given
        Trainee trainee = createTestTrainee(ID, ADDRESS);
        trainee.getTrainings().add(new Training());
        trainee.getTrainers().add(new Trainer());

        // When
        String result = trainee.toString();

        // Then
        assertFalse(result.contains("trainings="));
        assertFalse(result.contains("trainers="));
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        Trainee trainee1 = createTestTrainee(null, ADDRESS);
        Trainee trainee2 = createTestTrainee(null, ADDRESS);

        // Then - both have null id, so they should not be equal (business logic)
        assertNotEquals(trainee1, trainee2);
    }

    @Test
    @DisplayName("Should handle null date of birth")
    void shouldHandleNullDateOfBirth() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setDateOfBirth(null);

        // Then
        assertNull(trainee.getDateOfBirth());
    }

    @Test
    @DisplayName("Should handle null address")
    void shouldHandleNullAddress() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setAddress(null);

        // Then
        assertNull(trainee.getAddress());
    }

    @Test
    @DisplayName("Should handle null user")
    void shouldHandleNullUser() {
        // Given
        Trainee trainee = new Trainee();

        // When
        trainee.setUser(null);

        // Then
        assertNull(trainee.getUser());
    }

    // ==================== Helper Methods ====================

    private Trainee createTestTrainee(Long id, String address) {
        return Trainee.builder()
                .id(id)
                .dateOfBirth(TraineeTest.DATE_OF_BIRTH)
                .address(address)
                .build();
    }

    private User createTestUser() {
        // Assuming User has similar builder pattern
        // Adjust according to your actual User class
        User user = new User();
        user.setId(1L);
        // Set other fields if needed
        return user;
    }

    private Training createTestTraining() {
        // Assuming Training has no-args constructor or builder
        // Set fields if needed
        return new Training();
    }

    private Trainer createTestTrainer() {
        // Assuming Trainer has no-args constructor or builder
        // Set fields if needed
        return new Trainer();
    }
}