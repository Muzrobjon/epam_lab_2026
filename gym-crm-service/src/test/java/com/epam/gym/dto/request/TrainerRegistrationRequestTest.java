package com.epam.gym.dto.request;

import com.epam.gym.enums.TrainingTypeName;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TrainerRegistrationRequest Tests")
class TrainerRegistrationRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private TrainerRegistrationRequest createValidRequest() {
        return TrainerRegistrationRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .specialization(TrainingTypeName.values()[0]) // Use first available enum value
                .build();
    }

    @Nested
    @DisplayName("Valid Request Tests")
    class ValidRequestTests {

        @Test
        @DisplayName("Should pass validation with all valid fields")
        void shouldPassValidationWithAllValidFields() {
            TrainerRegistrationRequest request = createValidRequest();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(TrainingTypeName.class)
        @DisplayName("Should pass validation with all specialization types")
        void shouldPassValidationWithAllSpecializationTypes(TrainingTypeName specialization) {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(specialization)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with single character names")
        void shouldPassValidationWithSingleCharacterNames() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("A")
                    .lastName("S")
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with long names")
        void shouldPassValidationWithLongNames() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("A".repeat(100))
                    .lastName("B".repeat(100))
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

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
            TrainerRegistrationRequest request = createValidRequest();
            request.setFirstName(firstName);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("First name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid first name")
        void shouldPassValidationWithValidFirstName() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setFirstName("Jane");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with first name containing hyphen")
        void shouldPassValidationWithFirstNameContainingHyphen() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setFirstName("Mary-Jane");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with first name containing apostrophe")
        void shouldPassValidationWithFirstNameContainingApostrophe() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setFirstName("O'Brien");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

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
            TrainerRegistrationRequest request = createValidRequest();
            request.setLastName(lastName);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Last name is required");
        }

        @Test
        @DisplayName("Should pass validation with valid last name")
        void shouldPassValidationWithValidLastName() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setLastName("Johnson");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with compound last name")
        void shouldPassValidationWithCompoundLastName() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setLastName("Van Der Berg");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with hyphenated last name")
        void shouldPassValidationWithHyphenatedLastName() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setLastName("Garcia-Lopez");

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Specialization Validation Tests")
    class SpecializationValidationTests {

        @Test
        @DisplayName("Should fail validation when specialization is null")
        void shouldFailValidationWhenSpecializationIsNull() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setSpecialization(null);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Specialization is required");
        }

        @ParameterizedTest
        @EnumSource(TrainingTypeName.class)
        @DisplayName("Should pass validation with each specialization type")
        void shouldPassValidationWithEachSpecializationType(TrainingTypeName specialization) {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Alice")
                    .lastName("Smith")
                    .specialization(specialization)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
            assertThat(request.getSpecialization()).isEqualTo(specialization);
        }
    }

    @Nested
    @DisplayName("Multiple Violations Tests")
    class MultipleViolationsTests {

        @Test
        @DisplayName("Should return all violations when all fields are null")
        void shouldReturnAllViolationsWhenAllFieldsAreNull() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName(null)
                    .lastName(null)
                    .specialization(null)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "First name is required",
                            "Last name is required",
                            "Specialization is required"
                    );
        }

        @Test
        @DisplayName("Should return all violations when string fields are empty")
        void shouldReturnAllViolationsWhenStringFieldsAreEmpty() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("")
                    .lastName("")
                    .specialization(null)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return all violations when string fields are blank")
        void shouldReturnAllViolationsWhenStringFieldsAreBlank() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("   ")
                    .lastName("   ")
                    .specialization(null)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should return correct violations for partially invalid request")
        void shouldReturnCorrectViolationsForPartiallyInvalidRequest() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Alice")
                    .lastName(null)
                    .specialization(null)
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactlyInAnyOrder(
                            "Last name is required",
                            "Specialization is required"
                    );
        }
    }

    @Nested
    @DisplayName("Builder and Lombok Tests")
    class BuilderAndLombokTests {

        @Test
        @DisplayName("Should create request using builder")
        void shouldCreateRequestUsingBuilder() {
            TrainingTypeName specialization = TrainingTypeName.values()[0];
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Bob")
                    .lastName("Wilson")
                    .specialization(specialization)
                    .build();

            assertThat(request.getFirstName()).isEqualTo("Bob");
            assertThat(request.getLastName()).isEqualTo("Wilson");
            assertThat(request.getSpecialization()).isEqualTo(specialization);
        }

        @Test
        @DisplayName("Should create request using no-args constructor")
        void shouldCreateRequestUsingNoArgsConstructor() {
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();

            assertThat(request.getFirstName()).isNull();
            assertThat(request.getLastName()).isNull();
            assertThat(request.getSpecialization()).isNull();
        }

        @Test
        @DisplayName("Should create request using all-args constructor")
        void shouldCreateRequestUsingAllArgsConstructor() {
            TrainingTypeName specialization = TrainingTypeName.values()[0];
            TrainerRegistrationRequest request = new TrainerRegistrationRequest(
                    "Charlie",
                    "Brown",
                    specialization
            );

            assertThat(request.getFirstName()).isEqualTo("Charlie");
            assertThat(request.getLastName()).isEqualTo("Brown");
            assertThat(request.getSpecialization()).isEqualTo(specialization);
        }

        @Test
        @DisplayName("Should set and get all fields using setters and getters")
        void shouldSetAndGetAllFieldsUsingSettersAndGetters() {
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();
            TrainingTypeName specialization = TrainingTypeName.values()[0];

            request.setFirstName("Diana");
            request.setLastName("Prince");
            request.setSpecialization(specialization);

            assertThat(request.getFirstName()).isEqualTo("Diana");
            assertThat(request.getLastName()).isEqualTo("Prince");
            assertThat(request.getSpecialization()).isEqualTo(specialization);
        }

        @Test
        @DisplayName("Should have correct equals implementation")
        void shouldHaveCorrectEqualsImplementation() {
            TrainerRegistrationRequest request1 = createValidRequest();
            TrainerRegistrationRequest request2 = createValidRequest();

            assertThat(request1).isEqualTo(request2);
            assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        }

        @Test
        @DisplayName("Should have correct equals for different names")
        void shouldHaveCorrectEqualsForDifferentNames() {
            TrainerRegistrationRequest request1 = createValidRequest();
            TrainerRegistrationRequest request2 = createValidRequest();
            request2.setFirstName("Jane");

            assertThat(request1).isNotEqualTo(request2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            TrainerRegistrationRequest request = createValidRequest();

            assertThat(request).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            TrainerRegistrationRequest request = createValidRequest();

            assertThat(request).isEqualTo(request);
        }

        @Test
        @DisplayName("Should have correct toString implementation")
        void shouldHaveCorrectToStringImplementation() {
            TrainerRegistrationRequest request = createValidRequest();

            String toString = request.toString();

            assertThat(toString).contains("TrainerRegistrationRequest");
            assertThat(toString).contains("firstName=Alice");
            assertThat(toString).contains("lastName=Smith");
            assertThat(toString).contains("specialization=");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should pass validation with unicode characters in names")
        void shouldPassValidationWithUnicodeCharactersInNames() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("José")
                    .lastName("García")
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Cyrillic characters")
        void shouldPassValidationWithCyrillicCharacters() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("Анна")
                    .lastName("Иванова")
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with Chinese characters")
        void shouldPassValidationWithChineseCharacters() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("明")
                    .lastName("王")
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with mixed case names")
        void shouldPassValidationWithMixedCaseNames() {
            TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                    .firstName("McDonald")
                    .lastName("O'BRIEN")
                    .specialization(TrainingTypeName.values()[0])
                    .build();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Property Path Tests")
    class PropertyPathTests {

        @Test
        @DisplayName("Should have correct property path for firstName violation")
        void shouldHaveCorrectPropertyPathForFirstNameViolation() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setFirstName(null);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("firstName");
        }

        @Test
        @DisplayName("Should have correct property path for lastName violation")
        void shouldHaveCorrectPropertyPathForLastNameViolation() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setLastName(null);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("lastName");
        }

        @Test
        @DisplayName("Should have correct property path for specialization violation")
        void shouldHaveCorrectPropertyPathForSpecializationViolation() {
            TrainerRegistrationRequest request = createValidRequest();
            request.setSpecialization(null);

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("specialization");
        }
    }

    @Nested
    @DisplayName("All Fields Required Tests")
    class AllFieldsRequiredTests {

        @Test
        @DisplayName("All fields should be required")
        void allFieldsShouldBeRequired() {
            TrainerRegistrationRequest request = new TrainerRegistrationRequest();

            Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

            assertThat(violations).hasSize(3);
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("firstName", "lastName", "specialization");
        }
    }

    @Nested
    @DisplayName("Enum Type Safety Tests")
    class EnumTypeSafetyTests {

        @Test
        @DisplayName("Should only accept valid TrainingTypeName enum values")
        void shouldOnlyAcceptValidTrainingTypeNameEnumValues() {
            for (TrainingTypeName type : TrainingTypeName.values()) {
                TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                        .firstName("Test")
                        .lastName("Trainer")
                        .specialization(type)
                        .build();

                Set<ConstraintViolation<TrainerRegistrationRequest>> violations = validator.validate(request);

                assertThat(violations).isEmpty();
            }
        }

        @Test
        @DisplayName("Should store and retrieve specialization correctly")
        void shouldStoreAndRetrieveSpecializationCorrectly() {
            TrainerRegistrationRequest request = createValidRequest();

            for (TrainingTypeName type : TrainingTypeName.values()) {
                request.setSpecialization(type);
                assertThat(request.getSpecialization()).isEqualTo(type);
            }
        }

        @Test
        @DisplayName("Should have at least one enum value available")
        void shouldHaveAtLeastOneEnumValueAvailable() {
            assertThat(TrainingTypeName.values()).isNotEmpty();
        }
    }
}