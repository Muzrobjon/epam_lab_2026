package com.epam.gym.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateTrainerRequest Tests")
class UpdateTrainerRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UpdateTrainerRequest createValidRequest() {
        return UpdateTrainerRequest.builder()
                .username("Alice.Smith")
                .password("password123")
                .firstName("Alice")
                .lastName("Smith")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields and isActive true")
        void shouldPassValidationWithAllValidFieldsAndIsActiveTrue() {
            UpdateTrainerRequest request = createValidRequest();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with all valid fields and isActive false")
        void shouldPassValidationWithAllValidFieldsAndIsActiveFalse() {
            UpdateTrainerRequest request = createValidRequest();
            request.setIsActive(false);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("a")
                    .password("b")
                    .firstName("c")
                    .lastName("d")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("a".repeat(100))
                    .password("b".repeat(100))
                    .firstName("c".repeat(100))
                    .lastName("d".repeat(100))
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Username Validation Tests")
    class UsernameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when username is blank or null")
        void shouldFailValidationWhenUsernameIsBlankOrNull(String username) {
            UpdateTrainerRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            UpdateTrainerRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            UpdateTrainerRequest request = createValidRequest();
            request.setUsername("alice.smith@test_123");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Password Validation Tests")
    class PasswordValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when password is blank or null")
        void shouldFailValidationWhenPasswordIsBlankOrNull(String password) {
            UpdateTrainerRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            UpdateTrainerRequest request = createValidRequest();
            request.setPassword("validPassword123");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            UpdateTrainerRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%&*");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("First Name Validation Tests")
    class FirstNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when first name is blank or null")
        void shouldFailValidationWhenFirstNameIsBlankOrNull(String firstName) {
            UpdateTrainerRequest request = createValidRequest();
            request.setFirstName(firstName);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid first name")
        void shouldPassValidationWithValidFirstName() {
            UpdateTrainerRequest request = createValidRequest();
            request.setFirstName("Jane");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with hyphenated first name")
        void shouldPassValidationWithHyphenatedFirstName() {
            UpdateTrainerRequest request = createValidRequest();
            request.setFirstName("Mary-Jane");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with first name containing apostrophe")
        void shouldPassValidationWithFirstNameContainingApostrophe() {
            UpdateTrainerRequest request = createValidRequest();
            request.setFirstName("O'Brien");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Last Name Validation Tests")
    class LastNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r"})
        @DisplayName("Should fail validation when last name is blank or null")
        void shouldFailValidationWhenLastNameIsBlankOrNull(String lastName) {
            UpdateTrainerRequest request = createValidRequest();
            request.setLastName(lastName);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid last name")
        void shouldPassValidationWithValidLastName() {
            UpdateTrainerRequest request = createValidRequest();
            request.setLastName("Johnson");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with compound last name")
        void shouldPassValidationWithCompoundLastName() {
            UpdateTrainerRequest request = createValidRequest();
            request.setLastName("Van Der Berg");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with hyphenated last name")
        void shouldPassValidationWithHyphenatedLastName() {
            UpdateTrainerRequest request = createValidRequest();
            request.setLastName("Garcia-Lopez");

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsActive Validation Tests")
    class IsActiveValidationTests {

        @Test
        @DisplayName("Should fail validation when isActive is null")
        void shouldFailValidationWhenIsActiveIsNull() {
            UpdateTrainerRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("IsActive status is required");
        }

        @Test
        @DisplayName("Should pass validation when isActive is true")
        void shouldPassValidationWhenIsActiveIsTrue() {
            UpdateTrainerRequest request = createValidRequest();
            request.setIsActive(true);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when isActive is false")
        void shouldPassValidationWhenIsActiveIsFalse() {
            UpdateTrainerRequest request = createValidRequest();
            request.setIsActive(false);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle Boolean wrapper correctly")
        void shouldHandleBooleanWrapperCorrectly() {
            UpdateTrainerRequest request = createValidRequest();

            request.setIsActive(Boolean.TRUE);
            assertThat(request.getIsActive()).isTrue();

            request.setIsActive(Boolean.FALSE);
            assertThat(request.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username(null)
                    .password(null)
                    .firstName(null)
                    .lastName(null)
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Username is required",
                            "Password is required",
                            "First name is required",
                            "Last name is required",
                            "IsActive status is required"
                    );
        }

        @Test
        @DisplayName("Should return all violations when string fields are empty")
        void shouldReturnAllViolationsWhenStringFieldsAreEmpty() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("")
                    .password("")
                    .firstName("")
                    .lastName("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
        }

        @Test
        @DisplayName("Should return all violations when string fields are blank")
        void shouldReturnAllViolationsWhenStringFieldsAreBlank() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("   ")
                    .password("   ")
                    .firstName("   ")
                    .lastName("   ")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("valid.user")
                    .password("validPass")
                    .firstName(null)
                    .lastName("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "First name is required",
                            "Last name is required",
                            "IsActive status is required"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("bob.wilson")
                    .password("pass123")
                    .firstName("Bob")
                    .lastName("Wilson")
                    .isActive(true)
                    .build();

            assertThat(request.getUsername()).isEqualTo("bob.wilson");
            assertThat(request.getPassword()).isEqualTo("pass123");
            assertThat(request.getFirstName()).isEqualTo("Bob");
            assertThat(request.getLastName()).isEqualTo("Wilson");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            UpdateTrainerRequest request = new UpdateTrainerRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getPassword()).isNull();
            assertThat(request.getFirstName()).isNull();
            assertThat(request.getLastName()).isNull();
            assertThat(request.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            UpdateTrainerRequest request = new UpdateTrainerRequest(
                    "charlie.brown",
                    "pass456",
                    "Charlie",
                    "Brown",
                    false
            );

            assertThat(request.getUsername()).isEqualTo("charlie.brown");
            assertThat(request.getPassword()).isEqualTo("pass456");
            assertThat(request.getFirstName()).isEqualTo("Charlie");
            assertThat(request.getLastName()).isEqualTo("Brown");
            assertThat(request.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            UpdateTrainerRequest request = new UpdateTrainerRequest();

            request.setUsername("diana.prince");
            request.setPassword("pass789");
            request.setFirstName("Diana");
            request.setLastName("Prince");
            request.setIsActive(true);

            assertThat(request.getUsername()).isEqualTo("diana.prince");
            assertThat(request.getPassword()).isEqualTo("pass789");
            assertThat(request.getFirstName()).isEqualTo("Diana");
            assertThat(request.getLastName()).isEqualTo("Prince");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            UpdateTrainerRequest request1 = createValidRequest();
            UpdateTrainerRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different isActive values")
        void shouldHaveCorrectEqualsForDifferentIsActiveValues() {
            UpdateTrainerRequest request1 = createValidRequest();
            request1.setIsActive(true);

            UpdateTrainerRequest request2 = createValidRequest();
            request2.setIsActive(false);

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should have correct equals for different names")
        void shouldHaveCorrectEqualsForDifferentNames() {
            UpdateTrainerRequest request1 = createValidRequest();
            UpdateTrainerRequest request2 = createValidRequest();
            request2.setFirstName("Jane");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            UpdateTrainerRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            UpdateTrainerRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            UpdateTrainerRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("UpdateTrainerRequest");
            assertThat(toString).contains("username=Alice.Smith");
            assertThat(toString).contains("firstName=Alice");
            assertThat(toString).contains("lastName=Smith");
            assertThat(toString).contains("isActive=true");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("josé.garcía")
                    .password("пароль123")
                    .firstName("José")
                    .lastName("García")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Cyrillic characters")
        void shouldPassValidationWithCyrillicCharacters() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("анна.иванова")
                    .password("password")
                    .firstName("Анна")
                    .lastName("Иванова")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Chinese characters")
        void shouldPassValidationWithChineseCharacters() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("user.name")
                    .password("password")
                    .firstName("明")
                    .lastName("王")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with mixed case names")
        void shouldPassValidationWithMixedCaseNames() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("McDonalds.User")
                    .password("password")
                    .firstName("McDonald")
                    .lastName("O'BRIEN")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with numeric username")
        void shouldPassValidationWithNumericUsername() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder()
                    .username("123456")
                    .password("password")
                    .firstName("Test")
                    .lastName("User")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            UpdateTrainerRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            UpdateTrainerRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }

        @Test
        @DisplayName("Should have correct property path for firstName violation")
        void shouldHaveCorrectPropertyPathForFirstNameViolation() {
            UpdateTrainerRequest request = createValidRequest();
            request.setFirstName(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("firstName");
        }

        @Test
        @DisplayName("Should have correct property path for lastName violation")
        void shouldHaveCorrectPropertyPathForLastNameViolation() {
            UpdateTrainerRequest request = createValidRequest();
            request.setLastName(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("lastName");
        }

        @Test
        @DisplayName("Should have correct property path for isActive violation")
        void shouldHaveCorrectPropertyPathForIsActiveViolation() {
            UpdateTrainerRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("isActive");
        }
    }

    @Nested
    @DisplayName("All Fields Required Tests")
    class AllFieldsRequiredTests {

        @Test
        @DisplayName("All fields should be required")
        void allFieldsShouldBeRequired() {
            UpdateTrainerRequest request = new UpdateTrainerRequest();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder(
                            "username",
                            "password",
                            "firstName",
                            "lastName",
                            "isActive"
                    );
        }

        @Test
        @DisplayName("Should have exactly 5 required fields")
        void shouldHaveExactlyFiveRequiredFields() {
            UpdateTrainerRequest request = UpdateTrainerRequest.builder().build();

            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Comparison with UpdateTraineeRequest Tests")
    class ComparisonWithUpdateTraineeRequestTests {

        @Test
        @DisplayName("Should have 5 fields unlike UpdateTraineeRequest which has 7")
        void shouldHaveFiveFieldsUnlikeUpdateTraineeRequest() {
            UpdateTrainerRequest request = createValidRequest();

            // UpdateTrainerRequest has: username, password, firstName, lastName, isActive
            // UpdateTraineeRequest has: username, password, firstName, lastName, dateOfBirth, address, isActive
            assertThat(request.getClass().getDeclaredFields())
                    .extracting("name")
                    .containsExactlyInAnyOrder(
                            "username",
                            "password",
                            "firstName",
                            "lastName",
                            "isActive"
                    );
        }

        @Test
        @DisplayName("Should not have optional fields like dateOfBirth and address")
        void shouldNotHaveOptionalFieldsLikeDateOfBirthAndAddress() {
            UpdateTrainerRequest request = new UpdateTrainerRequest();

            // All violations should be for required fields only
            Set<ConstraintViolation<UpdateTrainerRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .doesNotContain("dateOfBirth", "address");
        }
    }
}