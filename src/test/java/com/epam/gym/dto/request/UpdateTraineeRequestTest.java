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

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UpdateTraineeRequest Tests")
class UpdateTraineeRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UpdateTraineeRequest createValidRequest() {
        return UpdateTraineeRequest.builder()
                .username("John.Doe")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .address("456 Oak Ave, Boston")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            UpdateTraineeRequest request = createValidRequest();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with only required fields")
        void shouldPassValidationWithOnlyRequiredFields() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with null optional fields")
        void shouldPassValidationWithNullOptionalFields() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("John.Doe")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(null)
                    .address(null)
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with isActive false")
        void shouldPassValidationWithIsActiveFalse() {
            UpdateTraineeRequest request = createValidRequest();
            request.setIsActive(false);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character values")
        void shouldPassValidationWithSingleCharacterValues() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("a")
                    .password("b")
                    .firstName("c")
                    .lastName("d")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            UpdateTraineeRequest request = createValidRequest();
            request.setUsername(username);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Username is required");
        }

        @Test
        @DisplayName("Should pass validation with valid username")
        void shouldPassValidationWithValidUsername() {
            UpdateTraineeRequest request = createValidRequest();
            request.setUsername("valid.username");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with username containing special characters")
        void shouldPassValidationWithUsernameContainingSpecialCharacters() {
            UpdateTraineeRequest request = createValidRequest();
            request.setUsername("john.doe@test_123");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            UpdateTraineeRequest request = createValidRequest();
            request.setPassword(password);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password is required");
        }

        @Test
        @DisplayName("Should pass validation with valid password")
        void shouldPassValidationWithValidPassword() {
            UpdateTraineeRequest request = createValidRequest();
            request.setPassword("validPassword123");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with password containing special characters")
        void shouldPassValidationWithPasswordContainingSpecialCharacters() {
            UpdateTraineeRequest request = createValidRequest();
            request.setPassword("P@$$w0rd!#%");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            UpdateTraineeRequest request = createValidRequest();
            request.setFirstName(firstName);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid first name")
        void shouldPassValidationWithValidFirstName() {
            UpdateTraineeRequest request = createValidRequest();
            request.setFirstName("Jane");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with hyphenated first name")
        void shouldPassValidationWithHyphenatedFirstName() {
            UpdateTraineeRequest request = createValidRequest();
            request.setFirstName("Mary-Jane");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            UpdateTraineeRequest request = createValidRequest();
            request.setLastName(lastName);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid last name")
        void shouldPassValidationWithValidLastName() {
            UpdateTraineeRequest request = createValidRequest();
            request.setLastName("Smith");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with compound last name")
        void shouldPassValidationWithCompoundLastName() {
            UpdateTraineeRequest request = createValidRequest();
            request.setLastName("Van Der Berg");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Date of Birth Validation Tests")
    class DateOfBirthValidationTests {

        @Test
        @DisplayName("Should pass validation when date of birth is null")
        void shouldPassValidationWhenDateOfBirthIsNull() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when date of birth is in the past")
        void shouldPassValidationWhenDateOfBirthIsInThePast() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(1990, 5, 15));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when date of birth is yesterday")
        void shouldPassValidationWhenDateOfBirthIsYesterday() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().minusDays(1));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when date of birth is today")
        void shouldFailValidationWhenDateOfBirthIsToday() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now());

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Should fail validation when date of birth is in the future")
        void shouldFailValidationWhenDateOfBirthIsInTheFuture() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Should pass validation with very old date of birth")
        void shouldPassValidationWithVeryOldDateOfBirth() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(1900, 1, 1));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should pass validation when address is null")
        void shouldPassValidationWhenAddressIsNull() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when address is empty")
        void shouldPassValidationWhenAddressIsEmpty() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress("");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when address is blank")
        void shouldPassValidationWhenAddressIsBlank() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress("   ");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with valid address")
        void shouldPassValidationWithValidAddress() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress("123 Main St, New York, NY 10001");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with multiline address")
        void shouldPassValidationWithMultilineAddress() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress("123 Main St\nApt 4B\nNew York, NY 10001");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("IsActive Validation Tests")
    class IsActiveValidationTests {

        @Test
        @DisplayName("Should fail validation when isActive is null")
        void shouldFailValidationWhenIsActiveIsNull() {
            UpdateTraineeRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("IsActive status is required");
        }

        @Test
        @DisplayName("Should pass validation when isActive is true")
        void shouldPassValidationWhenIsActiveIsTrue() {
            UpdateTraineeRequest request = createValidRequest();
            request.setIsActive(true);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when isActive is false")
        void shouldPassValidationWhenIsActiveIsFalse() {
            UpdateTraineeRequest request = createValidRequest();
            request.setIsActive(false);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should handle Boolean wrapper correctly")
        void shouldHandleBooleanWrapperCorrectly() {
            UpdateTraineeRequest request = createValidRequest();

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
        @DisplayName("Should return all violations when all required fields are null")
        void shouldReturnAllViolationsWhenAllRequiredFieldsAreNull() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(null)
                    .password(null)
                    .firstName(null)
                    .lastName(null)
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("")
                    .password("")
                    .firstName("")
                    .lastName("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(5);
        }

        @Test
        @DisplayName("Should return violations including date validation")
        void shouldReturnViolationsIncludingDateValidation() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username(null)
                    .password("valid")
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.now().plusDays(1))
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Username is required",
                            "Date of birth must be in the past"
                    );
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("valid.user")
                    .password("validPass")
                    .firstName(null)
                    .lastName("")
                    .isActive(null)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
            LocalDate dob = LocalDate.of(1990, 6, 15);
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("alice.smith")
                    .password("pass123")
                    .firstName("Alice")
                    .lastName("Smith")
                    .dateOfBirth(dob)
                    .address("789 Pine St")
                    .isActive(true)
                    .build();

            assertThat(request.getUsername()).isEqualTo("alice.smith");
            assertThat(request.getPassword()).isEqualTo("pass123");
            assertThat(request.getFirstName()).isEqualTo("Alice");
            assertThat(request.getLastName()).isEqualTo("Smith");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("789 Pine St");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            UpdateTraineeRequest request = new UpdateTraineeRequest();

            assertThat(request.getUsername()).isNull();
            assertThat(request.getPassword()).isNull();
            assertThat(request.getFirstName()).isNull();
            assertThat(request.getLastName()).isNull();
            assertThat(request.getDateOfBirth()).isNull();
            assertThat(request.getAddress()).isNull();
            assertThat(request.getIsActive()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            LocalDate dob = LocalDate.of(1985, 12, 25);
            UpdateTraineeRequest request = new UpdateTraineeRequest(
                    "bob.wilson",
                    "pass456",
                    "Bob",
                    "Wilson",
                    dob,
                    "321 Elm St",
                    false
            );

            assertThat(request.getUsername()).isEqualTo("bob.wilson");
            assertThat(request.getPassword()).isEqualTo("pass456");
            assertThat(request.getFirstName()).isEqualTo("Bob");
            assertThat(request.getLastName()).isEqualTo("Wilson");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("321 Elm St");
            assertThat(request.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            UpdateTraineeRequest request = new UpdateTraineeRequest();
            LocalDate dob = LocalDate.of(2000, 1, 1);

            request.setUsername("charlie.brown");
            request.setPassword("pass789");
            request.setFirstName("Charlie");
            request.setLastName("Brown");
            request.setDateOfBirth(dob);
            request.setAddress("555 Maple Ave");
            request.setIsActive(true);

            assertThat(request.getUsername()).isEqualTo("charlie.brown");
            assertThat(request.getPassword()).isEqualTo("pass789");
            assertThat(request.getFirstName()).isEqualTo("Charlie");
            assertThat(request.getLastName()).isEqualTo("Brown");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("555 Maple Ave");
            assertThat(request.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            UpdateTraineeRequest request1 = createValidRequest();
            UpdateTraineeRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different objects")
        void shouldHaveCorrectEqualsForDifferentObjects() {
            UpdateTraineeRequest request1 = createValidRequest();
            UpdateTraineeRequest request2 = createValidRequest();
            request2.setFirstName("Jane");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            UpdateTraineeRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            UpdateTraineeRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            UpdateTraineeRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("UpdateTraineeRequest");
            assertThat(toString).contains("username=John.Doe");
            assertThat(toString).contains("firstName=John");
            assertThat(toString).contains("lastName=Doe");
            assertThat(toString).contains("isActive=true");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters")
        void shouldPassValidationWithUnicodeCharacters() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("josé.müller")
                    .password("пароль123")
                    .firstName("José")
                    .lastName("Müller")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Cyrillic characters")
        void shouldPassValidationWithCyrillicCharacters() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("иван.петров")
                    .password("password")
                    .firstName("Иван")
                    .lastName("Петров")
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Chinese characters")
        void shouldPassValidationWithChineseCharacters() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("user.name")
                    .password("password")
                    .firstName("明")
                    .lastName("李")
                    .isActive(false)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with leap year date of birth")
        void shouldPassValidationWithLeapYearDateOfBirth() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(2000, 2, 29));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji in address")
        void shouldPassValidationWithEmojiInAddress() {
            UpdateTraineeRequest request = createValidRequest();
            request.setAddress("123 Main St 🏠");

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long values")
        void shouldPassValidationWithLongValues() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("a".repeat(100))
                    .password("b".repeat(100))
                    .firstName("c".repeat(100))
                    .lastName("d".repeat(100))
                    .address("e".repeat(500))
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for username violation")
        void shouldHaveCorrectPropertyPathForUsernameViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setUsername(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username");
        }

        @Test
        @DisplayName("Should have correct property path for password violation")
        void shouldHaveCorrectPropertyPathForPasswordViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setPassword(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("password");
        }

        @Test
        @DisplayName("Should have correct property path for firstName violation")
        void shouldHaveCorrectPropertyPathForFirstNameViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setFirstName(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("firstName");
        }

        @Test
        @DisplayName("Should have correct property path for lastName violation")
        void shouldHaveCorrectPropertyPathForLastNameViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setLastName(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("lastName");
        }

        @Test
        @DisplayName("Should have correct property path for dateOfBirth violation")
        void shouldHaveCorrectPropertyPathForDateOfBirthViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("dateOfBirth");
        }

        @Test
        @DisplayName("Should have correct property path for isActive violation")
        void shouldHaveCorrectPropertyPathForIsActiveViolation() {
            UpdateTraineeRequest request = createValidRequest();
            request.setIsActive(null);

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("isActive");
        }
    }

    @Nested
    @DisplayName("Required vs Optional Fields Tests")
    class RequiredVsOptionalFieldsTests {

        @Test
        @DisplayName("Should have 5 required fields")
        void shouldHaveFiveRequiredFields() {
            UpdateTraineeRequest request = new UpdateTraineeRequest();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

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
        @DisplayName("Should have 2 optional fields")
        void shouldHaveTwoOptionalFields() {
            UpdateTraineeRequest request = UpdateTraineeRequest.builder()
                    .username("user")
                    .password("pass")
                    .firstName("First")
                    .lastName("Last")
                    .dateOfBirth(null)  // optional
                    .address(null)       // optional
                    .isActive(true)
                    .build();

            Set<ConstraintViolation<UpdateTraineeRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}