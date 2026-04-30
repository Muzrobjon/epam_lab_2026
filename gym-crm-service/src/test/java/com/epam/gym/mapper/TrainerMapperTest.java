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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerMapper Unit Tests")
class TrainerMapperTest {

    private TrainerMapper trainerMapper;

    private static final Long TRAINER_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "alice.smith";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "Alice";
    private static final String LAST_NAME = "Smith";
    private static final Boolean IS_ACTIVE = true;

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINEE_FIRST_NAME = "John";
    private static final String TRAINEE_LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        trainerMapper = Mappers.getMapper(TrainerMapper.class);
    }

    @Nested
    @DisplayName("toProfileResponse Tests")
    class ToProfileResponseTests {

        @Test
        @DisplayName("Should map Trainer to TrainerProfileResponse correctly")
        void toProfileResponse_ValidTrainer_MapsCorrectly() {
            Trainer trainer = createTrainer();

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(LAST_NAME);
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getIsActive()).isEqualTo(IS_ACTIVE);
        }

        @Test
        @DisplayName("Should map username from nested user object")
        void toProfileResponse_ValidTrainer_MapsUsername() {
            Trainer trainer = createTrainerWithUsername("bob.johnson");

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getUsername()).isEqualTo("bob.johnson");
        }

        @Test
        @DisplayName("Should map first name from nested user object")
        void toProfileResponse_ValidTrainer_MapsFirstName() {
            Trainer trainer = createTrainerWithName("Robert", LAST_NAME);

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getFirstName()).isEqualTo("Robert");
        }

        @Test
        @DisplayName("Should map last name from nested user object")
        void toProfileResponse_ValidTrainer_MapsLastName() {
            Trainer trainer = createTrainerWithName(FIRST_NAME, "Johnson");

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getLastName()).isEqualTo("Johnson");
        }

        @Test
        @DisplayName("Should map specialization from nested training type")
        void toProfileResponse_ValidTrainer_MapsSpecialization() {
            Trainer trainer = createTrainerWithSpecialization(TrainingTypeName.YOGA);

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should map isActive status correctly when true")
        void toProfileResponse_ActiveTrainer_MapsIsActiveTrue() {
            Trainer trainer = createTrainerWithActiveStatus(true);

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should map isActive status correctly when false")
        void toProfileResponse_InactiveTrainer_MapsIsActiveFalse() {
            Trainer trainer = createTrainerWithActiveStatus(false);

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should map empty trainees list")
        void toProfileResponse_NoTrainees_MapsEmptyList() {
            Trainer trainer = createTrainer();

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getTrainees()).isNotNull();
            assertThat(response.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should map trainees list correctly")
        void toProfileResponse_WithTrainees_MapsTraineesList() {
            Trainer trainer = createTrainerWithTrainees(List.of(
                    createTrainee("john.doe", "John", "Doe"),
                    createTrainee("jane.smith", "Jane", "Smith")
            ));

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getTrainees()).isNotNull();
            assertThat(response.getTrainees()).hasSize(2);
            assertThat(response.getTrainees().get(0).getUsername()).isEqualTo("john.doe");
            assertThat(response.getTrainees().get(1).getUsername()).isEqualTo("jane.smith");
        }

        @Test
        @DisplayName("Should map single trainee correctly")
        void toProfileResponse_SingleTrainee_MapsCorrectly() {
            Trainer trainer = createTrainerWithTrainees(List.of(
                    createTrainee("john.doe", "John", "Doe")
            ));

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getTrainees()).hasSize(1);
            assertThat(response.getTrainees().getFirst().getUsername()).isEqualTo("john.doe");
            assertThat(response.getTrainees().getFirst().getFirstName()).isEqualTo("John");
            assertThat(response.getTrainees().getFirst().getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should return null when trainer is null")
        void toProfileResponse_NullTrainer_ReturnsNull() {
            TrainerProfileResponse response = trainerMapper.toProfileResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map all training type specializations correctly")
        void toProfileResponse_AllSpecializations_MapsCorrectly() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                Trainer trainer = createTrainerWithSpecialization(typeName);

                TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

                assertThat(response.getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should map trainer with many trainees")
        void toProfileResponse_ManyTrainees_MapsCorrectly() {
            List<Trainee> trainees = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                trainees.add(createTrainee("trainee" + i, "First" + i, "Last" + i));
            }
            Trainer trainer = createTrainerWithTrainees(trainees);

            TrainerProfileResponse response = trainerMapper.toProfileResponse(trainer);

            assertThat(response.getTrainees()).hasSize(50);
        }
    }

    @Nested
    @DisplayName("toSummaryResponse Tests")
    class ToSummaryResponseTests {

        @Test
        @DisplayName("Should map Trainer to TrainerSummaryResponse correctly")
        void toSummaryResponse_ValidTrainer_MapsCorrectly() {
            Trainer trainer = createTrainer();

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(LAST_NAME);
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should map username correctly")
        void toSummaryResponse_ValidTrainer_MapsUsername() {
            Trainer trainer = createTrainerWithUsername("charlie.brown");

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response.getUsername()).isEqualTo("charlie.brown");
        }

        @Test
        @DisplayName("Should map first name correctly")
        void toSummaryResponse_ValidTrainer_MapsFirstName() {
            Trainer trainer = createTrainerWithName("Charlie", LAST_NAME);

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response.getFirstName()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Should map last name correctly")
        void toSummaryResponse_ValidTrainer_MapsLastName() {
            Trainer trainer = createTrainerWithName(FIRST_NAME, "Brown");

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response.getLastName()).isEqualTo("Brown");
        }

        @Test
        @DisplayName("Should map specialization correctly")
        void toSummaryResponse_ValidTrainer_MapsSpecialization() {
            Trainer trainer = createTrainerWithSpecialization(TrainingTypeName.CARDIO);

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should return null when trainer is null")
        void toSummaryResponse_NullTrainer_ReturnsNull() {
            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map all specialization types")
        void toSummaryResponse_AllSpecializations_MapsCorrectly() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                Trainer trainer = createTrainerWithSpecialization(typeName);

                TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

                assertThat(response.getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should map trainer with special characters in name")
        void toSummaryResponse_SpecialCharactersInName_MapsCorrectly() {
            Trainer trainer = createTrainerWithName("Mary-Jane", "O'Connor");

            TrainerSummaryResponse response = trainerMapper.toSummaryResponse(trainer);

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("O'Connor");
        }
    }

    @Nested
    @DisplayName("toSummaryResponseList Tests")
    class ToSummaryResponseListTests {

        @Test
        @DisplayName("Should map list of trainers correctly")
        void toSummaryResponseList_ValidList_MapsCorrectly() {
            List<Trainer> trainers = List.of(
                    createTrainerWithUsername("trainer1"),
                    createTrainerWithUsername("trainer2"),
                    createTrainerWithUsername("trainer3")
            );

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getUsername()).isEqualTo("trainer1");
            assertThat(responses.get(1).getUsername()).isEqualTo("trainer2");
            assertThat(responses.get(2).getUsername()).isEqualTo("trainer3");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void toSummaryResponseList_EmptyList_ReturnsEmptyList() {
            List<Trainer> trainers = Collections.emptyList();

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return null when input list is null")
        void toSummaryResponseList_NullList_ReturnsNull() {
            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(null);

            assertThat(responses).isNull();
        }

        @Test
        @DisplayName("Should map single item list correctly")
        void toSummaryResponseList_SingleItem_MapsCorrectly() {
            List<Trainer> trainers = List.of(createTrainer());

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().getUsername()).isEqualTo(USERNAME);
        }

        @Test
        @DisplayName("Should preserve order when mapping list")
        void toSummaryResponseList_ValidList_PreservesOrder() {
            List<Trainer> trainers = List.of(
                    createTrainerWithUsername("first"),
                    createTrainerWithUsername("second"),
                    createTrainerWithUsername("third")
            );

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses.get(0).getUsername()).isEqualTo("first");
            assertThat(responses.get(1).getUsername()).isEqualTo("second");
            assertThat(responses.get(2).getUsername()).isEqualTo("third");
        }

        @Test
        @DisplayName("Should map list with different specializations")
        void toSummaryResponseList_DifferentSpecializations_MapsCorrectly() {
            List<Trainer> trainers = List.of(
                    createTrainerWithSpecialization(TrainingTypeName.FITNESS),
                    createTrainerWithSpecialization(TrainingTypeName.YOGA),
                    createTrainerWithSpecialization(TrainingTypeName.PILATES)
            );

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(responses.get(1).getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(responses.get(2).getSpecialization()).isEqualTo(TrainingTypeName.PILATES);
        }

        @Test
        @DisplayName("Should map large list correctly")
        void toSummaryResponseList_LargeList_MapsCorrectly() {
            List<Trainer> trainers = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                trainers.add(createTrainerWithUsername("trainer" + i));
            }

            List<TrainerSummaryResponse> responses = trainerMapper.toSummaryResponseList(trainers);

            assertThat(responses).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(responses.get(i).getUsername()).isEqualTo("trainer" + i);
            }
        }
    }

    @Nested
    @DisplayName("traineeToSummary Tests")
    class TraineeToSummaryTests {

        @Test
        @DisplayName("Should map Trainee to TraineeSummaryResponse correctly")
        void traineeToSummary_ValidTrainee_MapsCorrectly() {
            Trainee trainee = createTrainee(TRAINEE_USERNAME, TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME);

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(TRAINEE_USERNAME);
            assertThat(response.getFirstName()).isEqualTo(TRAINEE_FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(TRAINEE_LAST_NAME);
        }

        @Test
        @DisplayName("Should map username correctly")
        void traineeToSummary_ValidTrainee_MapsUsername() {
            Trainee trainee = createTrainee("bob.wilson", "Bob", "Wilson");

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response.getUsername()).isEqualTo("bob.wilson");
        }

        @Test
        @DisplayName("Should map first name correctly")
        void traineeToSummary_ValidTrainee_MapsFirstName() {
            Trainee trainee = createTrainee(TRAINEE_USERNAME, "Robert", TRAINEE_LAST_NAME);

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response.getFirstName()).isEqualTo("Robert");
        }

        @Test
        @DisplayName("Should map last name correctly")
        void traineeToSummary_ValidTrainee_MapsLastName() {
            Trainee trainee = createTrainee(TRAINEE_USERNAME, TRAINEE_FIRST_NAME, "Williams");

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response.getLastName()).isEqualTo("Williams");
        }

        @Test
        @DisplayName("Should return null when trainee is null")
        void traineeToSummary_NullTrainee_ReturnsNull() {
            TraineeSummaryResponse response = trainerMapper.traineeToSummary(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map trainee with special characters in name")
        void traineeToSummary_SpecialCharactersInName_MapsCorrectly() {
            Trainee trainee = createTrainee("mary.oconnor", "Mary-Jane", "O'Connor");

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("Should map trainee with accented characters")
        void traineeToSummary_AccentedCharacters_MapsCorrectly() {
            Trainee trainee = createTrainee("jose.garcia", "José", "García");

            TraineeSummaryResponse response = trainerMapper.traineeToSummary(trainee);

            assertThat(response.getFirstName()).isEqualTo("José");
            assertThat(response.getLastName()).isEqualTo("García");
        }
    }

    @Nested
    @DisplayName("traineesToSummaryList Tests")
    class TraineesToSummaryListTests {

        @Test
        @DisplayName("Should map list of trainees correctly")
        void traineesToSummaryList_ValidList_MapsCorrectly() {
            List<Trainee> trainees = List.of(
                    createTrainee("trainee1", "First1", "Last1"),
                    createTrainee("trainee2", "First2", "Last2"),
                    createTrainee("trainee3", "First3", "Last3")
            );

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getUsername()).isEqualTo("trainee1");
            assertThat(responses.get(1).getUsername()).isEqualTo("trainee2");
            assertThat(responses.get(2).getUsername()).isEqualTo("trainee3");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void traineesToSummaryList_EmptyList_ReturnsEmptyList() {
            List<Trainee> trainees = Collections.emptyList();

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return null when input list is null")
        void traineesToSummaryList_NullList_ReturnsNull() {
            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(null);

            assertThat(responses).isNull();
        }

        @Test
        @DisplayName("Should map single item list correctly")
        void traineesToSummaryList_SingleItem_MapsCorrectly() {
            List<Trainee> trainees = List.of(
                    createTrainee(TRAINEE_USERNAME, TRAINEE_FIRST_NAME, TRAINEE_LAST_NAME)
            );

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().getUsername()).isEqualTo(TRAINEE_USERNAME);
            assertThat(responses.getFirst().getFirstName()).isEqualTo(TRAINEE_FIRST_NAME);
            assertThat(responses.getFirst().getLastName()).isEqualTo(TRAINEE_LAST_NAME);
        }

        @Test
        @DisplayName("Should preserve order when mapping list")
        void traineesToSummaryList_ValidList_PreservesOrder() {
            List<Trainee> trainees = List.of(
                    createTrainee("first", "First", "User"),
                    createTrainee("second", "Second", "User"),
                    createTrainee("third", "Third", "User")
            );

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses.get(0).getUsername()).isEqualTo("first");
            assertThat(responses.get(1).getUsername()).isEqualTo("second");
            assertThat(responses.get(2).getUsername()).isEqualTo("third");
        }

        @Test
        @DisplayName("Should map large list correctly")
        void traineesToSummaryList_LargeList_MapsCorrectly() {
            List<Trainee> trainees = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                trainees.add(createTrainee("trainee" + i, "First" + i, "Last" + i));
            }

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(responses.get(i).getUsername()).isEqualTo("trainee" + i);
                assertThat(responses.get(i).getFirstName()).isEqualTo("First" + i);
                assertThat(responses.get(i).getLastName()).isEqualTo("Last" + i);
            }
        }

        @Test
        @DisplayName("Should map all trainee fields correctly in list")
        void traineesToSummaryList_ValidList_MapsAllFields() {
            List<Trainee> trainees = List.of(
                    createTrainee("john.doe", "John", "Doe"),
                    createTrainee("jane.smith", "Jane", "Smith")
            );

            List<TraineeSummaryResponse> responses = trainerMapper.traineesToSummaryList(trainees);

            assertThat(responses.getFirst().getUsername()).isEqualTo("john.doe");
            assertThat(responses.get(0).getFirstName()).isEqualTo("John");
            assertThat(responses.get(0).getLastName()).isEqualTo("Doe");

            assertThat(responses.get(1).getUsername()).isEqualTo("jane.smith");
            assertThat(responses.get(1).getFirstName()).isEqualTo("Jane");
            assertThat(responses.get(1).getLastName()).isEqualTo("Smith");
        }
    }

    // ==================== Helper Methods ====================

    private Trainer createTrainer() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithUsername(String username) {
        User user = User.builder()
                .id(USER_ID)
                .username(username)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithName(String firstName, String lastName) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(IS_ACTIVE)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithSpecialization(TrainingTypeName specialization) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, specialization))
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithActiveStatus(boolean isActive) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(isActive)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainees(new ArrayList<>())
                .build();
    }

    private Trainer createTrainerWithTrainees(List<Trainee> trainees) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainer.builder()
                .id(TRAINER_ID)
                .user(user)
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))
                .trainees(new ArrayList<>(trainees))
                .build();
    }

    private Trainee createTrainee(String username, String firstName, String lastName) {
        User user = User.builder()
                .id(2L)
                .username(username)
                .password(PASSWORD)
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