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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TraineeMapper Unit Tests")
class TraineeMapperTest {

    private TraineeMapper traineeMapper;

    private static final Long TRAINEE_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 15);
    private static final String ADDRESS = "123 Main St, New York";
    private static final Boolean IS_ACTIVE = true;

    private static final String TRAINER_USERNAME = "alice.smith";
    private static final String TRAINER_FIRST_NAME = "Alice";
    private static final String TRAINER_LAST_NAME = "Smith";

    @BeforeEach
    void setUp() {
        traineeMapper = Mappers.getMapper(TraineeMapper.class);
    }

    @Nested
    @DisplayName("toProfileResponse Tests")
    class ToProfileResponseTests {

        @Test
        @DisplayName("Should map Trainee to TraineeProfileResponse correctly")
        void toProfileResponse_ValidTrainee_MapsCorrectly() {
            Trainee trainee = createTrainee();

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(LAST_NAME);
            assertThat(response.getDateOfBirth()).isEqualTo(DATE_OF_BIRTH);
            assertThat(response.getAddress()).isEqualTo(ADDRESS);
            assertThat(response.getIsActive()).isEqualTo(IS_ACTIVE);
        }

        @Test
        @DisplayName("Should map username from nested user object")
        void toProfileResponse_ValidTrainee_MapsUsername() {
            Trainee trainee = createTraineeWithUsername("bob.wilson");

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getUsername()).isEqualTo("bob.wilson");
        }

        @Test
        @DisplayName("Should map first name from nested user object")
        void toProfileResponse_ValidTrainee_MapsFirstName() {
            Trainee trainee = createTraineeWithName("Robert", LAST_NAME);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getFirstName()).isEqualTo("Robert");
        }

        @Test
        @DisplayName("Should map last name from nested user object")
        void toProfileResponse_ValidTrainee_MapsLastName() {
            Trainee trainee = createTraineeWithName(FIRST_NAME, "Wilson");

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getLastName()).isEqualTo("Wilson");
        }

        @Test
        @DisplayName("Should map date of birth correctly")
        void toProfileResponse_ValidTrainee_MapsDateOfBirth() {
            LocalDate specificDate = LocalDate.of(1985, 12, 25);
            Trainee trainee = createTraineeWithDateOfBirth(specificDate);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getDateOfBirth()).isEqualTo(specificDate);
        }

        @Test
        @DisplayName("Should map null date of birth")
        void toProfileResponse_NullDateOfBirth_MapsNull() {
            Trainee trainee = createTraineeWithDateOfBirth(null);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getDateOfBirth()).isNull();
        }

        @Test
        @DisplayName("Should map address correctly")
        void toProfileResponse_ValidTrainee_MapsAddress() {
            Trainee trainee = createTraineeWithAddress("456 Oak Ave, Boston");

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getAddress()).isEqualTo("456 Oak Ave, Boston");
        }

        @Test
        @DisplayName("Should map null address")
        void toProfileResponse_NullAddress_MapsNull() {
            Trainee trainee = createTraineeWithAddress(null);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should map isActive status correctly when true")
        void toProfileResponse_ActiveTrainee_MapsIsActiveTrue() {
            Trainee trainee = createTraineeWithActiveStatus(true);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should map isActive status correctly when false")
        void toProfileResponse_InactiveTrainee_MapsIsActiveFalse() {
            Trainee trainee = createTraineeWithActiveStatus(false);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should map empty trainers list")
        void toProfileResponse_NoTrainers_MapsEmptyList() {
            Trainee trainee = createTrainee();

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getTrainers()).isNotNull();
            assertThat(response.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should map trainers list correctly")
        void toProfileResponse_WithTrainers_MapsTrainersList() {
            Trainee trainee = createTraineeWithTrainers(List.of(
                    createTrainer("alice.smith", "Alice", "Smith", TrainingTypeName.FITNESS),
                    createTrainer("bob.jones", "Bob", "Jones", TrainingTypeName.YOGA)
            ));

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getTrainers()).isNotNull();
            assertThat(response.getTrainers()).hasSize(2);
            assertThat(response.getTrainers().get(0).getUsername()).isEqualTo("alice.smith");
            assertThat(response.getTrainers().get(1).getUsername()).isEqualTo("bob.jones");
        }

        @Test
        @DisplayName("Should map single trainer correctly")
        void toProfileResponse_SingleTrainer_MapsCorrectly() {
            Trainee trainee = createTraineeWithTrainers(List.of(
                    createTrainer("alice.smith", "Alice", "Smith", TrainingTypeName.FITNESS)
            ));

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getTrainers()).hasSize(1);
            assertThat(response.getTrainers().getFirst().getUsername()).isEqualTo("alice.smith");
            assertThat(response.getTrainers().getFirst().getFirstName()).isEqualTo("Alice");
            assertThat(response.getTrainers().getFirst().getLastName()).isEqualTo("Smith");
            assertThat(response.getTrainers().getFirst().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should return null when trainee is null")
        void toProfileResponse_NullTrainee_ReturnsNull() {
            TraineeProfileResponse response = traineeMapper.toProfileResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map trainee with many trainers")
        void toProfileResponse_ManyTrainers_MapsCorrectly() {
            List<Trainer> trainers = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                trainers.add(createTrainer("trainer" + i, "First" + i, "Last" + i, TrainingTypeName.FITNESS));
            }
            Trainee trainee = createTraineeWithTrainers(trainers);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getTrainers()).hasSize(20);
        }

        @Test
        @DisplayName("Should map trainee with special characters in address")
        void toProfileResponse_SpecialCharactersInAddress_MapsCorrectly() {
            Trainee trainee = createTraineeWithAddress("123 Main St, Apt #4B, New York, NY 10001");

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getAddress()).isEqualTo("123 Main St, Apt #4B, New York, NY 10001");
        }

        @Test
        @DisplayName("Should map trainee with past date of birth")
        void toProfileResponse_PastDateOfBirth_MapsCorrectly() {
            LocalDate pastDate = LocalDate.of(1950, 1, 1);
            Trainee trainee = createTraineeWithDateOfBirth(pastDate);

            TraineeProfileResponse response = traineeMapper.toProfileResponse(trainee);

            assertThat(response.getDateOfBirth()).isEqualTo(pastDate);
        }
    }

    @Nested
    @DisplayName("toSummaryResponse Tests")
    class ToSummaryResponseTests {

        @Test
        @DisplayName("Should map Trainee to TraineeSummaryResponse correctly")
        void toSummaryResponse_ValidTrainee_MapsCorrectly() {
            Trainee trainee = createTrainee();

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(USERNAME);
            assertThat(response.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(LAST_NAME);
        }

        @Test
        @DisplayName("Should map username correctly")
        void toSummaryResponse_ValidTrainee_MapsUsername() {
            Trainee trainee = createTraineeWithUsername("charlie.brown");

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getUsername()).isEqualTo("charlie.brown");
        }

        @Test
        @DisplayName("Should map first name correctly")
        void toSummaryResponse_ValidTrainee_MapsFirstName() {
            Trainee trainee = createTraineeWithName("Charlie", LAST_NAME);

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getFirstName()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("Should map last name correctly")
        void toSummaryResponse_ValidTrainee_MapsLastName() {
            Trainee trainee = createTraineeWithName(FIRST_NAME, "Brown");

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getLastName()).isEqualTo("Brown");
        }

        @Test
        @DisplayName("Should return null when trainee is null")
        void toSummaryResponse_NullTrainee_ReturnsNull() {
            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map trainee with special characters in name")
        void toSummaryResponse_SpecialCharactersInName_MapsCorrectly() {
            Trainee trainee = createTraineeWithName("Mary-Jane", "O'Connor");

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("Should map trainee with accented characters")
        void toSummaryResponse_AccentedCharacters_MapsCorrectly() {
            Trainee trainee = createTraineeWithName("José", "García");

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getFirstName()).isEqualTo("José");
            assertThat(response.getLastName()).isEqualTo("García");
        }

        @Test
        @DisplayName("Should map trainee with long names")
        void toSummaryResponse_LongNames_MapsCorrectly() {
            Trainee trainee = createTraineeWithName("Christopher", "Bartholomew");

            TraineeSummaryResponse response = traineeMapper.toSummaryResponse(trainee);

            assertThat(response.getFirstName()).isEqualTo("Christopher");
            assertThat(response.getLastName()).isEqualTo("Bartholomew");
        }
    }

    @Nested
    @DisplayName("trainerToSummary Tests")
    class TrainerToSummaryTests {

        @Test
        @DisplayName("Should map Trainer to TrainerSummaryResponse correctly")
        void trainerToSummary_ValidTrainer_MapsCorrectly() {
            Trainer trainer = createTrainer(TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TrainingTypeName.FITNESS);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo(TRAINER_USERNAME);
            assertThat(response.getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
            assertThat(response.getLastName()).isEqualTo(TRAINER_LAST_NAME);
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should map username correctly")
        void trainerToSummary_ValidTrainer_MapsUsername() {
            Trainer trainer = createTrainer("bob.johnson", "Bob", "Johnson", TrainingTypeName.YOGA);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response.getUsername()).isEqualTo("bob.johnson");
        }

        @Test
        @DisplayName("Should map first name correctly")
        void trainerToSummary_ValidTrainer_MapsFirstName() {
            Trainer trainer = createTrainer(TRAINER_USERNAME, "Robert", TRAINER_LAST_NAME, TrainingTypeName.FITNESS);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response.getFirstName()).isEqualTo("Robert");
        }

        @Test
        @DisplayName("Should map last name correctly")
        void trainerToSummary_ValidTrainer_MapsLastName() {
            Trainer trainer = createTrainer(TRAINER_USERNAME, TRAINER_FIRST_NAME, "Johnson", TrainingTypeName.FITNESS);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response.getLastName()).isEqualTo("Johnson");
        }

        @Test
        @DisplayName("Should map specialization correctly")
        void trainerToSummary_ValidTrainer_MapsSpecialization() {
            Trainer trainer = createTrainer(TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TrainingTypeName.CARDIO);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should return null when trainer is null")
        void trainerToSummary_NullTrainer_ReturnsNull() {
            TrainerSummaryResponse response = traineeMapper.trainerToSummary(null);

            assertThat(response).isNull();
        }

        @Test
        @DisplayName("Should map all specialization types correctly")
        void trainerToSummary_AllSpecializations_MapsCorrectly() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                Trainer trainer = createTrainer(TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, typeName);

                TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

                assertThat(response.getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should map trainer with special characters in name")
        void trainerToSummary_SpecialCharactersInName_MapsCorrectly() {
            Trainer trainer = createTrainer("mary.oconnor", "Mary-Jane", "O'Connor", TrainingTypeName.YOGA);

            TrainerSummaryResponse response = traineeMapper.trainerToSummary(trainer);

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("O'Connor");
        }
    }

    @Nested
    @DisplayName("trainersToSummaryList Tests")
    class TrainersToSummaryListTests {

        @Test
        @DisplayName("Should map list of trainers correctly")
        void trainersToSummaryList_ValidList_MapsCorrectly() {
            List<Trainer> trainers = List.of(
                    createTrainer("trainer1", "First1", "Last1", TrainingTypeName.FITNESS),
                    createTrainer("trainer2", "First2", "Last2", TrainingTypeName.YOGA),
                    createTrainer("trainer3", "First3", "Last3", TrainingTypeName.CARDIO)
            );

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getUsername()).isEqualTo("trainer1");
            assertThat(responses.get(1).getUsername()).isEqualTo("trainer2");
            assertThat(responses.get(2).getUsername()).isEqualTo("trainer3");
        }

        @Test
        @DisplayName("Should return empty list when input is empty")
        void trainersToSummaryList_EmptyList_ReturnsEmptyList() {
            List<Trainer> trainers = Collections.emptyList();

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return null when input list is null")
        void trainersToSummaryList_NullList_ReturnsNull() {
            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(null);

            assertThat(responses).isNull();
        }

        @Test
        @DisplayName("Should map single item list correctly")
        void trainersToSummaryList_SingleItem_MapsCorrectly() {
            List<Trainer> trainers = List.of(
                    createTrainer(TRAINER_USERNAME, TRAINER_FIRST_NAME, TRAINER_LAST_NAME, TrainingTypeName.FITNESS)
            );

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().getUsername()).isEqualTo(TRAINER_USERNAME);
            assertThat(responses.getFirst().getFirstName()).isEqualTo(TRAINER_FIRST_NAME);
            assertThat(responses.getFirst().getLastName()).isEqualTo(TRAINER_LAST_NAME);
            assertThat(responses.getFirst().getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should preserve order when mapping list")
        void trainersToSummaryList_ValidList_PreservesOrder() {
            List<Trainer> trainers = List.of(
                    createTrainer("first", "First", "User", TrainingTypeName.FITNESS),
                    createTrainer("second", "Second", "User", TrainingTypeName.YOGA),
                    createTrainer("third", "Third", "User", TrainingTypeName.CARDIO)
            );

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses.get(0).getUsername()).isEqualTo("first");
            assertThat(responses.get(1).getUsername()).isEqualTo("second");
            assertThat(responses.get(2).getUsername()).isEqualTo("third");
        }

        @Test
        @DisplayName("Should map list with different specializations")
        void trainersToSummaryList_DifferentSpecializations_MapsCorrectly() {
            List<Trainer> trainers = List.of(
                    createTrainer("trainer1", "First1", "Last1", TrainingTypeName.FITNESS),
                    createTrainer("trainer2", "First2", "Last2", TrainingTypeName.YOGA),
                    createTrainer("trainer3", "First3", "Last3", TrainingTypeName.PILATES)
            );

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(responses.get(1).getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(responses.get(2).getSpecialization()).isEqualTo(TrainingTypeName.PILATES);
        }

        @Test
        @DisplayName("Should map large list correctly")
        void trainersToSummaryList_LargeList_MapsCorrectly() {
            List<Trainer> trainers = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                trainers.add(createTrainer("trainer" + i, "First" + i, "Last" + i, TrainingTypeName.FITNESS));
            }

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).hasSize(100);
            for (int i = 0; i < 100; i++) {
                assertThat(responses.get(i).getUsername()).isEqualTo("trainer" + i);
                assertThat(responses.get(i).getFirstName()).isEqualTo("First" + i);
                assertThat(responses.get(i).getLastName()).isEqualTo("Last" + i);
            }
        }

        @Test
        @DisplayName("Should map all trainer fields correctly in list")
        void trainersToSummaryList_ValidList_MapsAllFields() {
            List<Trainer> trainers = List.of(
                    createTrainer("alice.smith", "Alice", "Smith", TrainingTypeName.FITNESS),
                    createTrainer("bob.jones", "Bob", "Jones", TrainingTypeName.YOGA)
            );

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses.getFirst().getUsername()).isEqualTo("alice.smith");
            assertThat(responses.getFirst().getFirstName()).isEqualTo("Alice");
            assertThat(responses.get(0).getLastName()).isEqualTo("Smith");
            assertThat(responses.get(0).getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);

            assertThat(responses.get(1).getUsername()).isEqualTo("bob.jones");
            assertThat(responses.get(1).getFirstName()).isEqualTo("Bob");
            assertThat(responses.get(1).getLastName()).isEqualTo("Jones");
            assertThat(responses.get(1).getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should map trainers with all specialization types")
        void trainersToSummaryList_AllSpecializations_MapsCorrectly() {
            List<Trainer> trainers = new ArrayList<>();
            int index = 0;
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                trainers.add(createTrainer("trainer" + index, "First" + index, "Last" + index, typeName));
                index++;
            }

            List<TrainerSummaryResponse> responses = traineeMapper.trainersToSummaryList(trainers);

            assertThat(responses).hasSize(TrainingTypeName.values().length);
            index = 0;
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                assertThat(responses.get(index).getSpecialization()).isEqualTo(typeName);
                index++;
            }
        }
    }

    // ==================== Helper Methods ====================

    private Trainee createTrainee() {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithUsername(String username) {
        User user = User.builder()
                .id(USER_ID)
                .username(username)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithName(String firstName, String lastName) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithDateOfBirth(LocalDate dateOfBirth) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(dateOfBirth)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithAddress(String address) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(address)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithActiveStatus(boolean isActive) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(isActive)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>())
                .build();
    }

    private Trainee createTraineeWithTrainers(List<Trainer> trainers) {
        User user = User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(IS_ACTIVE)
                .build();

        return Trainee.builder()
                .id(TRAINEE_ID)
                .user(user)
                .dateOfBirth(DATE_OF_BIRTH)
                .address(ADDRESS)
                .trainers(new ArrayList<>(trainers))
                .build();
    }

    private Trainer createTrainer(String username, String firstName, String lastName, TrainingTypeName specialization) {
        User user = User.builder()
                .id(2L)
                .username(username)
                .password(PASSWORD)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(1L)
                .user(user)
                .specialization(new TrainingType(1L, specialization))
                .build();
    }
}