package com.epam.gym.mapper;

import com.epam.gym.dto.response.TrainingResponse;
import com.epam.gym.entity.Trainee;
import com.epam.gym.entity.Trainer;
import com.epam.gym.entity.Training;
import com.epam.gym.entity.TrainingType;
import com.epam.gym.entity.User;
import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainingMapper Unit Tests")
class TrainingMapperTest {

    private TrainingMapper trainingMapper;

    private static final Long TRAINING_ID = 1L;
    private static final String TRAINING_NAME = "Morning Workout";
    private static final LocalDate TRAINING_DATE = LocalDate.of(2024, 6, 15);
    private static final Integer TRAINING_DURATION = 60;

    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";
    private static final String TRAINER_USERNAME = "alice.smith";

    private static final String TRAINEE_FIRST_NAME = "John";
    private static final String TRAINEE_LAST_NAME = "Doe";
    private static final String TRAINEE_USERNAME = "john.doe";

    @BeforeEach
    void setUp() {
        trainingMapper = Mappers.getMapper(TrainingMapper.class);
    }

    @Nested
    @DisplayName("toResponse Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map Training to TrainingResponse correctly")
        void toResponse_ValidTraining_MapsCorrectly() {
            Training training = createTraining();

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response).isNotNull();
            assertThat(response.getTrainingName()).isEqualTo(TRAINING_NAME);
            assertThat(response.getTrainingDate()).isEqualTo(TRAINING_DATE);
            assertThat(response.getTrainingType()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getTrainingDuration()).isEqualTo(TRAINING_DURATION);
            assertThat(response.getTrainerName()).isEqualTo(TRAINER_FIRST_NAME + " " + TRAINER_LAST_NAME);
            assertThat(response.getTraineeName()).isEqualTo(TRAINEE_FIRST_NAME + " " + TRAINEE_LAST_NAME);
        }

        @Test
        @DisplayName("Should map training name correctly")
        void toResponse_ValidTraining_MapsTrainingName() {
            Training training = createTrainingWithName("Evening Yoga Session");

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingName()).isEqualTo("Evening Yoga Session");
        }

        @Test
        @DisplayName("Should map training date correctly")
        void toResponse_ValidTraining_MapsTrainingDate() {
            LocalDate specificDate = LocalDate.of(2025, 1, 15);
            Training training = createTrainingWithDate(specificDate);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDate()).isEqualTo(specificDate);
        }

        @Test
        @DisplayName("Should map training type correctly")
        void toResponse_ValidTraining_MapsTrainingType() {
            Training training = createTrainingWithType(TrainingTypeName.YOGA);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingType()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should map training duration correctly")
        void toResponse_ValidTraining_MapsTrainingDuration() {
            Training training = createTrainingWithDuration(90);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDuration()).isEqualTo(90);
        }

        @Test
        @DisplayName("Should map trainer full name correctly")
        void toResponse_ValidTraining_MapsTrainerFullName() {
            Training training = createTrainingWithTrainer();

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainerName()).isEqualTo("Bob Johnson");
        }

        @Test
        @DisplayName("Should map trainee full name correctly")
        void toResponse_ValidTraining_MapsTraineeFullName() {
            Training training = createTrainingWithTrainee();

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTraineeName()).isEqualTo("Jane Wilson");
        }

        @Test
        @DisplayName("Should return null when training is null")
        void toResponse_NullTraining_ReturnsNull() {
            TrainingResponse response = trainingMapper.toResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map all training types correctly")
        void toResponse_AllTrainingTypes_MapsCorrectly() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                Training training = createTrainingWithType(typeName);

                TrainingResponse response = trainingMapper.toResponse(training);

                assertThat(response.getTrainingType()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should map training with minimum duration")
        void toResponse_MinimumDuration_MapsCorrectly() {
            Training training = createTrainingWithDuration(1);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDuration()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should map training with long duration")
        void toResponse_LongDuration_MapsCorrectly() {
            Training training = createTrainingWithDuration(480);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDuration()).isEqualTo(480);
        }

        @Test
        @DisplayName("Should map training with past date")
        void toResponse_PastDate_MapsCorrectly() {
            LocalDate pastDate = LocalDate.of(2020, 1, 1);
            Training training = createTrainingWithDate(pastDate);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDate()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should map training with future date")
        void toResponse_FutureDate_MapsCorrectly() {
            LocalDate futureDate = LocalDate.of(2030, 12, 31);
            Training training = createTrainingWithDate(futureDate);

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingDate()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("Should map training with special characters in name")
        void toResponse_SpecialCharactersInName_MapsCorrectly() {
            Training training = createTrainingWithName("Advanced HIIT - Level 3 (Intense!)");

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainingName()).isEqualTo("Advanced HIIT - Level 3 (Intense!)");
        }

        @Test
        @DisplayName("Should map training with single character names")
        void toResponse_SingleCharacterNames_MapsCorrectly() {
            Training training = createTrainingWithTrainerAndTrainee();

            TrainingResponse response = trainingMapper.toResponse(training);

            assertThat(response.getTrainerName()).isEqualTo("A B");
            assertThat(response.getTraineeName()).isEqualTo("C D");
        }
    }

    @Nested
    @DisplayName("toResponseList Tests")
    class ToResponseListTests {

        @Test
        @DisplayName("Should map list of trainings correctly")
        void toResponseList_ValidList_MapsCorrectly() {
            List<Training> trainings = List.of(
                    createTrainingWithName("Training 1"),
                    createTrainingWithName("Training 2"),
                    createTrainingWithName("Training 3")
            );

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTrainingName()).isEqualTo("Training 1");
            assertThat(responses.get(1).getTrainingName()).isEqualTo("Training 2");
            assertThat(responses.get(2).getTrainingName()).isEqualTo("Training 3");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void toResponseList_EmptyList_ReturnsEmptyList() {
            List<Training> trainings = Collections.emptyList();

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return null when input list is null")
        void toResponseList_NullList_ReturnsNull() {
            List<TrainingResponse> responses = trainingMapper.toResponseList(null);

            assertThat(responses).isNull();
        }

        @Test
        @DisplayName("Should map single item list correctly")
        void toResponseList_SingleItemList_MapsCorrectly() {
            List<Training> trainings = List.of(createTraining());

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().getTrainingName()).isEqualTo(TRAINING_NAME);
        }

        @Test
        @DisplayName("Should map large list correctly")
        void toResponseList_LargeList_MapsCorrectly() {
            List<Training> trainings = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                trainings.add(createTrainingWithName("Training " + i));
            }

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(responses.get(i).getTrainingName()).isEqualTo("Training " + i);
            }
        }

        @Test
        @DisplayName("Should preserve order when mapping list")
        void toResponseList_ValidList_PreservesOrder() {
            List<Training> trainings = List.of(
                    createTrainingWithName("First"),
                    createTrainingWithName("Second"),
                    createTrainingWithName("Third")
            );

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses.get(0).getTrainingName()).isEqualTo("First");
            assertThat(responses.get(1).getTrainingName()).isEqualTo("Second");
            assertThat(responses.get(2).getTrainingName()).isEqualTo("Third");
        }

        @Test
        @DisplayName("Should map list with different training types")
        void toResponseList_DifferentTypes_MapsCorrectly() {
            List<Training> trainings = List.of(
                    createTrainingWithType(TrainingTypeName.FITNESS),
                    createTrainingWithType(TrainingTypeName.YOGA),
                    createTrainingWithType(TrainingTypeName.CARDIO)
            );

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTrainingType()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(responses.get(1).getTrainingType()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(responses.get(2).getTrainingType()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should map list with different dates")
        void toResponseList_DifferentDates_MapsCorrectly() {
            List<Training> trainings = List.of(
                    createTrainingWithDate(LocalDate.of(2024, 1, 1)),
                    createTrainingWithDate(LocalDate.of(2024, 6, 15)),
                    createTrainingWithDate(LocalDate.of(2024, 12, 31))
            );

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTrainingDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            assertThat(responses.get(1).getTrainingDate()).isEqualTo(LocalDate.of(2024, 6, 15));
            assertThat(responses.get(2).getTrainingDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        }

        @Test
        @DisplayName("Should map list with different durations")
        void toResponseList_DifferentDurations_MapsCorrectly() {
            List<Training> trainings = List.of(
                    createTrainingWithDuration(30),
                    createTrainingWithDuration(60),
                    createTrainingWithDuration(90)
            );

            List<TrainingResponse> responses = trainingMapper.toResponseList(trainings);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTrainingDuration()).isEqualTo(30);
            assertThat(responses.get(1).getTrainingDuration()).isEqualTo(60);
            assertThat(responses.get(2).getTrainingDuration()).isEqualTo(90);
        }
    }

    @Nested
    @DisplayName("getFullName Tests")
    class GetFullNameTests {

        @Test
        @DisplayName("Should concatenate first and last name with space")
        void getFullName_ValidNames_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("John", "Doe");

            assertThat(fullName).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should handle single character names")
        void getFullName_SingleCharacterNames_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("A", "B");

            assertThat(fullName).isEqualTo("A B");
        }

        @Test
        @DisplayName("Should handle long names")
        void getFullName_LongNames_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("Christopher", "Bartholomew");

            assertThat(fullName).isEqualTo("Christopher Bartholomew");
        }

        @Test
        @DisplayName("Should handle names with special characters")
        void getFullName_SpecialCharacters_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("Mary-Jane", "O'Connor");

            assertThat(fullName).isEqualTo("Mary-Jane O'Connor");
        }

        @Test
        @DisplayName("Should handle names with accents")
        void getFullName_AccentedNames_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("José", "García");

            assertThat(fullName).isEqualTo("José García");
        }

        @Test
        @DisplayName("Should handle empty first name")
        void getFullName_EmptyFirstName_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("", "Doe");

            assertThat(fullName).isEqualTo(" Doe");
        }

        @Test
        @DisplayName("Should handle empty last name")
        void getFullName_EmptyLastName_ConcatenatesCorrectly() {
            String fullName = trainingMapper.getFullName("John", "");

            assertThat(fullName).isEqualTo("John ");
        }

        @Test
        @DisplayName("Should handle both names empty")
        void getFullName_BothNamesEmpty_ReturnsSpace() {
            String fullName = trainingMapper.getFullName("", "");

            assertThat(fullName).isEqualTo(" ");
        }
    }

    // ==================== Helper Methods ====================

    private Training createTraining() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithName(String name) {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(name)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithDate(LocalDate date) {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(date)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithDuration(Integer duration) {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(duration)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithType(TrainingTypeName typeName) {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, typeName))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithTrainer() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer("Bob", "Johnson"))
                .trainee(createTrainee(TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME))
                .build();
    }

    private Training createTrainingWithTrainee() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer(TRAINER_FIRST_NAME, TRAINER_LAST_NAME))
                .trainee(createTrainee("Jane", "Wilson"))
                .build();
    }

    private Training createTrainingWithTrainerAndTrainee() {
        return Training.builder()
                .id(TRAINING_ID)
                .trainingName(TRAINING_NAME)
                .trainingDate(TRAINING_DATE)
                .trainingDurationMinutes(TRAINING_DURATION)
                .trainingType(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainer(createTrainer("A", "B"))
                .trainee(createTrainee("C", "D"))
                .build();
    }

    private Trainer createTrainer(String firstName, String lastName) {
        User user = User.builder()
                .id(1L)
                .username(TRAINER_USERNAME)
                .password("password123")
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .build();
    }

    private Trainee createTrainee(String firstName, String lastName) {
        User user = User.builder()
                .id(2L)
                .username(TRAINEE_USERNAME)
                .password("password123")
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(1L)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .build();
    }
}