package com.epam.gym.dto.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RegistrationResponse Tests")
class RegistrationResponseTest {

    private RegistrationResponse createValidResponse() {
        return RegistrationResponse.builder()
                .username("John.Doe")
                .password("aB3$xY9@kL")
                .build();
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create response with all fields using builder")
        void shouldCreateResponseWithAllFieldsUsingBuilder() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("Alice.Smith")
                    .password("pA5#mN2@qR")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Alice.Smith");
            assertThat(response.getPassword()).isEqualTo("pA5#mN2@qR");
        }

        @Test
        @DisplayName("Should create response with null fields using builder")
        void shouldCreateResponseWithNullFieldsUsingBuilder() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username(null)
                    .password(null)
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create response with only username using builder")
        void shouldCreateResponseWithOnlyUsernameUsingBuilder() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("Bob.Wilson")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Bob.Wilson");
            assertThat(response.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create response with only password using builder")
        void shouldCreateResponseWithOnlyPasswordUsingBuilder() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .password("secretPass123")
                    .build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getPassword()).isEqualTo("secretPass123");
        }

        @Test
        @DisplayName("Should create empty response using builder")
        void shouldCreateEmptyResponseUsingBuilder() {
            RegistrationResponse response = RegistrationResponse.builder().build();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getPassword()).isNull();
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create response using no-args constructor")
        void shouldCreateResponseUsingNoArgsConstructor() {
            RegistrationResponse response = new RegistrationResponse();

            assertThat(response.getUsername()).isNull();
            assertThat(response.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create response using all-args constructor")
        void shouldCreateResponseUsingAllArgsConstructor() {
            RegistrationResponse response = new RegistrationResponse("Charlie.Brown", "xY7@kL3#mN");

            assertThat(response.getUsername()).isEqualTo("Charlie.Brown");
            assertThat(response.getPassword()).isEqualTo("xY7@kL3#mN");
        }

        @Test
        @DisplayName("Should create response with null values using all-args constructor")
        void shouldCreateResponseWithNullValuesUsingAllArgsConstructor() {
            RegistrationResponse response = new RegistrationResponse(null, null);

            assertThat(response.getUsername()).isNull();
            assertThat(response.getPassword()).isNull();
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get username")
        void shouldSetAndGetUsername() {
            RegistrationResponse response = new RegistrationResponse();

            response.setUsername("Diana.Prince");

            assertThat(response.getUsername()).isEqualTo("Diana.Prince");
        }

        @Test
        @DisplayName("Should set and get password")
        void shouldSetAndGetPassword() {
            RegistrationResponse response = new RegistrationResponse();

            response.setPassword("newPassword123!");

            assertThat(response.getPassword()).isEqualTo("newPassword123!");
        }

        @Test
        @DisplayName("Should update username")
        void shouldUpdateUsername() {
            RegistrationResponse response = createValidResponse();

            response.setUsername("Updated.Username");

            assertThat(response.getUsername()).isEqualTo("Updated.Username");
        }

        @Test
        @DisplayName("Should update password")
        void shouldUpdatePassword() {
            RegistrationResponse response = createValidResponse();

            response.setPassword("updatedPass456!");

            assertThat(response.getPassword()).isEqualTo("updatedPass456!");
        }

        @Test
        @DisplayName("Should set username to null")
        void shouldSetUsernameToNull() {
            RegistrationResponse response = createValidResponse();

            response.setUsername(null);

            assertThat(response.getUsername()).isNull();
        }

        @Test
        @DisplayName("Should set password to null")
        void shouldSetPasswordToNull() {
            RegistrationResponse response = createValidResponse();

            response.setPassword(null);

            assertThat(response.getPassword()).isNull();
        }
    }

    @Nested
    @DisplayName("Username Format Tests")
    class UsernameFormatTests {

        @Test
        @DisplayName("Should handle standard FirstName.LastName format")
        void shouldHandleStandardFirstNameLastNameFormat() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe");
        }

        @Test
        @DisplayName("Should handle username with numeric suffix")
        void shouldHandleUsernameWithNumericSuffix() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe1")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe1");
        }

        @Test
        @DisplayName("Should handle username with large numeric suffix")
        void shouldHandleUsernameWithLargeNumericSuffix() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe999")
                    .build();

            assertThat(response.getUsername()).isEqualTo("John.Doe999");
        }

        @Test
        @DisplayName("Should handle hyphenated names in username")
        void shouldHandleHyphenatedNamesInUsername() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("Mary-Jane.Watson-Parker")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Mary-Jane.Watson-Parker");
        }

        @Test
        @DisplayName("Should handle single character names")
        void shouldHandleSingleCharacterNames() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("A.B")
                    .build();

            assertThat(response.getUsername()).isEqualTo("A.B");
        }

        @Test
        @DisplayName("Should handle long names")
        void shouldHandleLongNames() {
            String longUsername = "VeryLongFirstName.VeryLongLastName";

            RegistrationResponse response = RegistrationResponse.builder()
                    .username(longUsername)
                    .build();

            assertThat(response.getUsername()).isEqualTo(longUsername);
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("")
                    .build();

            assertThat(response.getUsername()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Format Tests")
    class PasswordFormatTests {

        @Test
        @DisplayName("Should handle password with special characters")
        void shouldHandlePasswordWithSpecialCharacters() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .password("aB3$xY9@kL")
                    .build();

            assertThat(response.getPassword()).isEqualTo("aB3$xY9@kL");
        }

        @Test
        @DisplayName("Should handle alphanumeric password")
        void shouldHandleAlphanumericPassword() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .password("AbCdEf123456")
                    .build();

            assertThat(response.getPassword()).isEqualTo("AbCdEf123456");
        }

        @Test
        @DisplayName("Should handle password with all special characters")
        void shouldHandlePasswordWithAllSpecialCharacters() {
            String password = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

            RegistrationResponse response = RegistrationResponse.builder()
                    .password(password)
                    .build();

            assertThat(response.getPassword()).isEqualTo(password);
        }

        @Test
        @DisplayName("Should handle short password")
        void shouldHandleShortPassword() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .password("abc")
                    .build();

            assertThat(response.getPassword()).isEqualTo("abc");
        }

        @Test
        @DisplayName("Should handle long password")
        void shouldHandleLongPassword() {
            String longPassword = "a".repeat(100);

            RegistrationResponse response = RegistrationResponse.builder()
                    .password(longPassword)
                    .build();

            assertThat(response.getPassword()).hasSize(100);
        }

        @Test
        @DisplayName("Should handle empty password")
        void shouldHandleEmptyPassword() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .password("")
                    .build();

            assertThat(response.getPassword()).isEmpty();
        }

        @Test
        @DisplayName("Should handle password with unicode characters")
        void shouldHandlePasswordWithUnicodeCharacters() {
            String unicodePassword = "пароль密码パスワード";

            RegistrationResponse response = RegistrationResponse.builder()
                    .password(unicodePassword)
                    .build();

            assertThat(response.getPassword()).isEqualTo(unicodePassword);
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            RegistrationResponse response = createValidResponse();

            assertThat(response).isEqualTo(response);
        }

        @Test
        @DisplayName("Should be equal to identical response")
        void shouldBeEqualToIdenticalResponse() {
            RegistrationResponse response1 = createValidResponse();
            RegistrationResponse response2 = createValidResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal to response with different username")
        void shouldNotBeEqualToResponseWithDifferentUsername() {
            RegistrationResponse response1 = createValidResponse();
            RegistrationResponse response2 = createValidResponse();
            response2.setUsername("Different.User");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to response with different password")
        void shouldNotBeEqualToResponseWithDifferentPassword() {
            RegistrationResponse response1 = createValidResponse();
            RegistrationResponse response2 = createValidResponse();
            response2.setPassword("differentPass");

            assertThat(response1).isNotEqualTo(response2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            RegistrationResponse response = createValidResponse();

            assertThat(response).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            RegistrationResponse response = createValidResponse();

            assertThat(response).isNotEqualTo("not a RegistrationResponse");
        }

        @Test
        @DisplayName("Empty responses should be equal")
        void emptyResponsesShouldBeEqual() {
            RegistrationResponse response1 = new RegistrationResponse();
            RegistrationResponse response2 = new RegistrationResponse();

            assertThat(response1).isEqualTo(response2);
            assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
        }

        @Test
        @DisplayName("Responses with null username should be equal if passwords match")
        void responsesWithNullUsernameShouldBeEqualIfPasswordsMatch() {
            RegistrationResponse response1 = RegistrationResponse.builder()
                    .username(null)
                    .password("samePass")
                    .build();
            RegistrationResponse response2 = RegistrationResponse.builder()
                    .username(null)
                    .password("samePass")
                    .build();

            assertThat(response1).isEqualTo(response2);
        }

        @Test
        @DisplayName("Responses with null password should be equal if usernames match")
        void responsesWithNullPasswordShouldBeEqualIfUsernamesMatch() {
            RegistrationResponse response1 = RegistrationResponse.builder()
                    .username("Same.User")
                    .password(null)
                    .build();
            RegistrationResponse response2 = RegistrationResponse.builder()
                    .username("Same.User")
                    .password(null)
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
            RegistrationResponse response = createValidResponse();

            String toString = response.toString();

            assertThat(toString).contains("RegistrationResponse");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("password=aB3$xY9@kL");
        }

        @Test
        @DisplayName("Should handle null values in toString")
        void shouldHandleNullValuesInToString() {
            RegistrationResponse response = new RegistrationResponse();

            String toString = response.toString();

            assertThat(toString).contains("RegistrationResponse");
            assertThat(toString).contains("username=null");
            assertThat(toString).contains("password=null");
        }

        @Test
        @DisplayName("Should handle empty values in toString")
        void shouldHandleEmptyValuesInToString() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("")
                    .password("")
                    .build();

            String toString = response.toString();

            assertThat(toString).contains("username=");
            assertThat(toString).contains("password=");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle username with unicode characters")
        void shouldHandleUsernameWithUnicodeCharacters() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("José.García")
                    .password("password123")
                    .build();

            assertThat(response.getUsername()).isEqualTo("José.García");
        }

        @Test
        @DisplayName("Should handle username with Cyrillic characters")
        void shouldHandleUsernameWithCyrillicCharacters() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("Иван.Петров")
                    .password("password123")
                    .build();

            assertThat(response.getUsername()).isEqualTo("Иван.Петров");
        }

        @Test
        @DisplayName("Should handle username with Chinese characters")
        void shouldHandleUsernameWithChineseCharacters() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("明.王")
                    .password("password123")
                    .build();

            assertThat(response.getUsername()).isEqualTo("明.王");
        }

        @Test
        @DisplayName("Should handle whitespace in values")
        void shouldHandleWhitespaceInValues() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("  John.Doe  ")
                    .password("  password  ")
                    .build();

            assertThat(response.getUsername()).isEqualTo("  John.Doe  ");
            assertThat(response.getPassword()).isEqualTo("  password  ");
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            String longUsername = "A".repeat(50) + "." + "B".repeat(50);

            RegistrationResponse response = RegistrationResponse.builder()
                    .username(longUsername)
                    .build();

            assertThat(response.getUsername()).hasSize(101);
        }

        @Test
        @DisplayName("Should handle password with newlines")
        void shouldHandlePasswordWithNewlines() {
            String passwordWithNewlines = "pass\nword\r\n123";

            RegistrationResponse response = RegistrationResponse.builder()
                    .password(passwordWithNewlines)
                    .build();

            assertThat(response.getPassword()).isEqualTo(passwordWithNewlines);
        }

        @Test
        @DisplayName("Should handle password with tabs")
        void shouldHandlePasswordWithTabs() {
            String passwordWithTabs = "pass\tword\t123";

            RegistrationResponse response = RegistrationResponse.builder()
                    .password(passwordWithTabs)
                    .build();

            assertThat(response.getPassword()).isEqualTo(passwordWithTabs);
        }
    }

    @Nested
    @DisplayName("Typical Usage Scenarios Tests")
    class TypicalUsageScenariosTests {

        @Test
        @DisplayName("Should represent trainee registration response")
        void shouldRepresentTraineeRegistrationResponse() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe")
                    .password("aB3$xY9@kL")
                    .build();

            assertThat(response.getUsername()).matches("[A-Za-z]+\\.[A-Za-z]+");
            assertThat(response.getPassword()).isNotEmpty();
        }

        @Test
        @DisplayName("Should represent trainer registration response")
        void shouldRepresentTrainerRegistrationResponse() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("Alice.Smith")
                    .password("pQ7#nM4@xR")
                    .build();

            assertThat(response.getUsername()).contains(".");
            assertThat(response.getPassword()).hasSize(10);
        }

        @Test
        @DisplayName("Should represent registration with duplicate username handling")
        void shouldRepresentRegistrationWithDuplicateUsernameHandling() {
            // When a username already exists, a numeric suffix is added
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe2")
                    .password("kL9@mN3#pQ")
                    .build();

            assertThat(response.getUsername()).endsWith("2");
        }

        @Test
        @DisplayName("Should represent registration with multiple duplicates")
        void shouldRepresentRegistrationWithMultipleDuplicates() {
            RegistrationResponse response = RegistrationResponse.builder()
                    .username("John.Doe15")
                    .password("xY2@kL8#mN")
                    .build();

            assertThat(response.getUsername()).matches("John\\.Doe\\d+");
        }
    }

    @Nested
    @DisplayName("Field Count Tests")
    class FieldCountTests {

        @Test
        @DisplayName("Should have exactly 2 fields")
        void shouldHaveExactlyTwoFields() {
            assertThat(RegistrationResponse.class.getDeclaredFields())
                    .hasSize(2)
                    .extracting("name")
                    .containsExactlyInAnyOrder("username", "password");
        }
    }
}