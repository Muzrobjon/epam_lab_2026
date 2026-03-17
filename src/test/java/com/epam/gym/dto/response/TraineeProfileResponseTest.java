package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TraineeProfileResponse Tests")
class TraineeProfileResponseTest {

    private TraineeProfileResponse createValidResponse() {
        return TraineeProfileResponse.builder()
                .username("John.Doe")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .address("123 Main St, New York")
                .isActive(true)
                .trainers(createSampleTrainers())
                .build();
    }

    private List<TrainerSummaryResponse> createSampleTrainers() {
        return Arrays.asList(
                TrainerSummaryResponse.builder()
                        .username("Alice.Smith")
                        .firstName("Alice")
                        .lastName("Smith")
                        .specialization(TrainingTypeName.FITNESS)
                        .build(),
                TrainerSummaryResponse.builder()
                        .username("Bob.Johnson")
                        .firstName("Bob")
                        .lastName("Johnson")
                        .specialization(TrainingTypeName.YOGA)
                        .build()
        );
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            LocalDate dob = LocalDate.of(1995, 3, 20);
            List<TrainerSummaryResponse> trainers = createSampleTrainers();

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(dob)
                    .address("123 Main St, New York")
                    .isActive(true)
                    .trainers(trainers)
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getDateOfBirth()).isEqualTo(dob);
            assertThat(response.getAddress()).isEqualTo("123 Main St, New York");
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainers()).hasSize(2);
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username(null)
                    .firstName(null)
                    .lastName(null)
                    .dateOfBirth(null)
                    .address(null)
                    .isActive(null)
                    .trainers(null)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainers()).isNull();
        }

        @Test
        @DisplayName("Should create response with partial fields using builder")
        void shouldCreateResponseWithPartialFieldsUsingBuilder() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainers()).isNull();
        }

        @Test
        @DisplayName("Should create empty response using builder")
        void shouldCreateEmptyResponseUsingBuilder() {
            TraineeProfileResponse response = TraineeProfileResponse.builder().build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainers()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainers()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            LocalDate dob = LocalDate.of(1990, 6, 15);
            List<TrainerSummaryResponse> trainers = createSampleTrainers();

            TraineeProfileResponse response = new TraineeProfileResponse(
                    "Alice.Smith",
                    "Alice",
                    "Smith",
                    dob,
                    "456 Oak Ave, Boston",
                    false,
                    trainers
            );

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getDateOfBirth()).isEqualTo(dob);
            assertThat(response.getAddress()).isEqualTo("456 Oak Ave, Boston");
            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getTrainers()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setUsername("Test.User");

            assertThat(response.getUsername()).isEqualTo("Test.User");
        }

        @Test
        @DisplayName("Should set and get firstName")
        void shouldSetAndGetFirstName() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setFirstName("Test");

            assertThat(response.getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should set and get lastName")
        void shouldSetAndGetLastName() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setLastName("User");

            assertThat(response.getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should set and get dateOfBirth")
        void shouldSetAndGetDateOfBirth() {
            TraineeProfileResponse response = new TraineeProfileResponse();
            LocalDate dob = LocalDate.of(2000, 1, 1);

            response.setDateOfBirth(dob);

            assertThat(response.getDateOfBirth()).isEqualTo(dob);
        }

        @Test
        @DisplayName("Should set and get address")
        void shouldSetAndGetAddress() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setAddress("789 Pine St, Chicago");

            assertThat(response.getAddress()).isEqualTo("789 Pine St, Chicago");
        }

        @Test
        @DisplayName("Should set and get isActive")
        void shouldSetAndGetIsActive() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setIsActive(true);
            assertThat(response.getIsActive()).isTrue();

            response.setIsActive(false);
            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get trainers")
        void shouldSetAndGetTrainers() {
            TraineeProfileResponse response = new TraineeProfileResponse();
            List<TrainerSummaryResponse> trainers = createSampleTrainers();

            response.setTrainers(trainers);

            assertThat(response.getTrainers()).hasSize(2);
        }

        @Test
        @DisplayName("Should allow setting all fields to null")
        void shouldAllowSettingAllFieldsToNull() {
            TraineeProfileResponse response = createValidResponse();

            response.setUsername(null);
            response.setFirstName(null);
            response.setLastName(null);
            response.setDateOfBirth(null);
            response.setAddress(null);
            response.setIsActive(null);
            response.setTrainers(null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainers()).isNull();
        }
    }

    @Nested
    @DisplayName("Trainers List Tests")
    class TrainersListTests {

        @Test
        @DisplayName("Should handle empty trainers list")
        void shouldHandleEmptyTrainersList() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .trainers(Collections.emptyList())
                    .build();

            assertThat(response.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should handle single trainer")
        void shouldHandleSingleTrainer() {
            TrainerSummaryResponse trainer = TrainerSummaryResponse.builder()
                    .username("Trainer.One")
                    .firstName("Trainer")
                    .lastName("One")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .trainers(Collections.singletonList(trainer))
                    .build();

            assertThat(response.getTrainers()).hasSize(1);
            assertThat(response.getTrainers().get(0).getUsername()).isEqualTo("Trainer.One");
        }

        @Test
        @DisplayName("Should handle multiple trainers")
        void shouldHandleMultipleTrainers() {
            List<TrainerSummaryResponse> trainers = new ArrayList<>();
            TrainingTypeName[] types = TrainingTypeName.values();
            for (int i = 1; i <= 5; i++) {
                trainers.add(TrainerSummaryResponse.builder()
                        .username("Trainer" + i + ".User")
                        .firstName("Trainer" + i)
                        .lastName("User")
                        .specialization(types[i % types.length])
                        .build());
            }

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .trainers(trainers)
                    .build();

            assertThat(response.getTrainers()).hasSize(5);
        }

        @Test
        @DisplayName("Should handle mutable trainers list")
        void shouldHandleMutableTrainersList() {
            List<TrainerSummaryResponse> trainers = new ArrayList<>(createSampleTrainers());

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .trainers(trainers)
                    .build();

            trainers.add(TrainerSummaryResponse.builder()
                    .username("New.Trainer")
                    .specialization(TrainingTypeName.CARDIO)
                    .build());

            assertThat(response.getTrainers()).hasSize(3);
        }

        @Test
        @DisplayName("Should handle trainers list with null elements")
        void shouldHandleTrainersListWithNullElements() {
            List<TrainerSummaryResponse> trainers = new ArrayList<>();
            trainers.add(null);
            trainers.add(TrainerSummaryResponse.builder()
                    .username("Valid.Trainer")
                    .specialization(TrainingTypeName.STRENGTH)
                    .build());

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .trainers(trainers)
                    .build();

            assertThat(response.getTrainers()).hasSize(2);
            assertThat(response.getTrainers().get(0)).isNull();
            assertThat(response.getTrainers().get(1).getUsername()).isEqualTo("Valid.Trainer");
        }
    }

    @Nested
    @DisplayName("Date of Birth Tests")
    class DateOfBirthTests {

        @Test
        @DisplayName("Should handle past date of birth")
        void shouldHandlePastDateOfBirth() {
            LocalDate pastDate = LocalDate.of(1980, 5, 15);

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .dateOfBirth(pastDate)
                    .build();

            assertThat(response.getDateOfBirth()).isEqualTo(pastDate);
        }

        @Test
        @DisplayName("Should handle recent date of birth")
        void shouldHandleRecentDateOfBirth() {
            LocalDate recentDate = LocalDate.now().minusYears(18);

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .dateOfBirth(recentDate)
                    .build();

            assertThat(response.getDateOfBirth()).isEqualTo(recentDate);
        }

        @Test
        @DisplayName("Should handle leap year date of birth")
        void shouldHandleLeapYearDateOfBirth() {
            LocalDate leapYearDate = LocalDate.of(2000, 2, 29);

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .dateOfBirth(leapYearDate)
                    .build();

            assertThat(response.getDateOfBirth()).isEqualTo(leapYearDate);
            assertThat(response.getDateOfBirth().getMonth()).hasToString("FEBRUARY");
            assertThat(response.getDateOfBirth().getDayOfMonth()).isEqualTo(29);
        }

        @Test
        @DisplayName("Should handle very old date of birth")
        void shouldHandleVeryOldDateOfBirth() {
            LocalDate veryOldDate = LocalDate.of(1900, 1, 1);

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .dateOfBirth(veryOldDate)
                    .build();

            assertThat(response.getDateOfBirth()).isEqualTo(veryOldDate);
        }
    }

    @Nested
    @DisplayName("Address Tests")
    class AddressTests {

        @Test
        @DisplayName("Should handle simple address")
        void shouldHandleSimpleAddress() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address("123 Main St")
                    .build();

            assertThat(response.getAddress()).isEqualTo("123 Main St");
        }

        @Test
        @DisplayName("Should handle full address with city and state")
        void shouldHandleFullAddressWithCityAndState() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address("123 Main St, New York, NY 10001")
                    .build();

            assertThat(response.getAddress()).isEqualTo("123 Main St, New York, NY 10001");
        }

        @Test
        @DisplayName("Should handle multiline address")
        void shouldHandleMultilineAddress() {
            String multilineAddress = "123 Main St\nApt 4B\nNew York, NY 10001";

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address(multilineAddress)
                    .build();

            assertThat(response.getAddress()).isEqualTo(multilineAddress);
        }

        @Test
        @DisplayName("Should handle international address")
        void shouldHandleInternationalAddress() {
            String internationalAddress = "221B Baker Street, London, UK";

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address(internationalAddress)
                    .build();

            assertThat(response.getAddress()).isEqualTo(internationalAddress);
        }

        @Test
        @DisplayName("Should handle empty address")
        void shouldHandleEmptyAddress() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address("")
                    .build();

            assertThat(response.getAddress()).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsActive Tests")
    class IsActiveTests {

        @Test
        @DisplayName("Should handle isActive true")
        void shouldHandleIsActiveTrue() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .isActive(true)
                    .build();

            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should handle isActive false")
        void shouldHandleIsActiveFalse() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .isActive(false)
                    .build();

            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle isActive null")
        void shouldHandleIsActiveNull() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .isActive(null)
                    .build();

            assertThat(response.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should handle Boolean wrapper values")
        void shouldHandleBooleanWrapperValues() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            response.setIsActive(Boolean.TRUE);
            assertThat(response.getIsActive()).isEqualTo(Boolean.TRUE);

            response.setIsActive(Boolean.FALSE);
            assertThat(response.getIsActive()).isEqualTo(Boolean.FALSE);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TraineeProfileResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different username")
        void shouldNotBeEqualToResponseWithDifferentUsername() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setUsername("Different.User");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different firstName")
        void shouldNotBeEqualToResponseWithDifferentFirstName() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setFirstName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different lastName")
        void shouldNotBeEqualToResponseWithDifferentLastName() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setLastName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different dateOfBirth")
        void shouldNotBeEqualToResponseWithDifferentDateOfBirth() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setDateOfBirth(LocalDate.of(2000, 1, 1));

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different address")
        void shouldNotBeEqualToResponseWithDifferentAddress() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setAddress("Different Address");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different isActive")
        void shouldNotBeEqualToResponseWithDifferentIsActive() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setIsActive(false);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different trainers")
        void shouldNotBeEqualToResponseWithDifferentTrainers() {
            TraineeProfileResponse response1 = createValidResponse();
            TraineeProfileResponse response2 = createValidResponse();
            response2.setTrainers(Collections.emptyList());

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TraineeProfileResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TraineeProfileResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not a TraineeProfileResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            TraineeProfileResponse response1 = new TraineeProfileResponse();
            TraineeProfileResponse response2 = new TraineeProfileResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TraineeProfileResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("TraineeProfileResponse");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("firstName=John");
            assertThat(toString).contains("lastName=Doe");
            assertThat(toString).contains("address=123 Main St, New York");
            assertThat(toString).contains("isActive=true");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TraineeProfileResponse response = new TraineeProfileResponse();

            String toString = response.toString();

            assertThat(toString).contains("TraineeProfileResponse");
            assertThat(toString).contains("username=null");
            assertThat(toString).contains("firstName=null");
            assertThat(toString).contains("lastName=null");
            assertThat(toString).contains("dateOfBirth=null");
            assertThat(toString).contains("address=null");
            assertThat(toString).contains("isActive=null");
            assertThat(toString).contains("trainers=null");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("José.García")
                    .firstName("José")
                    .lastName("García")
                    .build();

            assertThat(response.getUsername()).isEqualTo("José.García");
            assertThat(response.getFirstName()).isEqualTo("José");
            assertThat(response.getLastName()).isEqualTo("García");
        }

        @Test
        @DisplayName("Should handle Cyrillic characters")
        void shouldHandleCyrillicCharacters() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("Иван.Петров")
                    .firstName("Иван")
                    .lastName("Петров")
                    .address("Москва, ул. Ленина, 1")
                    .build();

            assertThat(response.getFirstName()).isEqualTo("Иван");
            assertThat(response.getLastName()).isEqualTo("Петров");
        }

        @Test
        @DisplayName("Should handle Chinese characters")
        void shouldHandleChineseCharacters() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .firstName("明")
                    .lastName("王")
                    .address("北京市朝阳区")
                    .build();

            assertThat(response.getFirstName()).isEqualTo("明");
            assertThat(response.getLastName()).isEqualTo("王");
        }

        @Test
        @DisplayName("Should handle very long values")
        void shouldHandleVeryLongValues() {
            String longUsername = "A".repeat(50) + "." + "B".repeat(50);
            String longAddress = "Street ".repeat(100);

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username(longUsername)
                    .address(longAddress)
                    .build();

            assertThat(response.getUsername()).hasSize(101);
            assertThat(response.getAddress()).hasSize(700);
        }

        @Test
        @DisplayName("Should handle special characters in address")
        void shouldHandleSpecialCharactersInAddress() {
            String addressWithSpecialChars = "123 Main St, Apt #4B, Suite 100 & Co.";

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .address(addressWithSpecialChars)
                    .build();

            assertThat(response.getAddress()).isEqualTo(addressWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Field Count Tests")
    class FieldCountTests {

        @Test
        @DisplayName("Should have exactly 7 fields")
        void shouldHaveExactlySevenFields() {
            assertThat(TraineeProfileResponse.class.getDeclaredFields())
                    .hasSize(7)
                    .extracting("name")
                    .containsExactlyInAnyOrder(
                            "username",
                            "firstName",
                            "lastName",
                            "dateOfBirth",
                            "address",
                            "isActive",
                            "trainers"
                    );
        }
    }

    @Nested
    @DisplayName("Typical Usage Scenarios Tests")
    class TypicalUsageScenariosTests {

        @Test
        @DisplayName("Should represent active trainee with trainers")
        void shouldRepresentActiveTraineeWithTrainers() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(1995, 3, 20))
                    .address("123 Main St, New York")
                    .isActive(true)
                    .trainers(createSampleTrainers())
                    .build();

            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainers()).isNotEmpty();
        }

        @Test
        @DisplayName("Should represent inactive trainee without trainers")
        void shouldRepresentInactiveTraineeWithoutTrainers() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("Jane.Smith")
                    .firstName("Jane")
                    .lastName("Smith")
                    .isActive(false)
                    .trainers(Collections.emptyList())
                    .build();

            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getTrainers()).isEmpty();
        }

        @Test
        @DisplayName("Should represent trainee with minimal information")
        void shouldRepresentTraineeWithMinimalInformation() {
            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("Minimal.User")
                    .firstName("Minimal")
                    .lastName("User")
                    .isActive(true)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Minimal.User");
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getTrainers()).isNull();
        }

        @Test
        @DisplayName("Should represent trainee with full profile")
        void shouldRepresentTraineeWithFullProfile() {
            TraineeProfileResponse response = createValidResponse();

            assertThat(response.getUsername()).isNotNull();
            assertThat(response.getFirstName()).isNotNull();
            assertThat(response.getLastName()).isNotNull();
            assertThat(response.getDateOfBirth()).isNotNull();
            assertThat(response.getAddress()).isNotNull();
            assertThat(response.getIsActive()).isNotNull();
            assertThat(response.getTrainers()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Training Type Name Tests")
    class TrainingTypeNameTests {

        @Test
        @DisplayName("Should handle all training type names")
        void shouldHandleAllTrainingTypeNames() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                TrainerSummaryResponse trainer = TrainerSummaryResponse.builder()
                        .username("Trainer.Test")
                        .firstName("Trainer")
                        .lastName("Test")
                        .specialization(typeName)
                        .build();

                TraineeProfileResponse response = TraineeProfileResponse.builder()
                        .username("John.Doe")
                        .trainers(Collections.singletonList(trainer))
                        .build();

                assertThat(response.getTrainers()).hasSize(1);
                assertThat(response.getTrainers().get(0).getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should handle trainers with different specializations")
        void shouldHandleTrainersWithDifferentSpecializations() {
            List<TrainerSummaryResponse> trainers = Arrays.asList(
                    TrainerSummaryResponse.builder()
                            .username("Fitness.Trainer")
                            .specialization(TrainingTypeName.FITNESS)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("Yoga.Trainer")
                            .specialization(TrainingTypeName.YOGA)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("Cardio.Trainer")
                            .specialization(TrainingTypeName.CARDIO)
                            .build()
            );

            TraineeProfileResponse response = TraineeProfileResponse.builder()
                    .username("John.Doe")
                    .trainers(trainers)
                    .build();

            assertThat(response.getTrainers()).hasSize(3);
            assertThat(response.getTrainers())
                    .extracting(TrainerSummaryResponse::getSpecialization)
                    .containsExactly(
                            TrainingTypeName.FITNESS,
                            TrainingTypeName.YOGA,
                            TrainingTypeName.CARDIO
                    );
        }
    }
}