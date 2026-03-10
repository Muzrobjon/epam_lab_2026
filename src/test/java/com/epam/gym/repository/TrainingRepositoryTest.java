package com.epam.gym.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple verification tests for TrainingRepository interface.
 * No Spring context required - pure interface testing.
 */
@DisplayName("TrainingRepository Interface Tests")
class TrainingRepositoryTest {

    @Test
    @DisplayName("Should extend JpaRepository")
    void shouldExtendJpaRepository() {
        // Given
        Class<?> repoClass = TrainingRepository.class;

        // Then
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(repoClass));
    }

    @Test
    @DisplayName("Should be annotated with Repository")
    void shouldBeAnnotatedWithRepository() {
        // Given
        Class<?> repoClass = TrainingRepository.class;

        // Then
        assertTrue(repoClass.isAnnotationPresent(org.springframework.stereotype.Repository.class));
    }

    @Test
    @DisplayName("Should have findTrainingsWithAllUsers method")
    void shouldHaveFindTrainingsWithAllUsersMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;

        // When
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // Then
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());

        // Verify EntityGraph annotation
        assertTrue(method.isAnnotationPresent(org.springframework.data.jpa.repository.EntityGraph.class));

        // Verify Query annotation
        assertTrue(method.isAnnotationPresent(org.springframework.data.jpa.repository.Query.class));
    }

    @Test
    @DisplayName("Should verify EntityGraph attribute paths for eager fetching")
    void shouldVerifyEntityGraphAttributePathsForEagerFetching() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        org.springframework.data.jpa.repository.EntityGraph entityGraph =
                method.getAnnotation(org.springframework.data.jpa.repository.EntityGraph.class);

        // Then
        assertNotNull(entityGraph);
        String[] attributePaths = entityGraph.attributePaths();

        // Verify both trainee.user and trainer.user are in attribute paths
        assertEquals(2, attributePaths.length);
        assertTrue(
                (attributePaths[0].equals("trainee.user") && attributePaths[1].equals("trainer.user")) ||
                        (attributePaths[0].equals("trainer.user") && attributePaths[1].equals("trainee.user")),
                "EntityGraph should include both trainee.user and trainer.user"
        );
    }

    @Test
    @DisplayName("Should verify Query value contains correct JPQL with optional parameters")
    void shouldVerifyQueryValueContainsCorrectJpql() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        org.springframework.data.jpa.repository.Query queryAnnotation =
                method.getAnnotation(org.springframework.data.jpa.repository.Query.class);

        // Then
        assertNotNull(queryAnnotation);
        String query = queryAnnotation.value();

        // Verify key parts of the query
        assertTrue(query.contains("SELECT t FROM Training t"));
        assertTrue(query.contains(":traineeUsername IS NULL OR t.trainee.user.username = :traineeUsername"));
        assertTrue(query.contains(":trainerUsername IS NULL OR t.trainer.user.username = :trainerUsername"));
        assertTrue(query.contains("t.trainingDate >= COALESCE(:fromDate, t.trainingDate)"));
        assertTrue(query.contains("t.trainingDate <= COALESCE(:toDate, t.trainingDate)"));
    }

    @Test
    @DisplayName("Should verify all parameters have Param annotations")
    void shouldVerifyAllParametersHaveParamAnnotations() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        java.lang.reflect.Parameter[] parameters = method.getParameters();

        // Then
        assertEquals(4, parameters.length);

        // Verify each parameter has @Param annotation
        String[] expectedNames = {"traineeUsername", "trainerUsername", "fromDate", "toDate"};
        for (int i = 0; i < parameters.length; i++) {
            assertTrue(parameters[i].isAnnotationPresent(org.springframework.data.repository.query.Param.class),
                    "Parameter " + i + " should have @Param annotation");

            org.springframework.data.repository.query.Param paramAnnotation =
                    parameters[i].getAnnotation(org.springframework.data.repository.query.Param.class);
            assertEquals(expectedNames[i], paramAnnotation.value(),
                    "Parameter " + i + " should have correct @Param value");
        }
    }

    @Test
    @DisplayName("Should verify return type is List of Training")
    void shouldVerifyReturnTypeIsListOfTraining() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        Class<?> returnType = method.getReturnType();

        // Then
        assertEquals(List.class, returnType);
    }

    @Test
    @DisplayName("Should verify generic type of JpaRepository is Training and Long")
    void shouldVerifyGenericTypeOfJpaRepository() {
        // Given
        Class<?> repoClass = TrainingRepository.class;

        // When - get generic interfaces
        java.lang.reflect.Type[] genericInterfaces = repoClass.getGenericInterfaces();

        // Then - find JpaRepository interface
        boolean foundJpaRepository = false;
        for (java.lang.reflect.Type type : genericInterfaces) {
            if (type.getTypeName().contains("JpaRepository")) {
                foundJpaRepository = true;
                assertTrue(type.getTypeName().contains("Training"));
                assertTrue(type.getTypeName().contains("Long"));
                break;
            }
        }
        assertTrue(foundJpaRepository, "Should implement JpaRepository<Training, Long>");
    }

    @Test
    @DisplayName("Should inherit standard CrudRepository methods")
    void shouldInheritStandardCrudRepositoryMethods() {
        // Given - verify methods exist in parent interfaces

        // Then - verify inherited methods from CrudRepository
        assertDoesNotThrow(() -> {
            Method findById = org.springframework.data.repository.CrudRepository.class
                    .getMethod("findById", Object.class);
            assertNotNull(findById);
        });

        assertDoesNotThrow(() -> {
            Method save = org.springframework.data.repository.CrudRepository.class
                    .getMethod("save", Object.class);
            assertNotNull(save);
        });

        assertDoesNotThrow(() -> {
            Method findAll = org.springframework.data.repository.CrudRepository.class
                    .getMethod("findAll");
            assertNotNull(findAll);
        });

        assertDoesNotThrow(() -> {
            Method deleteById = org.springframework.data.repository.CrudRepository.class
                    .getMethod("deleteById", Object.class);
            assertNotNull(deleteById);
        });
    }

    @Test
    @DisplayName("Should inherit JpaRepository specific methods")
    void shouldInheritJpaRepositorySpecificMethods() {
        // Then - verify JpaRepository specific methods
        assertDoesNotThrow(() -> {
            Method findAll = org.springframework.data.jpa.repository.JpaRepository.class
                    .getMethod("findAll");
            assertNotNull(findAll);
        });

        assertDoesNotThrow(() -> {
            Method saveAndFlush = org.springframework.data.jpa.repository.JpaRepository.class
                    .getMethod("saveAndFlush", Object.class);
            assertNotNull(saveAndFlush);
        });
    }

    @Test
    @DisplayName("Should verify query handles null parameters with COALESCE")
    void shouldVerifyQueryHandlesNullParametersWithCoalesce() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        org.springframework.data.jpa.repository.Query queryAnnotation =
                method.getAnnotation(org.springframework.data.jpa.repository.Query.class);

        // Then
        assertNotNull(queryAnnotation);
        String query = queryAnnotation.value();

        // Verify COALESCE is used for date parameters
        assertTrue(query.contains("COALESCE(:fromDate, t.trainingDate)"));
        assertTrue(query.contains("COALESCE(:toDate, t.trainingDate)"));

        // Verify IS NULL check for username parameters
        assertTrue(query.contains(":traineeUsername IS NULL"));
        assertTrue(query.contains(":trainerUsername IS NULL"));
    }

    @Test
    @DisplayName("Should verify method signature has correct parameter types")
    void shouldVerifyMethodSignatureHasCorrectParameterTypes() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingRepository.class;
        Method method = repoClass.getMethod("findTrainingsWithAllUsers",
                String.class, String.class, LocalDate.class, LocalDate.class);

        // When
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Then
        assertEquals(4, parameterTypes.length);
        assertEquals(String.class, parameterTypes[0]);
        assertEquals(String.class, parameterTypes[1]);
        assertEquals(LocalDate.class, parameterTypes[2]);
        assertEquals(LocalDate.class, parameterTypes[3]);
    }
}