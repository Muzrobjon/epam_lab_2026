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

@DisplayName("TraineeRegistrationRequest Tests")
class TraineeRegistrationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TraineeRegistrationRequest createValidRequest() {
        return TraineeRegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1995, 3, 20))
                .address("123 Main St, New York")
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            TraineeRegistrationRequest request = createValidRequest();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with only required fields")
        void shouldPassValidationWithOnlyRequiredFields() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with null optional fields")
        void shouldPassValidationWithNullOptionalFields() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(null)
                    .address(null)
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character names")
        void shouldPassValidationWithSingleCharacterNames() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("J")
                    .lastName("D")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long names")
        void shouldPassValidationWithLongNames() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("A".repeat(100))
                    .lastName("B".repeat(100))
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("First Name Validation Tests")
    class FirstNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when first name is blank or null")
        void shouldFailValidationWhenFirstNameIsBlankOrNull(String firstName) {
            TraineeRegistrationRequest request = createValidRequest();
            request.setFirstName(firstName);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid first name")
        void shouldPassValidationWithValidFirstName() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setFirstName("Jane");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with first name containing special characters")
        void shouldPassValidationWithFirstNameContainingSpecialCharacters() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setFirstName("Jean-Pierre");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with first name containing apostrophe")
        void shouldPassValidationWithFirstNameContainingApostrophe() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setFirstName("O'Connor");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Last Name Validation Tests")
    class LastNameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n", "\r", "  \t\n  "})
        @DisplayName("Should fail validation when last name is blank or null")
        void shouldFailValidationWhenLastNameIsBlankOrNull(String lastName) {
            TraineeRegistrationRequest request = createValidRequest();
            request.setLastName(lastName);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid last name")
        void shouldPassValidationWithValidLastName() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setLastName("Smith");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with compound last name")
        void shouldPassValidationWithCompoundLastName() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setLastName("Van Der Berg");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Date of Birth Validation Tests")
    class DateOfBirthValidationTests {

        @Test
        @DisplayName("Should pass validation when date of birth is null")
        void shouldPassValidationWhenDateOfBirthIsNull() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(null);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when date of birth is in the past")
        void shouldPassValidationWhenDateOfBirthIsInThePast() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(1990, 5, 15));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when date of birth is yesterday")
        void shouldPassValidationWhenDateOfBirthIsYesterday() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().minusDays(1));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when date of birth is today")
        void shouldFailValidationWhenDateOfBirthIsToday() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now());

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Should fail validation when date of birth is in the future")
        void shouldFailValidationWhenDateOfBirthIsInTheFuture() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Should fail validation when date of birth is far in the future")
        void shouldFailValidationWhenDateOfBirthIsFarInTheFuture() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(2099, 12, 31));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Date of birth must be in the past");
        }

        @Test
        @DisplayName("Should pass validation with very old date of birth")
        void shouldPassValidationWithVeryOldDateOfBirth() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.of(1900, 1, 1));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should pass validation when address is null")
        void shouldPassValidationWhenAddressIsNull() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setAddress(null);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when address is empty")
        void shouldPassValidationWhenAddressIsEmpty() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setAddress("");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation when address is blank")
        void shouldPassValidationWhenAddressIsBlank() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setAddress("   ");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with valid address")
        void shouldPassValidationWithValidAddress() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setAddress("456 Oak Avenue, Los Angeles, CA 90001");

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long address")
        void shouldPassValidationWithLongAddress() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setAddress("A".repeat(500));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when required fields are null")
        void shouldReturnAllViolationsWhenRequiredFieldsAreNull() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(null)
                    .lastName(null)
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "First name is required",
                            "Last name is required"
                    );
        }

        @Test
        @DisplayName("Should return all violations when required fields are empty")
        void shouldReturnAllViolationsWhenRequiredFieldsAreEmpty() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("")
                    .lastName("")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
        }

        @Test
        @DisplayName("Should return violations for required fields and date validation")
        void shouldReturnViolationsForRequiredFieldsAndDateValidation() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName(null)
                    .lastName("")
                    .dateOfBirth(LocalDate.now().plusDays(1))
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "First name is required",
                            "Last name is required",
                            "Date of birth must be in the past"
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
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("Alice")
                    .lastName("Johnson")
                    .dateOfBirth(dob)
                    .address("789 Pine St")
                    .build();

            assertThat(request.getFirstName()).isEqualTo("Alice");
            assertThat(request.getLastName()).isEqualTo("Johnson");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("789 Pine St");
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();

            assertThat(request.getFirstName()).isNull();
            assertThat(request.getLastName()).isNull();
            assertThat(request.getDateOfBirth()).isNull();
            assertThat(request.getAddress()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            LocalDate dob = LocalDate.of(1985, 12, 25);
            TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                    "Bob",
                    "Williams",
                    dob,
                    "321 Elm St"
            );

            assertThat(request.getFirstName()).isEqualTo("Bob");
            assertThat(request.getLastName()).isEqualTo("Williams");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("321 Elm St");
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            TraineeRegistrationRequest request = new TraineeRegistrationRequest();
            LocalDate dob = LocalDate.of(2000, 1, 1);

            request.setFirstName("Charlie");
            request.setLastName("Brown");
            request.setDateOfBirth(dob);
            request.setAddress("555 Maple Ave");

            assertThat(request.getFirstName()).isEqualTo("Charlie");
            assertThat(request.getLastName()).isEqualTo("Brown");
            assertThat(request.getDateOfBirth()).isEqualTo(dob);
            assertThat(request.getAddress()).isEqualTo("555 Maple Ave");
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            TraineeRegistrationRequest request1 = createValidRequest();
            TraineeRegistrationRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different objects")
        void shouldHaveCorrectEqualsForDifferentObjects() {
            TraineeRegistrationRequest request1 = createValidRequest();
            TraineeRegistrationRequest request2 = createValidRequest();
            request2.setFirstName("Jane");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TraineeRegistrationRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TraineeRegistrationRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TraineeRegistrationRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("TraineeRegistrationRequest");
            assertThat(toString).contains("firstName=John");
            assertThat(toString).contains("lastName=Doe");
            assertThat(toString).contains("dateOfBirth=1995-03-20");
            assertThat(toString).contains("address=123 Main St, New York");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters in names")
        void shouldPassValidationWithUnicodeCharactersInNames() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("José")
                    .lastName("Müller")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Cyrillic characters")
        void shouldPassValidationWithCyrillicCharacters() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("Иван")
                    .lastName("Петров")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Chinese characters")
        void shouldPassValidationWithChineseCharacters() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("明")
                    .lastName("李")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with emoji in address")
        void shouldPassValidationWithEmojiInAddress() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .address("123 Main St 🏠")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with leap year date of birth")
        void shouldPassValidationWithLeapYearDateOfBirth() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(2000, 2, 29))
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with multiline address")
        void shouldPassValidationWithMultilineAddress() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .address("123 Main St\nApt 4B\nNew York, NY 10001")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for firstName violation")
        void shouldHaveCorrectPropertyPathForFirstNameViolation() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setFirstName(null);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("firstName");
        }

        @Test
        @DisplayName("Should have correct property path for lastName violation")
        void shouldHaveCorrectPropertyPathForLastNameViolation() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setLastName(null);

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("lastName");
        }

        @Test
        @DisplayName("Should have correct property path for dateOfBirth violation")
        void shouldHaveCorrectPropertyPathForDateOfBirthViolation() {
            TraineeRegistrationRequest request = createValidRequest();
            request.setDateOfBirth(LocalDate.now().plusDays(1));

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("dateOfBirth");
        }
    }

    @Nested
    @DisplayName("Optional vs Required Fields Tests")
    class OptionalVsRequiredFieldsTests {

        @Test
        @DisplayName("Should identify firstName as required")
        void shouldIdentifyFirstNameAsRequired() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .lastName("Doe")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is required");
        }

        @Test
        @DisplayName("Should identify lastName as required")
        void shouldIdentifyLastNameAsRequired() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is required");
        }

        @Test
        @DisplayName("Should identify dateOfBirth as optional")
        void shouldIdentifyDateOfBirthAsOptional() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(null)
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should identify address as optional")
        void shouldIdentifyAddressAsOptional() {
            TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .address(null)
                    .build();

            Set<ConstraintViolation<TraineeRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }
}