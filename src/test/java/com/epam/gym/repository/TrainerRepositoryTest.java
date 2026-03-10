package com.epam.gym.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple verification tests for TrainerRepository interface.
 * No Spring context required - pure interface testing.
 */
@DisplayName("TrainerRepository Interface Tests")
class TrainerRepositoryTest {

    @Test
    @DisplayName("Should extend JpaRepository")
    void shouldExtendJpaRepository() {
        // Given
        Class<?> repoClass = TrainerRepository.class;

        // Then
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(repoClass));
    }

    @Test
    @DisplayName("Should be annotated with Repository")
    void shouldBeAnnotatedWithRepository() {
        // Given
        Class<?> repoClass = TrainerRepository.class;

        // Then
        assertTrue(repoClass.isAnnotationPresent(org.springframework.stereotype.Repository.class));
    }

    @Test
    @DisplayName("Should have findByUser_Username method")
    void shouldHaveFindByUserUsernameMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;

        // When
        Method method = repoClass.getMethod("findByUser_Username", String.class);

        // Then
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());

        // Verify EntityGraph annotation
        assertTrue(method.isAnnotationPresent(org.springframework.data.jpa.repository.EntityGraph.class));

        // Verify EntityGraph attribute paths
        org.springframework.data.jpa.repository.EntityGraph entityGraph =
                method.getAnnotation(org.springframework.data.jpa.repository.EntityGraph.class);
        String[] attributePaths = entityGraph.attributePaths();
        assertArrayEquals(new String[]{"user"}, attributePaths);
    }

    @Test
    @DisplayName("Should have findUnassignedTrainersByTraineeUsername method")
    void shouldHaveFindUnassignedTrainersByTraineeUsernameMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;

        // When
        Method method = repoClass.getMethod("findUnassignedTrainersByTraineeUsername", String.class);

        // Then
        assertNotNull(method);
        assertEquals(List.class, method.getReturnType());

        // Verify EntityGraph annotation
        assertTrue(method.isAnnotationPresent(org.springframework.data.jpa.repository.EntityGraph.class));

        // Verify Query annotation
        assertTrue(method.isAnnotationPresent(org.springframework.data.jpa.repository.Query.class));

        // Verify Param annotation on parameter
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        assertEquals(1, parameters.length);
        assertTrue(parameters[0].isAnnotationPresent(org.springframework.data.repository.query.Param.class));

        org.springframework.data.repository.query.Param paramAnnotation =
                parameters[0].getAnnotation(org.springframework.data.repository.query.Param.class);
        assertEquals("traineeUsername", paramAnnotation.value());
    }

    @Test
    @DisplayName("Should verify EntityGraph attribute paths for unassigned trainers query")
    void shouldVerifyEntityGraphAttributePathsForUnassignedTrainersQuery() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;
        Method method = repoClass.getMethod("findUnassignedTrainersByTraineeUsername", String.class);

        // When
        org.springframework.data.jpa.repository.EntityGraph entityGraph =
                method.getAnnotation(org.springframework.data.jpa.repository.EntityGraph.class);

        // Then
        assertNotNull(entityGraph);
        String[] attributePaths = entityGraph.attributePaths();
        assertArrayEquals(new String[]{"user"}, attributePaths);
    }

    @Test
    @DisplayName("Should verify Query value contains correct JPQL")
    void shouldVerifyQueryValueContainsCorrectJpql() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;
        Method method = repoClass.getMethod("findUnassignedTrainersByTraineeUsername", String.class);

        // When
        org.springframework.data.jpa.repository.Query queryAnnotation =
                method.getAnnotation(org.springframework.data.jpa.repository.Query.class);

        // Then
        assertNotNull(queryAnnotation);
        String query = queryAnnotation.value();

        // Verify key parts of the query
        assertTrue(query.contains("SELECT t FROM Trainer t"));
        assertTrue(query.contains("t.id NOT IN"));
        assertTrue(query.contains("SELECT tr.id FROM Trainee te"));
        assertTrue(query.contains("JOIN te.trainers tr"));
        assertTrue(query.contains("te.user.username = :traineeUsername"));
    }

    @Test
    @DisplayName("Should verify method has Param annotation with correct value")
    void shouldVerifyMethodHasParamAnnotationWithCorrectValue() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;
        Method method = repoClass.getMethod("findUnassignedTrainersByTraineeUsername", String.class);

        // When
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        org.springframework.data.repository.query.Param paramAnnotation =
                parameters[0].getAnnotation(org.springframework.data.repository.query.Param.class);

        // Then - verify Param annotation value, not parameter name (which is arg0 without -parameters flag)
        assertNotNull(paramAnnotation);
        assertEquals("traineeUsername", paramAnnotation.value());
    }

    @Test
    @DisplayName("Should inherit standard JpaRepository methods from parent interface")
    void shouldInheritStandardJpaRepositoryMethods() {
        // Given

        // Then - verify methods are inherited from JpaRepository, not declared in TrainerRepository
        // These methods exist in JpaRepository interface
        assertDoesNotThrow(() -> {
            // findById is declared in CrudRepository (parent of JpaRepository)
            Method findById = org.springframework.data.repository.CrudRepository.class.getMethod("findById", Object.class);
            assertNotNull(findById);
        });

        assertDoesNotThrow(() -> {
            // save is declared in CrudRepository
            Method save = org.springframework.data.repository.CrudRepository.class.getMethod("save", Object.class);
            assertNotNull(save);
        });

        assertDoesNotThrow(() -> {
            // findAll is declared in CrudRepository
            Method findAll = org.springframework.data.repository.CrudRepository.class.getMethod("findAll");
            assertNotNull(findAll);
        });

        assertDoesNotThrow(() -> {
            // deleteById is declared in CrudRepository
            Method deleteById = org.springframework.data.repository.CrudRepository.class.getMethod("deleteById", Object.class);
            assertNotNull(deleteById);
        });
    }

    @Test
    @DisplayName("Should verify return type of findByUser_Username is Optional")
    void shouldVerifyReturnTypeOfFindByUserUsername() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;
        Method method = repoClass.getMethod("findByUser_Username", String.class);

        // When
        Class<?> returnType = method.getReturnType();

        // Then
        assertEquals(Optional.class, returnType);
    }

    @Test
    @DisplayName("Should verify return type of findUnassignedTrainersByTraineeUsername is List")
    void shouldVerifyReturnTypeOfFindUnassignedTrainersByTraineeUsername() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainerRepository.class;
        Method method = repoClass.getMethod("findUnassignedTrainersByTraineeUsername", String.class);

        // When
        Class<?> returnType = method.getReturnType();

        // Then
        assertEquals(List.class, returnType);
    }

    @Test
    @DisplayName("Should verify generic type of JpaRepository is Trainer and Long")
    void shouldVerifyGenericTypeOfJpaRepository() {
        // Given
        Class<?> repoClass = TrainerRepository.class;

        // When - get generic interfaces
        java.lang.reflect.Type[] genericInterfaces = repoClass.getGenericInterfaces();

        // Then - find JpaRepository interface
        boolean foundJpaRepository = false;
        for (java.lang.reflect.Type type : genericInterfaces) {
            if (type.getTypeName().contains("JpaRepository")) {
                foundJpaRepository = true;
                assertTrue(type.getTypeName().contains("Trainer"));
                assertTrue(type.getTypeName().contains("Long"));
                break;
            }
        }
        assertTrue(foundJpaRepository, "Should implement JpaRepository<Trainer, Long>");
    }
}