package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerProfileResponse Tests")
class TrainerProfileResponseTest {

    private TrainerProfileResponse createValidResponse() {
        return TrainerProfileResponse.builder()
                .username("Alice.Smith")
                .firstName("Alice")
                .lastName("Smith")
                .specialization(TrainingTypeName.YOGA)
                .isActive(true)
                .trainees(createSampleTrainees())
                .build();
    }

    private List<TraineeSummaryResponse> createSampleTrainees() {
        return Arrays.asList(
                TraineeSummaryResponse.builder()
                        .username("John.Doe")
                        .firstName("John")
                        .lastName("Doe")
                        .build(),
                TraineeSummaryResponse.builder()
                        .username("Jane.Smith")
                        .firstName("Jane")
                        .lastName("Smith")
                        .build()
        );
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            List<TraineeSummaryResponse> trainees = createSampleTrainees();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA)
                    .isActive(true)
                    .trainees(trainees)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainees()).hasSize(2);
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username(null)
                    .firstName(null)
                    .lastName(null)
                    .specialization(null)
                    .isActive(null)
                    .trainees(null)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should create response with partial fields using builder")
        void shouldCreateResponseWithPartialFieldsUsingBuilder() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .isActive(true)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should create empty response using builder")
        void shouldCreateEmptyResponseUsingBuilder() {
            TrainerProfileResponse response = TrainerProfileResponse.builder().build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should create response with only specialization")
        void shouldCreateResponseWithOnlySpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getUsername()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            List<TraineeSummaryResponse> trainees = createSampleTrainees();

            TrainerProfileResponse response = new TrainerProfileResponse(
                    "Bob.Johnson",
                    "Bob",
                    "Johnson",
                    TrainingTypeName.FITNESS,
                    false,
                    trainees
            );

            assertThat(response.getUsername()).isEqualTo("Bob.Johnson");
            assertThat(response.getFirstName()).isEqualTo("Bob");
            assertThat(response.getLastName()).isEqualTo("Johnson");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getTrainees()).hasSize(2);
        }

        @Test
        @DisplayName("Should create response using all-args constructor with nulls")
        void shouldCreateResponseUsingAllArgsConstructorWithNulls() {
            TrainerProfileResponse response = new TrainerProfileResponse(
                    null, null, null, null, null, null
            );

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainees()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setUsername("Test.User");

            assertThat(response.getUsername()).isEqualTo("Test.User");
        }

        @Test
        @DisplayName("Should set and get firstName")
        void shouldSetAndGetFirstName() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setFirstName("Test");

            assertThat(response.getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should set and get lastName")
        void shouldSetAndGetLastName() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setLastName("User");

            assertThat(response.getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should set and get specialization")
        void shouldSetAndGetSpecialization() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setSpecialization(TrainingTypeName.CARDIO);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should set and get isActive")
        void shouldSetAndGetIsActive() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setIsActive(true);
            assertThat(response.getIsActive()).isTrue();

            response.setIsActive(false);
            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get trainees")
        void shouldSetAndGetTrainees() {
            TrainerProfileResponse response = new TrainerProfileResponse();
            List<TraineeSummaryResponse> trainees = createSampleTrainees();

            response.setTrainees(trainees);

            assertThat(response.getTrainees()).hasSize(2);
        }

        @Test
        @DisplayName("Should allow setting all fields to null")
        void shouldAllowSettingAllFieldsToNull() {
            TrainerProfileResponse response = createValidResponse();

            response.setUsername(null);
            response.setFirstName(null);
            response.setLastName(null);
            response.setSpecialization(null);
            response.setIsActive(null);
            response.setTrainees(null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getIsActive()).isNull();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should allow overwriting existing values")
        void shouldAllowOverwritingExistingValues() {
            TrainerProfileResponse response = createValidResponse();

            response.setUsername("New.Username");
            response.setFirstName("NewFirst");
            response.setLastName("NewLast");
            response.setSpecialization(TrainingTypeName.STRENGTH);
            response.setIsActive(false);
            response.setTrainees(Collections.emptyList());

            assertThat(response.getUsername()).isEqualTo("New.Username");
            assertThat(response.getFirstName()).isEqualTo("NewFirst");
            assertThat(response.getLastName()).isEqualTo("NewLast");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getTrainees()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Trainees List Tests")
    class TraineesListTests {

        @Test
        @DisplayName("Should handle empty trainees list")
        void shouldHandleEmptyTraineesList() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .trainees(Collections.emptyList())
                    .build();

            assertThat(response.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should handle single trainee")
        void shouldHandleSingleTrainee() {
            TraineeSummaryResponse trainee = TraineeSummaryResponse.builder()
                    .username("Trainee.One")
                    .firstName("Trainee")
                    .lastName("One")
                    .build();

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .trainees(Collections.singletonList(trainee))
                    .build();

            assertThat(response.getTrainees()).hasSize(1);
            assertThat(response.getTrainees().get(0).getUsername()).isEqualTo("Trainee.One");
        }

        @Test
        @DisplayName("Should handle multiple trainees")
        void shouldHandleMultipleTrainees() {
            List<TraineeSummaryResponse> trainees = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                trainees.add(TraineeSummaryResponse.builder()
                        .username("Trainee" + i + ".User")
                        .firstName("Trainee" + i)
                        .lastName("User")
                        .build());
            }

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .trainees(trainees)
                    .build();

            assertThat(response.getTrainees()).hasSize(10);
        }

        @Test
        @DisplayName("Should handle mutable trainees list")
        void shouldHandleMutableTraineesList() {
            List<TraineeSummaryResponse> trainees = new ArrayList<>(createSampleTrainees());

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .trainees(trainees)
                    .build();

            trainees.add(TraineeSummaryResponse.builder()
                    .username("New.Trainee")
                    .build());

            assertThat(response.getTrainees()).hasSize(3);
        }

        @Test
        @DisplayName("Should handle trainees list with null elements")
        void shouldHandleTraineesListWithNullElements() {
            List<TraineeSummaryResponse> trainees = new ArrayList<>();
            trainees.add(null);
            trainees.add(TraineeSummaryResponse.builder().username("Valid.Trainee").build());

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .trainees(trainees)
                    .build();

            assertThat(response.getTrainees()).hasSize(2);
            assertThat(response.getTrainees().get(0)).isNull();
            assertThat(response.getTrainees().get(1).getUsername()).isEqualTo("Valid.Trainee");
        }

        @Test
        @DisplayName("Should handle large number of trainees")
        void shouldHandleLargeNumberOfTrainees() {
            List<TraineeSummaryResponse> trainees = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                trainees.add(TraineeSummaryResponse.builder()
                        .username("Trainee" + i + ".User")
                        .firstName("Trainee" + i)
                        .lastName("User")
                        .build());
            }

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .trainees(trainees)
                    .build();

            assertThat(response.getTrainees()).hasSize(100);
        }
    }

    @Nested
    @DisplayName("Specialization Tests")
    class SpecializationTests {

        @Test
        @DisplayName("Should handle all training type names")
        void shouldHandleAllTrainingTypeNames() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                TrainerProfileResponse response = TrainerProfileResponse.builder()
                        .username("Trainer.Test")
                        .specialization(typeName)
                        .build();

                assertThat(response.getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should handle FITNESS specialization")
        void shouldHandleFitnessSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getSpecialization().name()).isEqualTo("FITNESS");
        }

        @Test
        @DisplayName("Should handle YOGA specialization")
        void shouldHandleYogaSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(response.getSpecialization().name()).isEqualTo("YOGA");
        }

        @Test
        @DisplayName("Should handle CARDIO specialization")
        void shouldHandleCardioSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should handle STRENGTH specialization")
        void shouldHandleStrengthSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.STRENGTH)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
        }

        @Test
        @DisplayName("Should handle null specialization")
        void shouldHandleNullSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(null)
                    .build();

            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should allow changing specialization")
        void shouldAllowChangingSpecialization() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            response.setSpecialization(TrainingTypeName.FITNESS);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }
    }

    @Nested
    @DisplayName("IsActive Tests")
    class IsActiveTests {

        @Test
        @DisplayName("Should handle isActive true")
        void shouldHandleIsActiveTrue() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .isActive(true)
                    .build();

            assertThat(response.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should handle isActive false")
        void shouldHandleIsActiveFalse() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .isActive(false)
                    .build();

            assertThat(response.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should handle isActive null")
        void shouldHandleIsActiveNull() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .isActive(null)
                    .build();

            assertThat(response.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should handle Boolean wrapper values")
        void shouldHandleBooleanWrapperValues() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            response.setIsActive(Boolean.TRUE);
            assertThat(response.getIsActive()).isEqualTo(Boolean.TRUE);

            response.setIsActive(Boolean.FALSE);
            assertThat(response.getIsActive()).isEqualTo(Boolean.FALSE);
        }

        @Test
        @DisplayName("Should toggle isActive status")
        void shouldToggleIsActiveStatus() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .isActive(true)
                    .build();

            assertThat(response.getIsActive()).isTrue();

            response.setIsActive(false);
            assertThat(response.getIsActive()).isFalse();

            response.setIsActive(true);
            assertThat(response.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TrainerProfileResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different username")
        void shouldNotBeEqualToResponseWithDifferentUsername() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setUsername("Different.User");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different firstName")
        void shouldNotBeEqualToResponseWithDifferentFirstName() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setFirstName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different lastName")
        void shouldNotBeEqualToResponseWithDifferentLastName() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setLastName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different specialization")
        void shouldNotBeEqualToResponseWithDifferentSpecialization() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setSpecialization(TrainingTypeName.FITNESS);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different isActive")
        void shouldNotBeEqualToResponseWithDifferentIsActive() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setIsActive(false);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different trainees")
        void shouldNotBeEqualToResponseWithDifferentTrainees() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            response2.setTrainees(Collections.emptyList());

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TrainerProfileResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TrainerProfileResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not a TrainerProfileResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            TrainerProfileResponse response1 = new TrainerProfileResponse();
            TrainerProfileResponse response2 = new TrainerProfileResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            TrainerProfileResponse response = createValidResponse();

            int hashCode1 = response.hashCode();
            int hashCode2 = response.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TrainerProfileResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("TrainerProfileResponse");
            assertThat(toString).contains("username=Alice.Smith");
            assertThat(toString).contains("firstName=Alice");
            assertThat(toString).contains("lastName=Smith");
            assertThat(toString).contains("specialization=YOGA");
            assertThat(toString).contains("isActive=true");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TrainerProfileResponse response = new TrainerProfileResponse();

            String toString = response.toString();

            assertThat(toString).contains("TrainerProfileResponse");
            assertThat(toString).contains("username=null");
            assertThat(toString).contains("firstName=null");
            assertThat(toString).contains("lastName=null");
            assertThat(toString).contains("specialization=null");
            assertThat(toString).contains("isActive=null");
            assertThat(toString).contains("trainees=null");
        }

        @Test
        @DisplayName("Should include trainees in toString")
        void shouldIncludeTraineesInToString() {
            TrainerProfileResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("trainees=");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
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
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Иван.Петров")
                    .firstName("Иван")
                    .lastName("Петров")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Иван.Петров");
            assertThat(response.getFirstName()).isEqualTo("Иван");
            assertThat(response.getLastName()).isEqualTo("Петров");
        }

        @Test
        @DisplayName("Should handle Chinese characters")
        void shouldHandleChineseCharacters() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("王.明")
                    .firstName("明")
                    .lastName("王")
                    .build();

            assertThat(response.getUsername()).isEqualTo("王.明");
            assertThat(response.getFirstName()).isEqualTo("明");
            assertThat(response.getLastName()).isEqualTo("王");
        }

        @Test
        @DisplayName("Should handle very long values")
        void shouldHandleVeryLongValues() {
            String longUsername = "A".repeat(50) + "." + "B".repeat(50);
            String longFirstName = "First".repeat(50);
            String longLastName = "Last".repeat(50);

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username(longUsername)
                    .firstName(longFirstName)
                    .lastName(longLastName)
                    .build();

            assertThat(response.getUsername()).hasSize(101);
            assertThat(response.getFirstName()).hasSize(250);
            assertThat(response.getLastName()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("")
                    .firstName("")
                    .lastName("")
                    .build();

            assertThat(response.getUsername()).isEmpty();
            assertThat(response.getFirstName()).isEmpty();
            assertThat(response.getLastName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle whitespace strings")
        void shouldHandleWhitespaceStrings() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("   ")
                    .firstName("   ")
                    .lastName("   ")
                    .build();

            assertThat(response.getUsername()).isEqualTo("   ");
            assertThat(response.getFirstName()).isEqualTo("   ");
            assertThat(response.getLastName()).isEqualTo("   ");
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("User@Name#123")
                    .firstName("First-Name")
                    .lastName("Last'Name")
                    .build();

            assertThat(response.getUsername()).isEqualTo("User@Name#123");
            assertThat(response.getFirstName()).isEqualTo("First-Name");
            assertThat(response.getLastName()).isEqualTo("Last'Name");
        }
    }

    @Nested
    @DisplayName("Field Count Tests")
    class FieldCountTests {

        @Test
        @DisplayName("Should have exactly 6 fields")
        void shouldHaveExactlySixFields() {
            assertThat(TrainerProfileResponse.class.getDeclaredFields())
                    .hasSize(6)
                    .extracting("name")
                    .containsExactlyInAnyOrder(
                            "username",
                            "firstName",
                            "lastName",
                            "specialization",
                            "isActive",
                            "trainees"
                    );
        }
    }

    @Nested
    @DisplayName("Typical Usage Scenarios Tests")
    class TypicalUsageScenariosTests {

        @Test
        @DisplayName("Should represent active trainer with trainees")
        void shouldRepresentActiveTrainerWithTrainees() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA)
                    .isActive(true)
                    .trainees(createSampleTrainees())
                    .build();

            assertThat(response.getIsActive()).isTrue();
            assertThat(response.getTrainees()).isNotEmpty();
            assertThat(response.getSpecialization()).isNotNull();
        }

        @Test
        @DisplayName("Should represent inactive trainer without trainees")
        void shouldRepresentInactiveTrainerWithoutTrainees() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Bob.Johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(false)
                    .trainees(Collections.emptyList())
                    .build();

            assertThat(response.getIsActive()).isFalse();
            assertThat(response.getTrainees()).isEmpty();
        }

        @Test
        @DisplayName("Should represent trainer with minimal information")
        void shouldRepresentTrainerWithMinimalInformation() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Minimal.User")
                    .firstName("Minimal")
                    .lastName("User")
                    .isActive(true)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Minimal.User");
            assertThat(response.getSpecialization()).isNull();
            assertThat(response.getTrainees()).isNull();
        }

        @Test
        @DisplayName("Should represent trainer with full profile")
        void shouldRepresentTrainerWithFullProfile() {
            TrainerProfileResponse response = createValidResponse();

            assertThat(response.getUsername()).isNotNull();
            assertThat(response.getFirstName()).isNotNull();
            assertThat(response.getLastName()).isNotNull();
            assertThat(response.getSpecialization()).isNotNull();
            assertThat(response.getIsActive()).isNotNull();
            assertThat(response.getTrainees()).isNotNull();
        }

        @Test
        @DisplayName("Should be usable in collections")
        void shouldBeUsableInCollections() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = TrainerProfileResponse.builder()
                    .username("Bob.Johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .build();

            List<TrainerProfileResponse> list = Arrays.asList(response1, response2);

            assertThat(list).hasSize(2);
            assertThat(list).extracting(TrainerProfileResponse::getUsername)
                    .containsExactly("Alice.Smith", "Bob.Johnson");
        }

        @Test
        @DisplayName("Should be usable in sets")
        void shouldBeUsableInSets() {
            TrainerProfileResponse response1 = createValidResponse();
            TrainerProfileResponse response2 = createValidResponse();
            TrainerProfileResponse response3 = TrainerProfileResponse.builder()
                    .username("Bob.Johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            Set<TrainerProfileResponse> set = new HashSet<>();
            set.add(response1);
            set.add(response2);
            set.add(response3);

            assertThat(set).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Trainer with Different Specializations Tests")
    class TrainerWithDifferentSpecializationsTests {

        @Test
        @DisplayName("Should create yoga trainer")
        void shouldCreateYogaTrainer() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Yoga.Trainer")
                    .firstName("Yoga")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.YOGA)
                    .isActive(true)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should create fitness trainer")
        void shouldCreateFitnessTrainer() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Fitness.Trainer")
                    .firstName("Fitness")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.FITNESS)
                    .isActive(true)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should create cardio trainer")
        void shouldCreateCardioTrainer() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Cardio.Trainer")
                    .firstName("Cardio")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.CARDIO)
                    .isActive(true)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should create strength trainer")
        void shouldCreateStrengthTrainer() {
            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .username("Strength.Trainer")
                    .firstName("Strength")
                    .lastName("Trainer")
                    .specialization(TrainingTypeName.STRENGTH)
                    .isActive(true)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            TrainerProfileResponse response = createValidResponse();
            String originalUsername = response.getUsername();

            response.setUsername("Modified.User");

            assertThat(response.getUsername()).isNotEqualTo(originalUsername);
            assertThat(response.getUsername()).isEqualTo("Modified.User");
        }

        @Test
        @DisplayName("Builder should create independent objects")
        void builderShouldCreateIndependentObjects() {
            TrainerProfileResponse.TrainerProfileResponseBuilder builder = TrainerProfileResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA)
                    .isActive(true);

            TrainerProfileResponse response1 = builder.build();
            TrainerProfileResponse response2 = builder.build();

            response1.setUsername("Modified.User");

            assertThat(response2.getUsername()).isEqualTo("Alice.Smith");
        }

        @Test
        @DisplayName("Modifying trainees list affects response")
        void modifyingTraineesListAffectsResponse() {
            List<TraineeSummaryResponse> trainees = new ArrayList<>();
            trainees.add(TraineeSummaryResponse.builder().username("Trainee.One").build());

            TrainerProfileResponse response = TrainerProfileResponse.builder()
                    .trainees(trainees)
                    .build();

            trainees.add(TraineeSummaryResponse.builder().username("Trainee.Two").build());

            assertThat(response.getTrainees()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Comparison with TraineeProfileResponse Tests")
    class ComparisonWithTraineeProfileResponseTests {

        @Test
        @DisplayName("Should have different structure than TraineeProfileResponse")
        void shouldHaveDifferentStructureThanTraineeProfileResponse() {
            // TrainerProfileResponse has specialization, TraineeProfileResponse has dateOfBirth and address
            TrainerProfileResponse trainerResponse = createValidResponse();

            assertThat(trainerResponse.getSpecialization()).isNotNull();
            assertThat(trainerResponse).hasFieldOrProperty("specialization");
            assertThat(trainerResponse).hasFieldOrProperty("trainees");
        }

        @Test
        @DisplayName("Trainer has trainees, not trainers")
        void trainerHasTraineesNotTrainers() {
            TrainerProfileResponse response = createValidResponse();

            assertThat(response.getTrainees()).isNotNull();
            assertThat(response.getTrainees()).isNotEmpty();
            assertThat(response.getTrainees().get(0)).isInstanceOf(TraineeSummaryResponse.class);
        }
    }
}