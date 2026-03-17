package com.epam.gym.mapper;

import com.epam.gym.dto.response.TraineeProfileResponse;
import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerSummaryResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TraineeMapperTest {

    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        // Get the MapStruct generated implementation
        traineeMapper = Mappers.getMapper(TraineeMapper.class);
    }

    // ==================== toProfileResponse Tests ====================

    @Test
    @DisplayName("Should map Trainee to TraineeProfileResponse with all fields")
    void shouldMapToProfileResponse() {
        // Given
        User user = User.builder()
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        Trainer trainer = createSampleTrainer("jane.smith", "Jane", "Smith", TrainingTypeName.YOGA);
        List<Trainer> trainers = new ArrayList<>();
        trainers.add(trainer);

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .address("123 Main St, New York")
                .trainers(trainers)
                .build();

        // When
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        // Then
        assertNotNull(response);
        assertEquals("john.doe", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertTrue(response.getIsActive());
        assertNotNull(response.getTrainers());
        assertEquals(1, response.getTrainers().size());

        // Verify nested trainer mapping
        TrainerSummaryResponse trainerResponse = response.getTrainers().get(0);
        assertEquals("jane.smith", trainerResponse.getUsername());
        assertEquals("Jane", trainerResponse.getFirstName());
        assertEquals("Smith", trainerResponse.getLastName());
        assertEquals(TrainingTypeName.YOGA, trainerResponse.getSpecialization());
    }

    @Test
    @DisplayName("Should map Trainee with empty trainers list")
    void shouldMapWithEmptyTrainersList() {
        // Given
        User user = User.builder()
                .username("empty.trainee")
                .firstName("Empty")
                .lastName("Trainee")
                .isActive(false)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .address("456 Empty Ave")
                .trainers(Collections.emptyList())
                .build();

        // When
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        // Then
        assertNotNull(response);
        assertEquals("empty.trainee", response.getUsername());
        assertFalse(response.getIsActive());
        assertNotNull(response.getTrainers());
        assertTrue(response.getTrainers().isEmpty());
    }

    @Test
    @DisplayName("Should map Trainee with null trainers")
    void shouldMapWithNullTrainers() {
        // Given
        User user = User.builder()
                .username("null.trainers")
                .firstName("Null")
                .lastName("Trainers")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1988, 3, 10))
                .address("789 Null Blvd")
                .trainers(null)
                .build();

        // When
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        // Then
        assertNotNull(response);
        assertEquals("null.trainers", response.getUsername());
        // Trainers list should be null when source is null
        assertNull(response.getTrainers());
    }

    // ==================== toSummaryResponse Tests ====================

    @Test
    @DisplayName("Should map Trainee to TraineeSummaryResponse")
    void shouldMapToSummaryResponse() {
        // Given
        User user = User.builder()
                .username("summary.user")
                .firstName("Summary")
                .lastName("User")
                .isActive(true)
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1992, 7, 25))
                .address("321 Summary Lane")
                .build();

        // When
        TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

        // Then
        assertNotNull(response);
        assertEquals("summary.user", response.getUsername());
        assertEquals("Summary", response.getFirstName());
        assertEquals("User", response.getLastName());
        // Note: isActive is not mapped in toSummaryResponse based on the interface
    }

    // ==================== toSummaryResponseList Tests ====================

    @Test
    @DisplayName("Should map list of Trainees to list of TraineeSummaryResponse")
    void shouldMapToSummaryResponseList() {
        // Given
        Trainee trainee1 = createSampleTrainee("user1", "First1", "Last1");
        Trainee trainee2 = createSampleTrainee("user2", "First2", "Last2");
        Trainee trainee3 = createSampleTrainee("user3", "First3", "Last3");

        List<Trainee> trainees = List.of(trainee1, trainee2, trainee3);

        // When
        List<TraineeSummaryResponse> responses = traineeMapper.toSummaryResponseList(trainees);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        assertEquals("user1", responses.get(0).getUsername());
        assertEquals("First1", responses.get(0).getFirstName());
        assertEquals("Last1", responses.get(0).getLastName());

        assertEquals("user2", responses.get(1).getUsername());
        assertEquals("user3", responses.get(2).getUsername());
    }

    @Test
    @DisplayName("Should handle empty list in toSummaryResponseList")
    void shouldHandleEmptyList() {
        // When
        List<TraineeSummaryResponse> responses = traineeMapper.toSummaryResponseList(Collections.emptyList());

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should handle null list in toSummaryResponseList")
    void shouldHandleNullList() {
        // When
        List<TraineeSummaryResponse> responses = traineeMapper.toSummaryResponseList(null);

        // Then
        assertNull(responses);
    }

    // ==================== toTrainerSummary Tests ====================

    @Test
    @DisplayName("Should map Trainer to TrainerSummaryResponse")
    void shouldMapTrainerToSummary() {
        // Given
        Trainer trainer = createSampleTrainer("trainer.pro", "Pro", "Trainer", TrainingTypeName.FITNESS);

        // When
        TrainerSummaryResponse response = traineeMapper.toTrainerSummary(trainer);

        // Then
        assertNotNull(response);
        assertEquals("trainer.pro", response.getUsername());
        assertEquals("Pro", response.getFirstName());
        assertEquals("Trainer", response.getLastName());
        assertEquals(TrainingTypeName.FITNESS, response.getSpecialization());
    }

    @Test
    @DisplayName("Should map Trainer with different specializations")
    void shouldMapTrainerWithDifferentSpecializations() {
        // Test all training types dynamically using values()
        TrainingTypeName[] types = TrainingTypeName.values();

        for (TrainingTypeName type : types) {
            // Given
            Trainer trainer = createSampleTrainer("trainer." + type.name().toLowerCase(),
                    "Trainer", type.name(), type);

            // When
            TrainerSummaryResponse response = traineeMapper.toTrainerSummary(trainer);

            // Then
            assertEquals(type, response.getSpecialization(),
                    "Specialization should be mapped correctly for " + type);
        }
    }

    // ==================== toTrainerSummaryList Tests ====================

    @Test
    @DisplayName("Should map list of Trainers to list of TrainerSummaryResponse")
    void shouldMapTrainerListToSummaryList() {
        // Given - use only enum values that actually exist
        Trainer trainer1 = createSampleTrainer("t1", "Trainer", "One", TrainingTypeName.YOGA);
        Trainer trainer2 = createSampleTrainer("t2", "Trainer", "Two", TrainingTypeName.FITNESS);

        List<Trainer> trainers = List.of(trainer1, trainer2);

        // When
        List<TrainerSummaryResponse> responses = traineeMapper.toTrainerSummaryList(trainers);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("t1", responses.get(0).getUsername());
        assertEquals(TrainingTypeName.YOGA, responses.get(0).getSpecialization());

        assertEquals("t2", responses.get(1).getUsername());
        assertEquals(TrainingTypeName.FITNESS, responses.get(1).getSpecialization());
    }

    @Test
    @DisplayName("Should handle empty trainer list")
    void shouldHandleEmptyTrainerList() {
        // When
        List<TrainerSummaryResponse> responses = traineeMapper.toTrainerSummaryList(Collections.emptyList());

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should handle null trainer list")
    void shouldHandleNullTrainerList() {
        // When
        List<TrainerSummaryResponse> responses = traineeMapper.toTrainerSummaryList(null);

        // Then
        assertNull(responses);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle Trainee with null User")
    void shouldHandleNullUser() {
        // Given
        Trainee trainee = Trainee.builder()
                .user(null)
                .dateOfBirth(LocalDate.now())
                .address("Address")
                .build();

        // When
        TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

        // Then - MapStruct will handle null source by setting null targets
        assertNotNull(response);
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
        assertNull(response.getIsActive());
    }

    @Test
    @DisplayName("Should handle Trainer with null specialization")
    void shouldHandleNullSpecialization() {
        // Given
        User user = User.builder()
                .username("no.spec")
                .firstName("No")
                .lastName("Specialization")
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(null)
                .build();

        // When
        TrainerSummaryResponse response = traineeMapper.toTrainerSummary(trainer);

        // Then
        assertNotNull(response);
        assertEquals("no.spec", response.getUsername());
        assertNull(response.getSpecialization());
    }

    // ==================== Helper Methods ====================

    private Trainee createSampleTrainee(String username, String firstName, String lastName) {
        User user = User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Test St")
                .build();
    }

    private Trainer createSampleTrainer(String username, String firstName, String lastName,
                                        TrainingTypeName specialization) {
        User user = User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        // Use mock instead of builder since TrainingType has no @Builder
        TrainingType trainingType = mock(TrainingType.class);
        when(trainingType.getId()).thenReturn(1L);
        when(trainingType.getTrainingTypeName()).thenReturn(specialization);

        return Trainer.builder()
                .user(user)
                .specialization(trainingType)
                .build();
    }
}