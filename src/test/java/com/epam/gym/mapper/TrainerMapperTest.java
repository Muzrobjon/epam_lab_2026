package com.epam.gym.mapper;

import com.epam.gym.dto.response.TraineeSummaryResponse;
import com.epam.gym.dto.response.TrainerProfileResponse;
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

class TrainerMapperTest {

    private TrainerMapper trainerMapper;

    @BeforeEach
    void setUp() {
        trainerMapper = Mappers.getMapper(TrainerMapper.class);
    }

    // ==================== toProfileResponse Tests ====================

    @Test
    @DisplayName("Should map Trainer to TrainerProfileResponse with all fields")
    void shouldMapToProfileResponse() {
        // Given
        User user = User.builder()
                .username("jane.smith")
                .firstName("Jane")
                .lastName("Smith")
                .isActive(true)
                .build();

        Trainee trainee = createSampleTrainee("john.doe", "John", "Doe");
        List<Trainee> trainees = new ArrayList<>();
        trainees.add(trainee);

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(createMockTrainingType(TrainingTypeName.YOGA))
                .trainees(trainees)
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("jane.smith", response.getUsername());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertTrue(response.getIsActive());
        assertEquals(TrainingTypeName.YOGA, response.getSpecialization());
        assertNotNull(response.getTrainees());
        assertEquals(1, response.getTrainees().size());

        // Verify nested trainee mapping
        TraineeSummaryResponse traineeResponse = response.getTrainees().get(0);
        assertEquals("john.doe", traineeResponse.getUsername());
        assertEquals("John", traineeResponse.getFirstName());
        assertEquals("Doe", traineeResponse.getLastName());
    }

    @Test
    @DisplayName("Should map Trainer with empty trainees list")
    void shouldMapWithEmptyTraineesList() {
        // Given
        User user = User.builder()
                .username("empty.trainer")
                .firstName("Empty")
                .lastName("Trainer")
                .isActive(false)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(createMockTrainingType(TrainingTypeName.FITNESS))
                .trainees(Collections.emptyList())
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("empty.trainer", response.getUsername());
        assertFalse(response.getIsActive());
        assertEquals(TrainingTypeName.FITNESS, response.getSpecialization());
        assertNotNull(response.getTrainees());
        assertTrue(response.getTrainees().isEmpty());
    }

    @Test
    @DisplayName("Should map Trainer with null trainees")
    void shouldMapWithNullTrainees() {
        // Given
        User user = User.builder()
                .username("null.trainees")
                .firstName("Null")
                .lastName("Trainees")
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(createMockTrainingType(TrainingTypeName.CARDIO))
                .trainees(null)
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("null.trainees", response.getUsername());
        assertNull(response.getTrainees());
    }

    // ==================== toSummaryResponse Tests ====================

    @Test
    @DisplayName("Should map Trainer to TrainerSummaryResponse")
    void shouldMapToSummaryResponse() {
        // Given
        User user = User.builder()
                .username("summary.trainer")
                .firstName("Summary")
                .lastName("Trainer")
                .isActive(true)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(createMockTrainingType(TrainingTypeName.YOGA))
                .build();

        // When
        TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("summary.trainer", response.getUsername());
        assertEquals("Summary", response.getFirstName());
        assertEquals("Trainer", response.getLastName());
        assertEquals(TrainingTypeName.YOGA, response.getSpecialization());
        // Note: isActive is not mapped in toSummaryResponse based on the interface
    }

    @Test
    @DisplayName("Should map Trainer with different specializations")
    void shouldMapTrainerWithDifferentSpecializations() {
        // Test all training types dynamically
        TrainingTypeName[] types = TrainingTypeName.values();

        for (TrainingTypeName type : types) {
            // Given
            User user = User.builder()
                    .username("trainer." + type.name().toLowerCase())
                    .firstName("Trainer")
                    .lastName(type.name())
                    .build();

            Trainer trainer = Trainer.builder()
                    .user(user)
                    .specialization(createMockTrainingType(type))
                    .build();

            // When
            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            // Then
            assertEquals(type, response.getSpecialization(),
                    "Specialization should be mapped correctly for " + type);
        }
    }

    // ==================== toSummaryResponseList Tests ====================

    @Test
    @DisplayName("Should map list of Trainers to list of TrainerSummaryResponse")
    void shouldMapToSummaryResponseList() {
        // Given
        Trainer trainer1 = createSampleTrainer("t1", "One", TrainingTypeName.YOGA);
        Trainer trainer2 = createSampleTrainer("t2", "Two", TrainingTypeName.FITNESS);
        Trainer trainer3 = createSampleTrainer("t3", "Three", TrainingTypeName.CARDIO);

        List<Trainer> trainers = List.of(trainer1, trainer2, trainer3);

        // When
        List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

        // Then
        assertNotNull(responses);
        assertEquals(3, responses.size());

        assertEquals("t1", responses.get(0).getUsername());
        assertEquals("Trainer", responses.get(0).getFirstName());
        assertEquals("One", responses.get(0).getLastName());
        assertEquals(TrainingTypeName.YOGA, responses.get(0).getSpecialization());

        assertEquals("t2", responses.get(1).getUsername());
        assertEquals(TrainingTypeName.FITNESS, responses.get(1).getSpecialization());

        assertEquals("t3", responses.get(2).getUsername());
        assertEquals(TrainingTypeName.CARDIO, responses.get(2).getSpecialization());
    }

    @Test
    @DisplayName("Should handle empty list in toSummaryResponseList")
    void shouldHandleEmptyList() {
        // When
        List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(Collections.emptyList());

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should handle null list in toSummaryResponseList")
    void shouldHandleNullList() {
        // When
        List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(null);

        // Then
        assertNull(responses);
    }

    // ==================== toTraineeSummary Tests ====================

    @Test
    @DisplayName("Should map Trainee to TraineeSummaryResponse")
    void shouldMapTraineeToSummary() {
        // Given
        Trainee trainee = createSampleTrainee("trainee.john", "John", "Doe");

        // When
        TraineeSummaryResponse response = trainerMapper.toTraineeSummary(trainee);

        // Then
        assertNotNull(response);
        assertEquals("trainee.john", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    // ==================== toTraineeSummaryList Tests ====================

    @Test
    @DisplayName("Should map list of Trainees to list of TraineeSummaryResponse")
    void shouldMapTraineeListToSummaryList() {
        // Given
        Trainee trainee1 = createSampleTrainee("t1", "Trainee", "One");
        Trainee trainee2 = createSampleTrainee("t2", "Trainee", "Two");

        List<Trainee> trainees = List.of(trainee1, trainee2);

        // When
        List<TraineeSummaryResponse> responses = trainerMapper.toTraineeSummaryList(trainees);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("t1", responses.get(0).getUsername());
        assertEquals("Trainee", responses.get(0).getFirstName());
        assertEquals("One", responses.get(0).getLastName());

        assertEquals("t2", responses.get(1).getUsername());
        assertEquals("Two", responses.get(1).getLastName());
    }

    @Test
    @DisplayName("Should handle empty trainee list")
    void shouldHandleEmptyTraineeList() {
        // When
        List<TraineeSummaryResponse> responses = trainerMapper.toTraineeSummaryList(Collections.emptyList());

        // Then
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should handle null trainee list")
    void shouldHandleNullTraineeList() {
        // When
        List<TraineeSummaryResponse> responses = trainerMapper.toTraineeSummaryList(null);

        // Then
        assertNull(responses);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle Trainer with null User")
    void shouldHandleNullUser() {
        // Given - Trainer with null User but with specialization
        TrainingType mockTrainingType = mock(TrainingType.class);
        when(mockTrainingType.getId()).thenReturn(1L);
        when(mockTrainingType.getTrainingTypeName()).thenReturn(TrainingTypeName.FITNESS);

        Trainer trainer = Trainer.builder()
                .user(null)
                .specialization(mockTrainingType)
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then - User fields are null, but specialization is still mapped
        assertNotNull(response);
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
        assertNull(response.getIsActive());
        // Specialization is NOT null - it's mapped from trainer.specialization
        assertEquals(TrainingTypeName.FITNESS, response.getSpecialization());
    }

    @Test
    @DisplayName("Should handle Trainer with null User and null specialization")
    void shouldHandleNullUserAndNullSpecialization() {
        // Given
        Trainer trainer = Trainer.builder()
                .user(null)
                .specialization(null)
                .build();

        // When
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
        assertNull(response.getIsActive());
        assertNull(response.getSpecialization());
    }

    @Test
    @DisplayName("Should handle Trainer with null specialization only")
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
        TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

        // Then
        assertNotNull(response);
        assertEquals("no.spec", response.getUsername());
        assertNull(response.getSpecialization());
    }

    @Test
    @DisplayName("Should handle Trainee with null User in toTraineeSummary")
    void shouldHandleNullUserInTraineeSummary() {
        // Given
        Trainee trainee = Trainee.builder()
                .user(null)
                .dateOfBirth(LocalDate.now())
                .address("Address")
                .build();

        // When
        TraineeSummaryResponse response = trainerMapper.toTraineeSummary(trainee);

        // Then
        assertNotNull(response);
        assertNull(response.getUsername());
        assertNull(response.getFirstName());
        assertNull(response.getLastName());
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

    private Trainer createSampleTrainer(String username, String lastName,
                                        TrainingTypeName specialization) {
        User user = User.builder()
                .username(username)
                .firstName("Trainer")
                .lastName(lastName)
                .isActive(true)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(createMockTrainingType(specialization))
                .build();
    }

    private TrainingType createMockTrainingType(TrainingTypeName specialization) {
        TrainingType mock = mock(TrainingType.class);
        when(mock.getId()).thenReturn(1L);
        when(mock.getTrainingTypeName()).thenReturn(specialization);
        return mock;
    }
}