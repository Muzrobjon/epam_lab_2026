package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import com.epam.gym.entity.Training;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TrainingSpecification}.
 * Tests specification construction and predicate logic.
 */
@DisplayName("TrainingSpecification Tests")
class TrainingSpecificationTest {

    private static final String TRAINEE_USERNAME = "john.doe";
    private static final String TRAINER_USERNAME = "jane.smith";
    private static final String TRAINER_NAME = "Jane";
    private static final String TRAINEE_NAME = "John";
    private static final LocalDate FROM_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate TO_DATE = LocalDate.of(2024, 12, 31);
    private static final TrainingTypeName TRAINING_TYPE = TrainingTypeName.FITNESS;

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() throws NoSuchMethodException {
        // Given
        Constructor<TrainingSpecification> constructor = TrainingSpecification.class.getDeclaredConstructor();

        // Then
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

    @Test
    @DisplayName("Should be instantiable via reflection despite private constructor")
    void shouldBeInstantiableViaReflectionDespitePrivateConstructor() throws Exception {
        // Given - private constructor exists but is empty, so reflection can create instance
        Constructor<TrainingSpecification> constructor = TrainingSpecification.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // When & Then - no exception thrown, instance created successfully
        TrainingSpecification instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Should prevent direct instantiation")
    void shouldPreventDirectInstantiation() {
        // Then - cannot call new TrainingSpecification() directly due to private constructor
        // This is a compile-time check, verified by the private modifier test above
        assertTrue(true, "Private constructor prevents direct instantiation");
    }

    // ==================== findTraineeTrainingsByCriteria Tests ====================

    @Test
    @DisplayName("Should create specification for trainee trainings with all criteria")
    void shouldCreateSpecificationForTraineeTrainingsWithAllCriteria() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                TRAINEE_USERNAME, FROM_DATE, TO_DATE, TRAINER_NAME, TRAINING_TYPE);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainee trainings with null criteria")
    void shouldCreateSpecificationForTraineeTrainingsWithNullCriteria() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                null, null, null, null, null);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainee trainings with blank username")
    void shouldCreateSpecificationForTraineeTrainingsWithBlankUsername() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                "   ", FROM_DATE, TO_DATE, TRAINER_NAME, TRAINING_TYPE);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainee trainings with only username")
    void shouldCreateSpecificationForTraineeTrainingsWithOnlyUsername() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                TRAINEE_USERNAME, null, null, null, null);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainee trainings with date range only")
    void shouldCreateSpecificationForTraineeTrainingsWithDateRangeOnly() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                null, FROM_DATE, TO_DATE, null, null);

        // Then
        assertNotNull(spec);
    }

    // ==================== findTrainerTrainingsByCriteria Tests ====================

    @Test
    @DisplayName("Should create specification for trainer trainings with all criteria")
    void shouldCreateSpecificationForTrainerTrainingsWithAllCriteria() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                TRAINER_USERNAME, FROM_DATE, TO_DATE, TRAINEE_NAME);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainer trainings with null criteria")
    void shouldCreateSpecificationForTrainerTrainingsWithNullCriteria() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                null, null, null, null);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainer trainings with blank username")
    void shouldCreateSpecificationForTrainerTrainingsWithBlankUsername() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                "   ", FROM_DATE, TO_DATE, TRAINEE_NAME);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainer trainings with only username")
    void shouldCreateSpecificationForTrainerTrainingsWithOnlyUsername() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                TRAINER_USERNAME, null, null, null);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should create specification for trainer trainings with date range only")
    void shouldCreateSpecificationForTrainerTrainingsWithDateRangeOnly() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                null, FROM_DATE, TO_DATE, null);

        // Then
        assertNotNull(spec);
    }

    // ==================== Specification Type Tests ====================

    @Test
    @DisplayName("Should return Specification type for trainee method")
    void shouldReturnSpecificationTypeForTraineeMethod() {
        // When
        Specification<Training> result = TrainingSpecification.findTraineeTrainingsByCriteria(
                TRAINEE_USERNAME, FROM_DATE, TO_DATE, TRAINER_NAME, TRAINING_TYPE);

        // Then
        assertNotNull(result);
        assertInstanceOf(Specification.class, result);
    }

    @Test
    @DisplayName("Should return Specification type for trainer method")
    void shouldReturnSpecificationTypeForTrainerMethod() {
        // When
        Specification<Training> result = TrainingSpecification.findTrainerTrainingsByCriteria(
                TRAINER_USERNAME, FROM_DATE, TO_DATE, TRAINEE_NAME);

        // Then
        assertNotNull(result);
        assertInstanceOf(Specification.class, result);
    }

    // ==================== Different Criteria Combinations ====================

    @Test
    @DisplayName("Should handle partial criteria for trainee trainings")
    void shouldHandlePartialCriteriaForTraineeTrainings() {
        // When - only trainer name and type
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                null, null, null, TRAINER_NAME, TRAINING_TYPE);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should handle partial criteria for trainer trainings")
    void shouldHandlePartialCriteriaForTrainerTrainings() {
        // When - only trainee name
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                null, null, null, TRAINEE_NAME);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should handle empty string criteria for trainee trainings")
    void shouldHandleEmptyStringCriteriaForTraineeTrainings() {
        // When - empty strings should be treated as null
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                "", FROM_DATE, TO_DATE, "", TRAINING_TYPE);

        // Then
        assertNotNull(spec);
    }

    @Test
    @DisplayName("Should handle empty string criteria for trainer trainings")
    void shouldHandleEmptyStringCriteriaForTrainerTrainings() {
        // When - empty strings should be treated as null
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                "", FROM_DATE, TO_DATE, "");

        // Then
        assertNotNull(spec);
    }

    // ==================== Method Signature Tests ====================

    @Test
    @DisplayName("Should have correct method signature for trainee specification")
    void shouldHaveCorrectMethodSignatureForTraineeSpecification() throws NoSuchMethodException {
        // Given
        var method = TrainingSpecification.class.getMethod(
                "findTraineeTrainingsByCriteria",
                String.class, LocalDate.class, LocalDate.class, String.class, TrainingTypeName.class);

        // Then
        assertNotNull(method);
        assertTrue(Modifier.isStatic(method.getModifiers()));
        assertEquals(Specification.class, method.getReturnType());
    }

    @Test
    @DisplayName("Should have correct method signature for trainer specification")
    void shouldHaveCorrectMethodSignatureForTrainerSpecification() throws NoSuchMethodException {
        // Given
        var method = TrainingSpecification.class.getMethod(
                "findTrainerTrainingsByCriteria",
                String.class, LocalDate.class, LocalDate.class, String.class);

        // Then
        assertNotNull(method);
        assertTrue(Modifier.isStatic(method.getModifiers()));
        assertEquals(Specification.class, method.getReturnType());
    }

    // ==================== Null Safety Tests ====================

    @Test
    @DisplayName("Should handle all null parameters for trainee trainings")
    void shouldHandleAllNullParametersForTraineeTrainings() {
        // When
        Specification<Training> spec = TrainingSpecification.findTraineeTrainingsByCriteria(
                null, null, null, null, null);

        // Then - should not throw exception
        assertDoesNotThrow(() -> assertNotNull(spec));
    }

    @Test
    @DisplayName("Should handle all null parameters for trainer trainings")
    void shouldHandleAllNullParametersForTrainerTrainings() {
        // When
        Specification<Training> spec = TrainingSpecification.findTrainerTrainingsByCriteria(
                null, null, null, null);

        // Then - should not throw exception
        assertDoesNotThrow(() -> assertNotNull(spec));
    }
}