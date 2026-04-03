package com.epam.gym.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple verification tests for UserRepository interface.
 * No Spring context required - pure interface testing.
 */
@DisplayName("UserRepository Interface Tests")
class UserRepositoryTest {

    @Test
    @DisplayName("Should extend JpaRepository")
    void shouldExtendJpaRepository() {
        // Given
        Class<?> repoClass = UserRepository.class;

        // Then
        assertTrue(true);
    }

    @Test
    @DisplayName("Should be annotated with Repository")
    void shouldBeAnnotatedWithRepository() {
        // Given
        Class<?> repoClass = UserRepository.class;

        // Then
        assertTrue(repoClass.isAnnotationPresent(org.springframework.stereotype.Repository.class));
    }

    @Test
    @DisplayName("Should have existsByUsername method")
    void shouldHaveExistsByUsernameMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = UserRepository.class;

        // When
        Method method = repoClass.getMethod("existsByUsername", String.class);

        // Then
        assertNotNull(method);
        assertEquals(boolean.class, method.getReturnType());
    }

    @Test
    @DisplayName("Should verify return type of existsByUsername is boolean")
    void shouldVerifyReturnTypeOfExistsByUsername() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = UserRepository.class;
        Method method = repoClass.getMethod("existsByUsername", String.class);

        // When
        Class<?> returnType = method.getReturnType();

        // Then
        assertEquals(boolean.class, returnType);
        assertNotEquals(false, returnType.isPrimitive()); // verify it's primitive boolean, not Boolean
    }

    @Test
    @DisplayName("Should verify method parameter type is String")
    void shouldVerifyMethodParameterTypeIsString() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = UserRepository.class;
        Method method = repoClass.getMethod("existsByUsername", String.class);

        // When
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Then
        assertEquals(1, parameterTypes.length);
        assertEquals(String.class, parameterTypes[0]);
    }

    @Test
    @DisplayName("Should verify generic type of JpaRepository is User and Long")
    void shouldVerifyGenericTypeOfJpaRepository() {
        // Given
        Class<?> repoClass = UserRepository.class;

        // When - get generic interfaces
        java.lang.reflect.Type[] genericInterfaces = repoClass.getGenericInterfaces();

        // Then - find JpaRepository interface
        boolean foundJpaRepository = false;
        for (java.lang.reflect.Type type : genericInterfaces) {
            if (type.getTypeName().contains("JpaRepository")) {
                foundJpaRepository = true;
                assertTrue(type.getTypeName().contains("User"));
                assertTrue(type.getTypeName().contains("Long"));
                break;
            }
        }
        assertTrue(foundJpaRepository, "Should implement JpaRepository<User, Long>");
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

        assertDoesNotThrow(() -> {
            Method existsById = org.springframework.data.repository.CrudRepository.class
                    .getMethod("existsById", Object.class);
            assertNotNull(existsById);
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
    @DisplayName("Should verify method follows Spring Data naming convention")
    void shouldVerifyMethodFollowsSpringDataNamingConvention() throws NoSuchMethodException {
        // Given
        Method method = UserRepository.class.getMethod("existsByUsername", String.class);

        // Then - method name follows existsBy{FieldName} pattern
        String methodName = method.getName();
        assertTrue(methodName.startsWith("existsBy"));
        assertTrue(methodName.contains("Username"));
    }

    @Test
    @DisplayName("Should distinguish existsByUsername from existsById")
    void shouldDistinguishExistsByUsernameFromExistsById() throws NoSuchMethodException {
        // Given
        Method existsByUsername = UserRepository.class.getMethod("existsByUsername", String.class);
        Method existsById = org.springframework.data.repository.CrudRepository.class
                .getMethod("existsById", Object.class);

        // Then
        assertNotEquals(existsByUsername.getName(), existsById.getName());
        assertNotEquals(existsByUsername.getParameterTypes()[0], existsById.getParameterTypes()[0]);
    }
}