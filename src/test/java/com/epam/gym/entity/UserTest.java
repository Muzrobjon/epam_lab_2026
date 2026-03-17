package com.epam.gym.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link User} entity.
 * Pure JUnit 5 - no external dependencies.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private static final Long ID = 1L;
    private static final Long ID_2 = 2L;
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "securePassword123";
    private static final String USERNAME_2 = "jane.doe";

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create user using no-args constructor")
    void shouldCreateUserUsingNoArgsConstructor() {
        // When
        User user = new User();

        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        // @Builder.Default initializes isActive to true even in no-args constructor
        assertNotNull(user.getIsActive());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user using all-args constructor")
    void shouldCreateUserUsingAllArgsConstructor() {
        // When
        User user = new User(ID, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, true);

        // Then
        assertEquals(ID, user.getId());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(USERNAME, user.getUsername());
        assertEquals(PASSWORD, user.getPassword());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user using all-args constructor with false isActive")
    void shouldCreateUserUsingAllArgsConstructorWithFalseIsActive() {
        // When
        User user = new User(ID, FIRST_NAME, LAST_NAME, USERNAME, PASSWORD, false);

        // Then
        assertFalse(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user using builder")
    void shouldCreateUserUsingBuilder() {
        // When
        User user = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        // Then
        assertEquals(ID, user.getId());
        assertEquals(FIRST_NAME, user.getFirstName());
        assertEquals(LAST_NAME, user.getLastName());
        assertEquals(USERNAME, user.getUsername());
        assertEquals(PASSWORD, user.getPassword());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user with default isActive using builder")
    void shouldCreateUserWithDefaultIsActiveUsingBuilder() {
        // When
        User user = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .build();

        // Then - @Builder.Default sets isActive to true
        assertNotNull(user.getIsActive());
        assertTrue(user.getIsActive());
    }

    @Test
    @DisplayName("Should create user with explicit false isActive using builder")
    void shouldCreateUserWithExplicitFalseIsActiveUsingBuilder() {
        // When
        User user = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(false)
                .build();

        // Then
        assertNotNull(user.getIsActive());
        assertFalse(user.getIsActive());
    }

    // ==================== Getter & Setter Tests ====================

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        // Given
        User user = new User();

        // When
        user.setId(ID);

        // Then
        assertEquals(ID, user.getId());
    }

    @Test
    @DisplayName("Should set and get first name")
    void shouldSetAndGetFirstName() {
        // Given
        User user = new User();

        // When
        user.setFirstName(FIRST_NAME);

        // Then
        assertEquals(FIRST_NAME, user.getFirstName());
    }

    @Test
    @DisplayName("Should set and get last name")
    void shouldSetAndGetLastName() {
        // Given
        User user = new User();

        // When
        user.setLastName(LAST_NAME);

        // Then
        assertEquals(LAST_NAME, user.getLastName());
    }

    @Test
    @DisplayName("Should set and get username")
    void shouldSetAndGetUsername() {
        // Given
        User user = new User();

        // When
        user.setUsername(USERNAME);

        // Then
        assertEquals(USERNAME, user.getUsername());
    }

    @Test
    @DisplayName("Should set and get password")
    void shouldSetAndGetPassword() {
        // Given
        User user = new User();

        // When
        user.setPassword(PASSWORD);

        // Then
        assertEquals(PASSWORD, user.getPassword());
    }

    @Test
    @DisplayName("Should set and get isActive")
    void shouldSetAndGetIsActive() {
        // Given
        User user = new User();

        // When
        user.setIsActive(false);

        // Then
        assertFalse(user.getIsActive());
    }

    @Test
    @DisplayName("Should override default isActive with setter")
    void shouldOverrideDefaultIsActiveWithSetter() {
        // Given - no-args constructor sets isActive to true via @Builder.Default
        User user = new User();

        // When
        user.setIsActive(false);

        // Then
        assertFalse(user.getIsActive());
    }

    // ==================== Equals & HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // Then
        assertEquals(user, user);
    }

    @Test
    @DisplayName("Should be equal to another user with same id")
    void shouldBeEqualToAnotherUserWithSameId() {
        // Given
        User user1 = createTestUser(ID, USERNAME);
        User user2 = createTestUser(ID, USERNAME_2);

        // Then - equals only checks id
        assertEquals(user1, user2);
    }

    @Test
    @DisplayName("Should not be equal to user with different id")
    void shouldNotBeEqualToUserWithDifferentId() {
        // Given
        User user1 = createTestUser(ID, USERNAME);
        User user2 = createTestUser(ID_2, USERNAME);

        // Then
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // Then
        assertNotEquals(null, user);
    }

    @Test
    @DisplayName("Should not be equal to different type")
    void shouldNotBeEqualToDifferentType() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // Then
        assertNotEquals("not a user", user);
    }

    @Test
    @DisplayName("Should return same hash code for equal objects")
    void shouldReturnSameHashCodeForEqualObjects() {
        // Given
        User user1 = createTestUser(ID, USERNAME);
        User user2 = createTestUser(ID, USERNAME_2);

        // Then
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should return consistent hash code")
    void shouldReturnConsistentHashCode() {
        // Given
        User user = createTestUser(ID, USERNAME);
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should include id in toString")
    void shouldIncludeIdInToString() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("id=" + ID));
    }

    @Test
    @DisplayName("Should include first name in toString")
    void shouldIncludeFirstNameInToString() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("firstName='" + FIRST_NAME + "'"));
    }

    @Test
    @DisplayName("Should include last name in toString")
    void shouldIncludeLastNameInToString() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("lastName='" + LAST_NAME + "'"));
    }

    @Test
    @DisplayName("Should include username in toString")
    void shouldIncludeUsernameInToString() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("username='" + USERNAME + "'"));
    }

    @Test
    @DisplayName("Should include isActive in toString")
    void shouldIncludeIsActiveInToString() {
        // Given
        User user = createTestUser(ID, USERNAME);

        // When
        String result = user.toString();

        // Then
        assertTrue(result.contains("isActive=" + true));
    }

    @Test
    @DisplayName("Should not include password in toString")
    void shouldNotIncludePasswordInToString() {
        // Given
        User user = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();

        // When
        String result = user.toString();

        // Then - security: password should not appear in logs
        assertFalse(result.contains("password"));
        assertFalse(result.contains(PASSWORD));
    }

    // ==================== Null Handling Tests ====================

    @Test
    @DisplayName("Should handle null id in equals")
    void shouldHandleNullIdInEquals() {
        // Given
        User user1 = createTestUser(null, USERNAME);
        User user2 = createTestUser(null, USERNAME);

        // Then
        assertNotEquals(user1, user2);
    }

    @Test
    @DisplayName("Should handle null first name")
    void shouldHandleNullFirstName() {
        // Given
        User user = new User();

        // When
        user.setFirstName(null);

        // Then
        assertNull(user.getFirstName());
    }

    @Test
    @DisplayName("Should handle null last name")
    void shouldHandleNullLastName() {
        // Given
        User user = new User();

        // When
        user.setLastName(null);

        // Then
        assertNull(user.getLastName());
    }

    @Test
    @DisplayName("Should handle null username")
    void shouldHandleNullUsername() {
        // Given
        User user = new User();

        // When
        user.setUsername(null);

        // Then
        assertNull(user.getUsername());
    }

    @Test
    @DisplayName("Should handle null password")
    void shouldHandleNullPassword() {
        // Given
        User user = new User();

        // When
        user.setPassword(null);

        // Then
        assertNull(user.getPassword());
    }

    @Test
    @DisplayName("Should handle null isActive via setter")
    void shouldHandleNullIsActiveViaSetter() {
        // Given
        User user = new User();

        // When
        user.setIsActive(null);

        // Then
        assertNull(user.getIsActive());
    }

    // ==================== Boolean State Tests ====================

    @Test
    @DisplayName("Should toggle isActive from true to false")
    void shouldToggleIsActiveFromTrueToFalse() {
        // Given
        User user = User.builder()
                .isActive(true)
                .build();

        // When
        user.setIsActive(false);

        // Then
        assertFalse(user.getIsActive());
    }

    @Test
    @DisplayName("Should toggle isActive from false to true")
    void shouldToggleIsActiveFromFalseToTrue() {
        // Given
        User user = User.builder()
                .isActive(false)
                .build();

        // When
        user.setIsActive(true);

        // Then
        assertTrue(user.getIsActive());
    }

    // ==================== Helper Methods ====================

    private User createTestUser(Long id, String username) {
        return User.builder()
                .id(id)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(username)
                .password(PASSWORD)
                .isActive(true)
                .build();
    }
}