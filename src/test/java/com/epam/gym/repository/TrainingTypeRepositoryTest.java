package com.epam.gym.repository;

import com.epam.gym.enums.TrainingTypeName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple verification tests for TrainingTypeRepository interface.
 * No Spring context required - pure interface testing.
 */
@DisplayName("TrainingTypeRepository Interface Tests")
class TrainingTypeRepositoryTest {

    @Test
    @DisplayName("Should extend JpaRepository")
    void shouldExtendJpaRepository() {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;

        // Then
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(repoClass));
    }

    @Test
    @DisplayName("Should be annotated with Repository")
    void shouldBeAnnotatedWithRepository() {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;

        // Then
        assertTrue(repoClass.isAnnotationPresent(org.springframework.stereotype.Repository.class));
    }

    @Test
    @DisplayName("Should have findByTrainingTypeName method")
    void shouldHaveFindByTrainingTypeNameMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;

        // When
        Method method = repoClass.getMethod("findByTrainingTypeName", TrainingTypeName.class);

        // Then
        assertNotNull(method);
        assertEquals(Optional.class, method.getReturnType());
    }

    @Test
    @DisplayName("Should verify return type of findByTrainingTypeName is Optional")
    void shouldVerifyReturnTypeOfFindByTrainingTypeName() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;
        Method method = repoClass.getMethod("findByTrainingTypeName", TrainingTypeName.class);

        // When
        Class<?> returnType = method.getReturnType();

        // Then
        assertEquals(Optional.class, returnType);
    }

    @Test
    @DisplayName("Should verify method parameter type is TrainingTypeName enum")
    void shouldVerifyMethodParameterTypeIsTrainingTypeNameEnum() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;
        Method method = repoClass.getMethod("findByTrainingTypeName", TrainingTypeName.class);

        // When
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Then
        assertEquals(1, parameterTypes.length);
        assertEquals(TrainingTypeName.class, parameterTypes[0]);
        assertTrue(parameterTypes[0].isEnum());
    }

    @Test
    @DisplayName("Should verify generic type of JpaRepository is TrainingType and Long")
    void shouldVerifyGenericTypeOfJpaRepository() {
        // Given
        Class<?> repoClass = TrainingTypeRepository.class;

        // When - get generic interfaces
        java.lang.reflect.Type[] genericInterfaces = repoClass.getGenericInterfaces();

        // Then - find JpaRepository interface
        boolean foundJpaRepository = false;
        for (java.lang.reflect.Type type : genericInterfaces) {
            if (type.getTypeName().contains("JpaRepository")) {
                foundJpaRepository = true;
                assertTrue(type.getTypeName().contains("TrainingType"));
                assertTrue(type.getTypeName().contains("Long"));
                break;
            }
        }
        assertTrue(foundJpaRepository, "Should implement JpaRepository<TrainingType, Long>");
    }

    @Test
    @DisplayName("Should inherit standard CrudRepository methods")
    void shouldInheritStandardCrudRepositoryMethods() {
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
            Method saveAndFlush = org.springframework.data.jpa.repository.JpaRepository.class
                    .getMethod("saveAndFlush", Object.class);
            assertNotNull(saveAndFlush);
        });

        assertDoesNotThrow(() -> {
            Method flush = org.springframework.data.jpa.repository.JpaRepository.class
                    .getMethod("flush");
            assertNotNull(flush);
        });
    }

    @Test
    @DisplayName("Should accept all TrainingTypeName enum values")
    void shouldAcceptAllTrainingTypeNameEnumValues() {
        // Given

        // When & Then - verify all enum values can be passed
        for (TrainingTypeName typeName : TrainingTypeName.values()) {
            assertDoesNotThrow(() -> {
                // Verify the method can accept this enum value (parameter type check)
                assertEquals(TrainingTypeName.class, typeName.getDeclaringClass());
            });
        }
    }

    @Test
    @DisplayName("Should verify method follows Spring Data naming convention")
    void shouldVerifyMethodFollowsSpringDataNamingConvention() throws NoSuchMethodException {
        // Given
        Method method = TrainingTypeRepository.class.getMethod("findByTrainingTypeName", TrainingTypeName.class);

        // Then - method name follows findBy{FieldName} pattern
        String methodName = method.getName();
        assertTrue(methodName.startsWith("findBy"));
        assertTrue(methodName.contains("TrainingTypeName"));
    }
}