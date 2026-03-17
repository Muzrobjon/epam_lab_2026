package com.epam.gym.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TraineeSummaryResponse Tests")
class TraineeSummaryResponseTest {

    private TraineeSummaryResponse createValidResponse() {
        return TraineeSummaryResponse.builder()
                .username("John.Doe")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username(null)
                    .firstName(null)
                    .lastName(null)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should create response with partial fields using builder")
        void shouldCreateResponseWithPartialFieldsUsingBuilder() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should create empty response using builder")
        void shouldCreateEmptyResponseUsingBuilder() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder().build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should create response with only firstName and lastName")
        void shouldCreateResponseWithOnlyFirstNameAndLastName() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isEqualTo("John");
            assertThat(response.getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            TraineeSummaryResponse response = new TraineeSummaryResponse();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            TraineeSummaryResponse response = new TraineeSummaryResponse(
                    "Alice.Smith",
                    "Alice",
                    "Smith"
            );

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getFirstName()).isEqualTo("Alice");
            assertThat(response.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should create response using all-args constructor with nulls")
        void shouldCreateResponseUsingAllArgsConstructorWithNulls() {
            TraineeSummaryResponse response = new TraineeSummaryResponse(null, null, null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            TraineeSummaryResponse response = new TraineeSummaryResponse();

            response.setUsername("Test.User");

            assertThat(response.getUsername()).isEqualTo("Test.User");
        }

        @Test
        @DisplayName("Should set and get firstName")
        void shouldSetAndGetFirstName() {
            TraineeSummaryResponse response = new TraineeSummaryResponse();

            response.setFirstName("Test");

            assertThat(response.getFirstName()).isEqualTo("Test");
        }

        @Test
        @DisplayName("Should set and get lastName")
        void shouldSetAndGetLastName() {
            TraineeSummaryResponse response = new TraineeSummaryResponse();

            response.setLastName("User");

            assertThat(response.getLastName()).isEqualTo("User");
        }

        @Test
        @DisplayName("Should allow setting all fields to null")
        void shouldAllowSettingAllFieldsToNull() {
            TraineeSummaryResponse response = createValidResponse();

            response.setUsername(null);
            response.setFirstName(null);
            response.setLastName(null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should allow overwriting existing values")
        void shouldAllowOverwritingExistingValues() {
            TraineeSummaryResponse response = createValidResponse();

            response.setUsername("New.Username");
            response.setFirstName("NewFirst");
            response.setLastName("NewLast");

            assertThat(response.getUsername()).isEqualTo("New.Username");
            assertThat(response.getFirstName()).isEqualTo("NewFirst");
            assertThat(response.getLastName()).isEqualTo("NewLast");
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TraineeSummaryResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different username")
        void shouldNotBeEqualToResponseWithDifferentUsername() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = createValidResponse();
            response2.setUsername("Different.User");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different firstName")
        void shouldNotBeEqualToResponseWithDifferentFirstName() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = createValidResponse();
            response2.setFirstName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different lastName")
        void shouldNotBeEqualToResponseWithDifferentLastName() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = createValidResponse();
            response2.setLastName("Different");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TraineeSummaryResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            TraineeSummaryResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not a TraineeSummaryResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            TraineeSummaryResponse response1 = new TraineeSummaryResponse();
            TraineeSummaryResponse response2 = new TraineeSummaryResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            TraineeSummaryResponse response = createValidResponse();

            int hashCode1 = response.hashCode();
            int hashCode2 = response.hashCode();

            assertThat(hashCode1).isEqualTo(hashCode2);
        }

        @Test
        @DisplayName("Different objects with same values should have same hashCode")
        void differentObjectsWithSameValuesShouldHaveSameHashCode() {
            TraineeSummaryResponse response1 = new TraineeSummaryResponse("User.Name", "User", "Name");
            TraineeSummaryResponse response2 = new TraineeSummaryResponse("User.Name", "User", "Name");

            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TraineeSummaryResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("TraineeSummaryResponse");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("firstName=John");
            assertThat(toString).contains("lastName=Doe");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            TraineeSummaryResponse response = new TraineeSummaryResponse();

            String toString = response.toString();

            assertThat(toString).contains("TraineeSummaryResponse");
            assertThat(toString).contains("username=null");
            assertThat(toString).contains("firstName=null");
            assertThat(toString).contains("lastName=null");
        }

        @Test
        @DisplayName("Should include all fields in toString")
        void shouldIncludeAllFieldsInToString() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("Test.User")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            String toString = response.toString();

            assertThat(toString).contains("username=Test.User");
            assertThat(toString).contains("firstName=Test");
            assertThat(toString).contains("lastName=User");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("王.明")
                    .firstName("明")
                    .lastName("王")
                    .build();

            assertThat(response.getUsername()).isEqualTo("王.明");
            assertThat(response.getFirstName()).isEqualTo("明");
            assertThat(response.getLastName()).isEqualTo("王");
        }

        @Test
        @DisplayName("Should handle Arabic characters")
        void shouldHandleArabicCharacters() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("محمد.أحمد")
                    .firstName("محمد")
                    .lastName("أحمد")
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

            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
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
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("User\tName")
                    .firstName("First\tName")
                    .lastName("Last\tName")
                    .build();

            assertThat(response.getUsername()).contains("\t");
            assertThat(response.getFirstName()).contains("\t");
            assertThat(response.getLastName()).contains("\t");
        }

        @Test
        @DisplayName("Should handle mixed case")
        void shouldHandleMixedCase() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("JoHn.DoE")
                    .firstName("JoHn")
                    .lastName("DoE")
                    .build();

            assertThat(response.getUsername()).isEqualTo("JoHn.DoE");
            assertThat(response.getFirstName()).isEqualTo("JoHn");
            assertThat(response.getLastName()).isEqualTo("DoE");
        }
    }

    @Nested
    @DisplayName("Field Count Tests")
    class FieldCountTests {

        @Test
        @DisplayName("Should have exactly 3 fields")
        void shouldHaveExactlyThreeFields() {
            assertThat(TraineeSummaryResponse.class.getDeclaredFields())
                    .hasSize(3)
                    .extracting("name")
                    .containsExactlyInAnyOrder(
                            "username",
                            "firstName",
                            "lastName"
                    );
        }
    }

    @Nested
    @DisplayName("Username Format Tests")
    class UsernameFormatTests {

        @Test
        @DisplayName("Should handle standard username format")
        void shouldHandleStandardUsernameFormat() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
            assertThat(response.getUsername()).contains(".");
        }

        @Test
        @DisplayName("Should handle username with numbers")
        void shouldHandleUsernameWithNumbers() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Doe1")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe1");
        }

        @Test
        @DisplayName("Should handle username with multiple dots")
        void shouldHandleUsernameWithMultipleDots() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Middle.Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Middle.Doe");
        }

        @Test
        @DisplayName("Should handle username without dots")
        void shouldHandleUsernameWithoutDots() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("JohnDoe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("JohnDoe");
        }

        @Test
        @DisplayName("Should handle username with underscore")
        void shouldHandleUsernameWithUnderscore() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John_Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John_Doe");
        }
    }

    @Nested
    @DisplayName("Typical Usage Scenarios Tests")
    class TypicalUsageScenariosTests {

        @Test
        @DisplayName("Should represent trainee summary for list display")
        void shouldRepresentTraineeSummaryForListDisplay() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            assertThat(response.getUsername()).isNotNull();
            assertThat(response.getFirstName()).isNotNull();
            assertThat(response.getLastName()).isNotNull();
        }

        @Test
        @DisplayName("Should represent trainee summary with minimal data")
        void shouldRepresentTraineeSummaryWithMinimalData() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("Minimal.User")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Minimal.User");
            assertThat(response.getFirstName()).isNull();
            assertThat(response.getLastName()).isNull();
        }

        @Test
        @DisplayName("Should be usable in collections")
        void shouldBeUsableInCollections() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = TraineeSummaryResponse.builder()
                    .username("Jane.Smith")
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            java.util.List<TraineeSummaryResponse> list = java.util.Arrays.asList(response1, response2);

            assertThat(list).hasSize(2);
            assertThat(list).extracting(TraineeSummaryResponse::getUsername)
                    .containsExactly("John.Doe", "Jane.Smith");
        }

        @Test
        @DisplayName("Should be usable in sets")
        void shouldBeUsableInSets() {
            TraineeSummaryResponse response1 = createValidResponse();
            TraineeSummaryResponse response2 = createValidResponse();
            TraineeSummaryResponse response3 = TraineeSummaryResponse.builder()
                    .username("Jane.Smith")
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            java.util.Set<TraineeSummaryResponse> set = new java.util.HashSet<>();
            set.add(response1);
            set.add(response2);
            set.add(response3);

            assertThat(set).hasSize(2); // response1 and response2 are equal
        }

        @Test
        @DisplayName("Should be usable as map key")
        void shouldBeUsableAsMapKey() {
            TraineeSummaryResponse response = createValidResponse();

            java.util.Map<TraineeSummaryResponse, String> map = new java.util.HashMap<>();
            map.put(response, "value");

            assertThat(map.get(response)).isEqualTo("value");
            assertThat(map.get(createValidResponse())).isEqualTo("value");
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should allow modification after creation")
        void shouldAllowModificationAfterCreation() {
            TraineeSummaryResponse response = createValidResponse();
            String originalUsername = response.getUsername();

            response.setUsername("Modified.User");

            assertThat(response.getUsername()).isNotEqualTo(originalUsername);
            assertThat(response.getUsername()).isEqualTo("Modified.User");
        }

        @Test
        @DisplayName("Builder should create independent objects")
        void builderShouldCreateIndependentObjects() {
            TraineeSummaryResponse.TraineeSummaryResponseBuilder builder = TraineeSummaryResponse.builder()
                    .username("John.Doe")
                    .firstName("John")
                    .lastName("Doe");

            TraineeSummaryResponse response1 = builder.build();
            TraineeSummaryResponse response2 = builder.build();

            response1.setUsername("Modified.User");

            assertThat(response2.getUsername()).isEqualTo("John.Doe");
        }
    }

    @Nested
    @DisplayName("Name Combination Tests")
    class NameCombinationTests {

        @Test
        @DisplayName("Should handle same first and last name")
        void shouldHandleSameFirstAndLastName() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("John.John")
                    .firstName("John")
                    .lastName("John")
                    .build();

            assertThat(response.getFirstName()).isEqualTo(response.getLastName());
        }

        @Test
        @DisplayName("Should handle hyphenated names")
        void shouldHandleHyphenatedNames() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("Mary-Jane.Watson-Parker")
                    .firstName("Mary-Jane")
                    .lastName("Watson-Parker")
                    .build();

            assertThat(response.getFirstName()).isEqualTo("Mary-Jane");
            assertThat(response.getLastName()).isEqualTo("Watson-Parker");
        }

        @Test
        @DisplayName("Should handle names with apostrophes")
        void shouldHandleNamesWithApostrophes() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("Patrick.O'Brien")
                    .firstName("Patrick")
                    .lastName("O'Brien")
                    .build();

            assertThat(response.getLastName()).isEqualTo("O'Brien");
        }

        @Test
        @DisplayName("Should handle single character names")
        void shouldHandleSingleCharacterNames() {
            TraineeSummaryResponse response = TraineeSummaryResponse.builder()
                    .username("J.D")
                    .firstName("J")
                    .lastName("D")
                    .build();

            assertThat(response.getFirstName()).hasSize(1);
            assertThat(response.getLastName()).hasSize(1);
        }
    }
}