package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AbstractUserService Tests")
class AbstractUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private Validator validator;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private TestUserService testUserService;
    private TestEntity testEntity;
    private User testUser;

    /**
     * Concrete implementation of AbstractUserService for testing
     */
    static class TestEntity {
        private User user;

        public TestEntity(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    class TestUserService extends AbstractUserService<TestEntity> {
        private Function<String, TestEntity> findByUsernameFunction;

        public TestUserService(
                UserRepository userRepository,
                UsernameGenerator usernameGenerator,
                PasswordGenerator passwordGenerator,
                Validator validator
        ) {
            super(userRepository, usernameGenerator, passwordGenerator, validator);
        }

        public void setFindByUsernameFunction(Function<String, TestEntity> function) {
            this.findByUsernameFunction = function;
        }

        @Override
        protected Function<String, TestEntity> findByUsername() {
            return findByUsernameFunction;
        }

        @Override
        protected User extractUser(TestEntity entity) {
            return entity != null ? entity.getUser() : null;
        }

        // Expose protected methods for testing
        public User testCreateAndSaveUser(String firstName, String lastName) {
            return createAndSaveUser(firstName, lastName);
        }

        public void testUpdateUserBasicInfo(User existingUser, User updatedUser) {
            updateUserBasicInfo(existingUser, updatedUser);
        }

        public void testAuthenticateUser(String username, String password) {
            authenticateUser(username, password);
        }

        public void testValidateEntity(TestEntity entity) {
            validateEntity(entity);
        }
    }

    @BeforeEach
    void setUp() {
        testUserService = new TestUserService(
                userRepository,
                usernameGenerator,
                passwordGenerator,
                validator
        );

        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();

        testEntity = new TestEntity(testUser);

        // Default setup for findByUsername
        testUserService.setFindByUsernameFunction(username -> {
            if ("john.doe".equals(username)) {
                return testEntity;
            }
            throw new NotFoundException("Entity not found for username: " + username);
        });
    }

    @Nested
    @DisplayName("createAndSaveUser Tests")
    class CreateAndSaveUserTests {

        @Test
        @DisplayName("Should create user with generated username and password")
        void shouldCreateUserWithGeneratedUsernameAndPassword() {
            String firstName = "John";
            String lastName = "Doe";
            String generatedPassword = "generatedPass123";
            String generatedUsername = "john.doe";

            when(passwordGenerator.generatePassword()).thenReturn(generatedPassword);
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(generatedUsername);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            User result = testUserService.testCreateAndSaveUser(firstName, lastName);

            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo(firstName);
            assertThat(result.getLastName()).isEqualTo(lastName);
            assertThat(result.getUsername()).isEqualTo(generatedUsername);
            assertThat(result.getPassword()).isEqualTo(generatedPassword);
            assertThat(result.getIsActive()).isTrue();

            verify(passwordGenerator).generatePassword();
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getFirstName()).isEqualTo(firstName);
            assertThat(savedUser.getLastName()).isEqualTo(lastName);
        }

        @Test
        @DisplayName("Should set user as active by default")
        void shouldSetUserAsActiveByDefault() {
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn("test.user");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = testUserService.testCreateAndSaveUser("Test", "User");

            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should pass existsByUsername function to username generator")
        void shouldPassExistsByUsernameFunctionToUsernameGenerator() {
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn("test.user");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            testUserService.testCreateAndSaveUser("Test", "User");

            verify(usernameGenerator).generateUsername(any(User.class), any());
        }
    }

    @Nested
    @DisplayName("updateUserBasicInfo Tests")
    class UpdateUserBasicInfoTests {

        @Test
        @DisplayName("Should update first name when provided")
        void shouldUpdateFirstNameWhenProvided() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .firstName("Jane")
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, updatedUser);

            assertThat(existingUser.getFirstName()).isEqualTo("Jane");
            assertThat(existingUser.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should update last name when provided")
        void shouldUpdateLastNameWhenProvided() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .lastName("Smith")
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, updatedUser);

            assertThat(existingUser.getFirstName()).isEqualTo("John");
            assertThat(existingUser.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should update both first and last name when provided")
        void shouldUpdateBothNamesWhenProvided() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, updatedUser);

            assertThat(existingUser.getFirstName()).isEqualTo("Jane");
            assertThat(existingUser.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should not update when updated user is null")
        void shouldNotUpdateWhenUpdatedUserIsNull() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, null);

            assertThat(existingUser.getFirstName()).isEqualTo("John");
            assertThat(existingUser.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should not update first name when null in updated user")
        void shouldNotUpdateFirstNameWhenNull() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .firstName(null)
                    .lastName("Smith")
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, updatedUser);

            assertThat(existingUser.getFirstName()).isEqualTo("John");
            assertThat(existingUser.getLastName()).isEqualTo("Smith");
        }

        @Test
        @DisplayName("Should not update last name when null in updated user")
        void shouldNotUpdateLastNameWhenNull() {
            User existingUser = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User updatedUser = User.builder()
                    .firstName("Jane")
                    .lastName(null)
                    .build();

            testUserService.testUpdateUserBasicInfo(existingUser, updatedUser);

            assertThat(existingUser.getFirstName()).isEqualTo("Jane");
            assertThat(existingUser.getLastName()).isEqualTo("Doe");
        }
    }

    @Nested
    @DisplayName("authenticateUser Tests")
    class AuthenticateUserTests {

        @Test
        @DisplayName("Should authenticate user with correct credentials")
        void shouldAuthenticateUserWithCorrectCredentials() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            testUserService.testAuthenticateUser("john.doe", "password123");

            verify(userRepository).findByUsername("john.doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user not found")
        void shouldThrowAuthenticationExceptionWhenUserNotFound() {
            when(userRepository.findByUsername("unknown.user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testUserService.testAuthenticateUser("unknown.user", "password"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIncorrect() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> testUserService.testAuthenticateUser("john.doe", "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid password");
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate entity with correct credentials")
        void shouldAuthenticateEntityWithCorrectCredentials() {
            testUserService.authenticate("john.doe", "password123");

            // No exception means success
        }

        @Test
        @DisplayName("Should throw AuthenticationException when entity not found")
        void shouldThrowAuthenticationExceptionWhenEntityNotFound() {
            testUserService.setFindByUsernameFunction(username -> {
                throw new NotFoundException("Entity not found");
            });

            assertThatThrownBy(() -> testUserService.authenticate("unknown.user", "password"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user is null in entity")
        void shouldThrowAuthenticationExceptionWhenUserIsNullInEntity() {
            TestEntity entityWithNullUser = new TestEntity(null);
            testUserService.setFindByUsernameFunction(username -> entityWithNullUser);

            assertThatThrownBy(() -> testUserService.authenticate("john.doe", "password"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIncorrect() {
            assertThatThrownBy(() -> testUserService.authenticate("john.doe", "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid password");
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.changePassword("john.doe", "password123", "newPassword456");

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("newPassword456");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when old password is incorrect")
        void shouldThrowAuthenticationExceptionWhenOldPasswordIncorrect() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> testUserService.changePassword("john.doe", "wrongPassword", "newPassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid password");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when entity not found")
        void shouldThrowNotFoundExceptionWhenEntityNotFound() {
            when(userRepository.findByUsername("unknown.user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> testUserService.changePassword("unknown.user", "password", "newPassword"))
                    .isInstanceOf(AuthenticationException.class);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user is null in entity")
        void shouldThrowNotFoundExceptionWhenUserIsNullInEntity() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            TestEntity entityWithNullUser = new TestEntity(null);
            testUserService.setFindByUsernameFunction(username -> entityWithNullUser);

            assertThatThrownBy(() -> testUserService.changePassword("john.doe", "password123", "newPassword"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("toggleActiveStatus Tests")
    class ToggleActiveStatusTests {

        @Test
        @DisplayName("Should toggle active status from true to false")
        void shouldToggleActiveStatusFromTrueToFalse() {
            testUser.setIsActive(true);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.toggleActiveStatus("john.doe", "password123");

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should toggle active status from false to true")
        void shouldToggleActiveStatusFromFalseToTrue() {
            testUser.setIsActive(false);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.toggleActiveStatus("john.doe", "password123");

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIncorrect() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> testUserService.toggleActiveStatus("john.doe", "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when user is null in entity")
        void shouldThrowNotFoundExceptionWhenUserIsNullInEntity() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            TestEntity entityWithNullUser = new TestEntity(null);
            testUserService.setFindByUsernameFunction(username -> entityWithNullUser);

            assertThatThrownBy(() -> testUserService.toggleActiveStatus("john.doe", "password123"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("setActiveStatus Tests")
    class SetActiveStatusTests {

        @Test
        @DisplayName("Should set active status to true")
        void shouldSetActiveStatusToTrue() {
            testUser.setIsActive(false);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.setActiveStatus("john.doe", "password123", true);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should set active status to false")
        void shouldSetActiveStatusToFalse() {
            testUser.setIsActive(true);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.setActiveStatus("john.doe", "password123", false);

            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should be idempotent - setting same status multiple times")
        void shouldBeIdempotent() {
            testUser.setIsActive(true);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            testUserService.setActiveStatus("john.doe", "password123", true);
            testUserService.setActiveStatus("john.doe", "password123", true);

            verify(userRepository, times(2)).save(userCaptor.capture());
            assertThat(userCaptor.getAllValues()).allMatch(User::getIsActive);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIncorrect() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> testUserService.setActiveStatus("john.doe", "wrongPassword", true))
                    .isInstanceOf(AuthenticationException.class);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when user is null in entity")
        void shouldThrowNotFoundExceptionWhenUserIsNullInEntity() {
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            TestEntity entityWithNullUser = new TestEntity(null);
            testUserService.setFindByUsernameFunction(username -> entityWithNullUser);

            assertThatThrownBy(() -> testUserService.setActiveStatus("john.doe", "password123", true))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("validateEntity Tests")
    class ValidateEntityTests {

        @Test
        @DisplayName("Should pass validation when no violations")
        void shouldPassValidationWhenNoViolations() {
            when(validator.validate(any(TestEntity.class))).thenReturn(Collections.emptySet());

            testUserService.testValidateEntity(testEntity);

            verify(validator).validate(testEntity);
        }

        @Test
        @DisplayName("Should throw ValidationException when violations exist")
        void shouldThrowValidationExceptionWhenViolationsExist() {
            @SuppressWarnings("unchecked")
            ConstraintViolation<TestEntity> violation1 = mock(ConstraintViolation.class);
            @SuppressWarnings("unchecked")
            ConstraintViolation<TestEntity> violation2 = mock(ConstraintViolation.class);

            when(violation1.getMessage()).thenReturn("First name is required");
            when(violation2.getMessage()).thenReturn("Last name is required");

            Set<ConstraintViolation<TestEntity>> violations = new HashSet<>();
            violations.add(violation1);
            violations.add(violation2);

            when(validator.validate(any(TestEntity.class))).thenReturn(violations);

            assertThatThrownBy(() -> testUserService.testValidateEntity(testEntity))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Validation failed")
                    .hasMessageContaining("First name is required");
        }

        @Test
        @DisplayName("Should include all violation messages in exception")
        void shouldIncludeAllViolationMessagesInException() {
            @SuppressWarnings("unchecked")
            ConstraintViolation<TestEntity> violation = mock(ConstraintViolation.class);
            when(violation.getMessage()).thenReturn("Field is invalid");

            Set<ConstraintViolation<TestEntity>> violations = Set.of(violation);
            when(validator.validate(any(TestEntity.class))).thenReturn(violations);

            assertThatThrownBy(() -> testUserService.testValidateEntity(testEntity))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Field is invalid");
        }
    }

    @Nested
    @DisplayName("Abstract Method Implementation Tests")
    class AbstractMethodTests {

        @Test
        @DisplayName("findByUsername should return correct entity")
        void findByUsernameShouldReturnCorrectEntity() {
            TestEntity result = testUserService.findByUsername().apply("john.doe");

            assertThat(result).isEqualTo(testEntity);
            assertThat(result.getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("findByUsername should throw NotFoundException for unknown user")
        void findByUsernameShouldThrowNotFoundExceptionForUnknownUser() {
            assertThatThrownBy(() -> testUserService.findByUsername().apply("unknown.user"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("extractUser should return user from entity")
        void extractUserShouldReturnUserFromEntity() {
            User result = testUserService.extractUser(testEntity);

            assertThat(result).isEqualTo(testUser);
        }

        @Test
        @DisplayName("extractUser should return null for null entity")
        void extractUserShouldReturnNullForNullEntity() {
            User result = testUserService.extractUser(null);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("extractUser should return null when entity has null user")
        void extractUserShouldReturnNullWhenEntityHasNullUser() {
            TestEntity entityWithNullUser = new TestEntity(null);

            User result = testUserService.extractUser(entityWithNullUser);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty strings in user creation")
        void shouldHandleEmptyStringsInUserCreation() {
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(".");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = testUserService.testCreateAndSaveUser("", "");

            assertThat(result.getFirstName()).isEmpty();
            assertThat(result.getLastName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void shouldHandleSpecialCharactersInNames() {
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn("jean-pierre.o'connor");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = testUserService.testCreateAndSaveUser("Jean-Pierre", "O'Connor");

            assertThat(result.getFirstName()).isEqualTo("Jean-Pierre");
            assertThat(result.getLastName()).isEqualTo("O'Connor");
        }

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn("josé.müller");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = testUserService.testCreateAndSaveUser("José", "Müller");

            assertThat(result.getFirstName()).isEqualTo("José");
            assertThat(result.getLastName()).isEqualTo("Müller");
        }

        @Test
        @DisplayName("Should handle password with special characters")
        void shouldHandlePasswordWithSpecialCharacters() {
            String specialPassword = "P@$$w0rd!#%&*";
            testUser.setPassword(specialPassword);
            when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(testUser));

            testUserService.testAuthenticateUser("john.doe", specialPassword);

            verify(userRepository).findByUsername("john.doe");
        }

        @Test
        @DisplayName("Should handle very long username")
        void shouldHandleVeryLongUsername() {
            String longUsername = "a".repeat(255);
            User userWithLongUsername = User.builder()
                    .username(longUsername)
                    .password("password123")
                    .build();

            when(userRepository.findByUsername(longUsername)).thenReturn(Optional.of(userWithLongUsername));

            testUserService.testAuthenticateUser(longUsername, "password123");

            verify(userRepository).findByUsername(longUsername);
        }
    }
}