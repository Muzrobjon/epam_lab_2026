package com.epam.gym.dto.response;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerSummaryResponse Tests")
class TrainerSummaryResponseTest {

    private TrainerSummaryResponse createValidResponse() {
        return TrainerSummaryResponse.builder()
                .username("Alice.Smith")
                .firstName("Alice")
                .lastName("Smith")
                .specialization(TrainingTypeName.YOGA)
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username(null)
                    .firstName(null)
                    .lastName(null)
                    .specialization(null)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create response with partial fields using builder")
        void shouldCreateResponseWithPartialFieldsUsingBuilder() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Smith")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should create empty response using builder")
        void shouldCreateEmptyResponseUsingBuilder() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder().build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create response with only names")
        void shouldCreateResponseWithOnlyNames() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .firstName("Alice")
                    .lastName("Smith")
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create response with only specialization")
        void shouldCreateResponseWithOnlySpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            TrainerSummaryResponse response = new TrainerSummaryResponse(
                    "Bob.Johnson",
                    "Bob",
                    "Johnson",
                    TrainingTypeName.FITNESS
            );

            assertThat(response.getUsername()).isEqualTo("Bob.Johnson");
            assertThat(response.getFirstName()).isEqualTo("Bob");
            assertThat(response.getLastName()).isEqualTo("Johnson");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should create response using all-args constructor with nulls")
        void shouldCreateResponseUsingAllArgsConstructorWithNulls() {
            TrainerSummaryResponse response = new TrainerSummaryResponse(null, null, null, null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create response with mixed null and non-null values")
        void shouldCreateResponseWithMixedNullAndNonNullValues() {
            TrainerSummaryResponse response = new TrainerSummaryResponse(
                    "Test.User",
                    null,
                    "User",
                    TrainingTypeName.STRENGTH
            );

            assertThat(response.getUsername()).isEqualTo("Test.User");
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isEqualTo("User");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            response.setUsername("Test.User");

            assertThat(response.getUsername()).isEqualTo("Test.User");
        }

        @Test
        @DisplayName("Should set and get firstName")
        void shouldSetAndGetFirstName() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            response.setFirstName("Test");

            assertThat(response.getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should set and get lastName")
        void shouldSetAndGetLastName() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            response.setLastName("User");

            assertThat(response.getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should set and get specialization")
        void shouldSetAndGetSpecialization() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            response.setSpecialization(TrainingTypeName.YOGA);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should allow setting all fields to null")
        void shouldAllowSettingAllFieldsToNull() {
            TrainerSummaryResponse response = createValidResponse();

            response.setUsername(null);
            response.setFirstName(null);
            response.setLastName(null);
            response.setSpecialization(null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should allow overwriting existing values")
        void shouldAllowOverwritingExistingValues() {
            TrainerSummaryResponse response = createValidResponse();

            response.setUsername("New.Username");
            response.setFirstName("NewFirst");
            response.setLastName("NewLast");
            response.setSpecialization(TrainingTypeName.FITNESS);

            assertThat(response.getUsername()).isEqualTo("New.Username");
            assertThat(response.getFirstName()).isEqualTo("NewFirst");
            assertThat(response.getLastName()).isEqualTo("NewLast");
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }
    }

    @Nested
    @DisplayName("Specialization Tests")
    class SpecializationTests {

        @Test
        @DisplayName("Should handle all training type names")
        void shouldHandleAllTrainingTypeNames() {
            for (TrainingTypeName typeName : TrainingTypeName.values()) {
                TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                        .username("Trainer.Test")
                        .specialization(typeName)
                        .build();

                assertThat(response.getSpecialization()).isEqualTo(typeName);
            }
        }

        @Test
        @DisplayName("Should handle FITNESS specialization")
        void shouldHandleFitnessSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
            assertThat(response.getSpecialization().name()).isEqualTo("FITNESS");
        }

        @Test
        @DisplayName("Should handle YOGA specialization")
        void shouldHandleYogaSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
            assertThat(response.getSpecialization().name()).isEqualTo("YOGA");
        }

        @Test
        @DisplayName("Should handle CARDIO specialization")
        void shouldHandleCardioSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
            assertThat(response.getSpecialization().name()).isEqualTo("CARDIO");
        }

        @Test
        @DisplayName("Should handle STRENGTH specialization")
        void shouldHandleStrengthSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.STRENGTH)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
            assertThat(response.getSpecialization().name()).isEqualTo("STRENGTH");
        }

        @Test
        @DisplayName("Should handle null specialization")
        void shouldHandleNullSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(null)
                    .build();

            assertThat(response.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should allow changing specialization")
        void shouldAllowChangingSpecialization() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            response.setSpecialization(TrainingTypeName.FITNESS);

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should compare specializations correctly")
        void shouldCompareSpecializationsCorrectly() {
            TrainerSummaryResponse response1 = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            TrainerSummaryResponse response2 = TrainerSummaryResponse.builder()
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response1.getSpecialization()).isEqualTo(response2.getSpecialization());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TrainerSummaryResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different username")
        void shouldNotBeEqualToResponseWithDifferentUsername() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();
            response2.setUsername("Different.User");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different firstName")
        void shouldNotBeEqualToResponseWithDifferentFirstName() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();
            response2.setFirstName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different lastName")
        void shouldNotBeEqualToResponseWithDifferentLastName() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();
            response2.setLastName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different specialization")
        void shouldNotBeEqualToResponseWithDifferentSpecialization() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();
            response2.setSpecialization(TrainingTypeName.FITNESS);

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TrainerSummaryResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TrainerSummaryResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not a TrainerSummaryResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            TrainerSummaryResponse response1 = new TrainerSummaryResponse();
            TrainerSummaryResponse response2 = new TrainerSummaryResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            TrainerSummaryResponse response = createValidResponse();

            int hashCode1 = response.hashCode();
            int hashCode2 = response.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Different objects with same values should have same hashCode")
        void differentObjectsWithSameValuesShouldHaveSameHashCode() {
            TrainerSummaryResponse response1 = new TrainerSummaryResponse(
                    "User.Name", "User", "Name", TrainingTypeName.YOGA
            );
            TrainerSummaryResponse response2 = new TrainerSummaryResponse(
                    "User.Name", "User", "Name", TrainingTypeName.YOGA
            );

            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should handle equality with null specialization")
        void shouldHandleEqualityWithNullSpecialization() {
            TrainerSummaryResponse response1 = TrainerSummaryResponse.builder()
                    .username("Test.User")
                    .specialization(null)
                    .build();

            TrainerSummaryResponse response2 = TrainerSummaryResponse.builder()
                    .username("Test.User")
                    .specialization(null)
                    .build();

            assertThat(response1).isEqualTo(response2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TrainerSummaryResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("TrainerSummaryResponse");
            assertThat(toString).contains("username=Alice.Smith");
            assertThat(toString).contains("firstName=Alice");
            assertThat(toString).contains("lastName=Smith");
            assertThat(toString).contains("specialization=YOGA");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TrainerSummaryResponse response = new TrainerSummaryResponse();

            String toString = response.toString();

            assertThat(toString).contains("TrainerSummaryResponse");
            assertThat(toString).contains("username=null");
            assertThat(toString).contains("firstName=null");
            assertThat(toString).contains("lastName=null");
            assertThat(toString).contains("specialization=null");
        }

        @Test
        @DisplayName("Should include all fields in toString")
        void shouldIncludeAllFieldsInToString() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Test.User")
                    .firstName("Test")
                    .lastName("User")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            String toString = response.toString();

            assertThat(toString).contains("username=Test.User");
            assertThat(toString).contains("firstName=Test");
            assertThat(toString).contains("lastName=User");
            assertThat(toString).contains("specialization=FITNESS");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("José.García")
                    .firstName("José")
                    .lastName("García")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getUsername()).isEqualTo("José.García");
            assertThat(response.getFirstName()).isEqualTo("José");
            assertThat(response.getLastName()).isEqualTo("García");
        }

        @Test
        @DisplayName("Should handle Cyrillic characters")
        void shouldHandleCyrillicCharacters() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Иван.Петров")
                    .firstName("Иван")
                    .lastName("Петров")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Иван.Петров");
            assertThat(response.getFirstName()).isEqualTo("Иван");
            assertThat(response.getLastName()).isEqualTo("Петров");
        }

        @Test
        @DisplayName("Should handle Chinese characters")
        void shouldHandleChineseCharacters() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("王.明")
                    .firstName("明")
                    .lastName("王")
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getUsername()).isEqualTo("王.明");
            assertThat(response.getFirstName()).isEqualTo("明");
            assertThat(response.getLastName()).isEqualTo("王");
        }

        @Test
        @DisplayName("Should handle Arabic characters")
        void shouldHandleArabicCharacters() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("محمد.أحمد")
                    .firstName("محمد")
                    .lastName("أحمد")
                    .specialization(TrainingTypeName.STRENGTH)
                    .build();

            assertThat(response.getUsername()).isEqualTo("محمد.أحمد");
            assertThat(response.getFirstName()).isEqualTo("محمد");
            assertThat(response.getLastName()).isEqualTo("أحمد");
        }

        @Test
        @DisplayName("Should handle very long values")
        void shouldHandleVeryLongValues() {
            String longUsername = "A".repeat(50) + "." + "B".repeat(50);
            String longFirstName = "First".repeat(50);
            String longLastName = "Last".repeat(50);

            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username(longUsername)
                    .firstName(longFirstName)
                    .lastName(longLastName)
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getUsername()).hasSize(101);
            assertThat(response.getFirstName()).hasSize(250);
            assertThat(response.getLastName()).hasSize(200);
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
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
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
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
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("User@Name#123")
                    .firstName("First-Name")
                    .lastName("Last'Name")
                    .build();

            assertThat(response.getUsername()).isEqualTo("User@Name#123");
            assertThat(response.getFirstName()).isEqualTo("First-Name");
            assertThat(response.getLastName()).isEqualTo("Last'Name");
        }

        @Test
        @DisplayName("Should handle newline characters")
        void shouldHandleNewlineCharacters() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("User\nName")
                    .firstName("First\nName")
                    .lastName("Last\nName")
                    .build();

            assertThat(response.getUsername()).contains("\n");
            assertThat(response.getFirstName()).contains("\n");
            assertThat(response.getLastName()).contains("\n");
        }

        @Test
        @DisplayName("Should handle tab characters")
        void shouldHandleTabCharacters() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("User\tName")
                    .firstName("First\tName")
                    .lastName("Last\tName")
                    .build();

            assertThat(response.getUsername()).contains("\t");
            assertThat(response.getFirstName()).contains("\t");
            assertThat(response.getLastName()).contains("\t");
        }
    }

    @Nested
    @DisplayName("Field Count Tests")
    class FieldCountTests {

        @Test
        @DisplayName("Should have exactly 4 fields")
        void shouldHaveExactlyFourFields() {
            assertThat(TrainerSummaryResponse.class.getDeclaredFields())
                    .hasSize(4)
                    .extracting("name")
                    .containsExactlyInAnyOrder(
                            "username",
                            "firstName",
                            "lastName",
                            "specialization"
                    );
        }
    }

    @Nested
    @DisplayName("Username Format Tests")
    class UsernameFormatTests {

        @Test
        @DisplayName("Should handle standard username format")
        void shouldHandleStandardUsernameFormat() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Smith")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getUsername()).contains(".");
        }

        @Test
        @DisplayName("Should handle username with numbers")
        void shouldHandleUsernameWithNumbers() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Smith1")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith1");
        }

        @Test
        @DisplayName("Should handle username with multiple dots")
        void shouldHandleUsernameWithMultipleDots() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Middle.Smith")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Middle.Smith");
        }

        @Test
        @DisplayName("Should handle username without dots")
        void shouldHandleUsernameWithoutDots() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("AliceSmith")
                    .build();

            assertThat(response.getUsername()).isEqualTo("AliceSmith");
        }
    }

    @Nested
    @DisplayName("Typical Usage Scenarios Tests")
    class TypicalUsageScenariosTests {

        @Test
        @DisplayName("Should represent trainer summary for list display")
        void shouldRepresentTrainerSummaryForListDisplay() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getUsername()).isNotNull();
            assertThat(response.getFirstName()).isNotNull();
            assertThat(response.getLastName()).isNotNull();
            assertThat(response.getSpecialization()).isNotNull();
        }

        @Test
        @DisplayName("Should represent trainer summary with minimal data")
        void shouldRepresentTrainerSummaryWithMinimalData() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Minimal.User")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getUsername()).isEqualTo("Minimal.User");
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should be usable in collections")
        void shouldBeUsableInCollections() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = TrainerSummaryResponse.builder()
                    .username("Bob.Johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            List<TrainerSummaryResponse> list = Arrays.asList(response1, response2);

            assertThat(list).hasSize(2);
            assertThat(list).extracting(TrainerSummaryResponse::getUsername)
                    .containsExactly("Alice.Smith", "Bob.Johnson");
        }

        @Test
        @DisplayName("Should be usable in sets")
        void shouldBeUsableInSets() {
            TrainerSummaryResponse response1 = createValidResponse();
            TrainerSummaryResponse response2 = createValidResponse();
            TrainerSummaryResponse response3 = TrainerSummaryResponse.builder()
                    .username("Bob.Johnson")
                    .firstName("Bob")
                    .lastName("Johnson")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            Set<TrainerSummaryResponse> set = new HashSet<>();
            set.add(response1);
            set.add(response2);
            set.add(response3);

            assertThat(set).hasSize(2);
        }

        @Test
        @DisplayName("Should be usable as map key")
        void shouldBeUsableAsMapKey() {
            TrainerSummaryResponse response = createValidResponse();

            java.util.Map<TrainerSummaryResponse, String> map = new java.util.HashMap<>();
            map.put(response, "value");

            assertThat(map.get(response)).isEqualTo("value");
            assertThat(map.get(createValidResponse())).isEqualTo("value");
        }

        @Test
        @DisplayName("Should filter trainers by specialization")
        void shouldFilterTrainersBySpecialization() {
            List<TrainerSummaryResponse> trainers = Arrays.asList(
                    TrainerSummaryResponse.builder()
                            .username("Yoga.Trainer")
                            .specialization(TrainingTypeName.YOGA)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("Fitness.Trainer")
                            .specialization(TrainingTypeName.FITNESS)
                            .build(),
                    TrainerSummaryResponse.builder()
                            .username("Another.Yoga")
                            .specialization(TrainingTypeName.YOGA)
                            .build()
            );

            long yogaTrainersCount = trainers.stream()
                    .filter(t -> t.getSpecialization() == TrainingTypeName.YOGA)
                    .count();

            assertThat(yogaTrainersCount).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            TrainerSummaryResponse response = createValidResponse();
            String originalUsername = response.getUsername();

            response.setUsername("Modified.User");

            assertThat(response.getUsername()).isNotEqualTo(originalUsername);
            assertThat(response.getUsername()).isEqualTo("Modified.User");
        }

        @Test
        @DisplayName("Builder should create independent objects")
        void builderShouldCreateIndependentObjects() {
            TrainerSummaryResponse.TrainerSummaryResponseBuilder builder = TrainerSummaryResponse.builder()
                    .username("Alice.Smith")
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(TrainingTypeName.YOGA);

            TrainerSummaryResponse response1 = builder.build();
            TrainerSummaryResponse response2 = builder.build();

            response1.setUsername("Modified.User");

            assertThat(response2.getUsername()).isEqualTo("Alice.Smith");
        }
    }

    @Nested
    @DisplayName("Name Combination Tests")
    class NameCombinationTests {

        @Test
        @DisplayName("Should handle same first and last name")
        void shouldHandleSameFirstAndLastName() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("John.John")
                    .firstName("John")
                    .lastName("John")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getFirstName()).isEqualTo(response.getLastName());
        }

        @Test
        @DisplayName("Should handle hyphenated names")
        void shouldHandleHyphenatedNames() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Mary-Jane.Watson-Parker")
                    .firstName("Mary-Jane")
                    .lastName("Watson-Parker")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("Watson-Parker");
        }

        @Test
        @DisplayName("Should handle names with apostrophes")
        void shouldHandleNamesWithApostrophes() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Patrick.O'Brien")
                    .firstName("Patrick")
                    .lastName("O'Brien")
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getLastName()).isEqualTo("O'Brien");
        }

        @Test
        @DisplayName("Should handle single character names")
        void shouldHandleSingleCharacterNames() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("J.D")
                    .firstName("J")
                    .lastName("D")
                    .specialization(TrainingTypeName.STRENGTH)
                    .build();

            assertThat(response.getFirstName()).hasSize(1);
            assertThat(response.getLastName()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Comparison with TraineeSummaryResponse Tests")
    class ComparisonWithTraineeSummaryResponseTests {

        @Test
        @DisplayName("Should have specialization unlike TraineeSummaryResponse")
        void shouldHaveSpecializationUnlikeTraineeSummaryResponse() {
            TrainerSummaryResponse trainerResponse = createValidResponse();

            assertThat(trainerResponse.getSpecialization()).isNotNull();
            assertThat(trainerResponse).hasFieldOrProperty("specialization");
        }

        @Test
        @DisplayName("Should have same basic name fields as TraineeSummaryResponse")
        void shouldHaveSameBasicNameFieldsAsTraineeSummaryResponse() {
            TrainerSummaryResponse response = createValidResponse();

            assertThat(response).hasFieldOrProperty("username");
            assertThat(response).hasFieldOrProperty("firstName");
            assertThat(response).hasFieldOrProperty("lastName");
        }
    }

    @Nested
    @DisplayName("Trainer Specialization Scenarios Tests")
    class TrainerSpecializationScenariosTests {

        @Test
        @DisplayName("Should create yoga trainer")
        void shouldCreateYogaTrainer() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Yoga.Master")
                    .firstName("Yoga")
                    .lastName("Master")
                    .specialization(TrainingTypeName.YOGA)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.YOGA);
        }

        @Test
        @DisplayName("Should create fitness trainer")
        void shouldCreateFitnessTrainer() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Fitness.Pro")
                    .firstName("Fitness")
                    .lastName("Pro")
                    .specialization(TrainingTypeName.FITNESS)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.FITNESS);
        }

        @Test
        @DisplayName("Should create cardio trainer")
        void shouldCreateCardioTrainer() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Cardio.Expert")
                    .firstName("Cardio")
                    .lastName("Expert")
                    .specialization(TrainingTypeName.CARDIO)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.CARDIO);
        }

        @Test
        @DisplayName("Should create strength trainer")
        void shouldCreateStrengthTrainer() {
            TrainerSummaryResponse response = TrainerSummaryResponse.builder()
                    .username("Strength.Coach")
                    .firstName("Strength")
                    .lastName("Coach")
                    .specialization(TrainingTypeName.STRENGTH)
                    .build();

            assertThat(response.getSpecialization()).isEqualTo(TrainingTypeName.STRENGTH);
        }

        @Test
        @DisplayName("Should group trainers by specialization")
        void shouldGroupTrainersBySpecialization() {
            List<TrainerSummaryResponse> trainers = Arrays.asList(
                    TrainerSummaryResponse.builder().username("T1").specialization(TrainingTypeName.YOGA).build(),
                    TrainerSummaryResponse.builder().username("T2").specialization(TrainingTypeName.FITNESS).build(),
                    TrainerSummaryResponse.builder().username("T3").specialization(TrainingTypeName.YOGA).build(),
                    TrainerSummaryResponse.builder().username("T4").specialization(TrainingTypeName.CARDIO).build()
            );

            java.util.Map<TrainingTypeName, List<TrainerSummaryResponse>> grouped = trainers.stream()
                    .collect(java.util.stream.Collectors.groupingBy(TrainerSummaryResponse::getSpecialization));

            assertThat(grouped.get(TrainingTypeName.YOGA)).hasSize(2);
            assertThat(grouped.get(TrainingTypeName.FITNESS)).hasSize(1);
            assertThat(grouped.get(TrainingTypeName.CARDIO)).hasSize(1);
        }
    }
}