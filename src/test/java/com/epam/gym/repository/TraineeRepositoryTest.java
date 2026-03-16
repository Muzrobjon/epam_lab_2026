package com.epam.gym.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple verification tests for TraineeRepository interface.
 * No Spring context required.
 */
@DisplayName("TraineeRepository Interface Tests")
class TraineeRepositoryTest {

    @Test
    @DisplayName("Should extend JpaRepository")
    void shouldExtendJpaRepository() {
        // Given
        Class<?> repoClass = TraineeRepository.class;

        // Then
        assertTrue(org.springframework.data.jpa.repository.JpaRepository.class.isAssignableFrom(repoClass));
    }

    @Test
    @DisplayName("Should have findByUser_Username method")
    void shouldHaveFindByUserUsernameMethod() throws NoSuchMethodException {
        // Given
        Class<?> repoClass = TraineeRepository.class;

        // When/Then
        Method method = repoClass.getMethod("findByUser_Username", String.class);
        assertNotNull(method);

        // Verify return type is Optional
        assertEquals(java.util.Optional.class, method.getReturnType());
    }

    @Test
    @DisplayName("Should be annotated with Repository")
    void shouldBeAnnotatedWithRepository() {
        // Given
        Class<?> repoClass = TraineeRepository.class;

        // Then
        assertTrue(repoClass.isAnnotationPresent(org.springframework.stereotype.Repository.class));
    }
}