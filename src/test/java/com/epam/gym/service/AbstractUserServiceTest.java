package com.epam.gym.service;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private Validator validator;

    private AbstractUserService<User> userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new TestUserService(userRepository, usernameGenerator, passwordGenerator, validator);
    }

    @Test
    void authenticate_success() {
        String username = "test.user";
        String password = "pass123";
        User user = new User();
        user.setPassword(password);

        userService = spy(userService);

        when(userService.findByUsername()).thenReturn(ignored -> user);
        when(userService.extractUser(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> userService.authenticate(username, password));
    }

    @Test
    void authenticate_userNotFound() {
        String username = "unknown";
        String password = "pass123";

        userService = spy(userService);

        when(userService.findByUsername()).thenReturn(ignored -> null);
        when(userService.extractUser(null)).thenReturn(null);

        assertThrows(AuthenticationException.class,
                () -> userService.authenticate(username, password));
    }

    @Test
    void authenticate_invalidPassword() {
        String username = "test.user";
        String password = "wrong";
        User user = new User();
        user.setPassword("correct");

        userService = spy(userService);

        when(userService.findByUsername()).thenReturn(ignored -> user);
        when(userService.extractUser(any(User.class))).thenReturn(user);

        assertThrows(AuthenticationException.class,
                () -> userService.authenticate(username, password));
    }

    @Test
    void changePassword_success() {
        String username = "test.user";
        String oldPassword = "old123";
        String newPassword = "new456";
        User user = new User();
        user.setPassword(oldPassword);

        userService = spy(userService);

        when(userService.findByUsername()).thenReturn(ignored -> user);
        when(userService.extractUser(any(User.class))).thenReturn(user);
        doNothing().when(userService).authenticate(username, oldPassword);

        assertDoesNotThrow(() -> userService.changePassword(username, oldPassword, newPassword));

        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void changePassword_userNotFound() {
        String username = "unknown";
        String oldPassword = "old123";
        String newPassword = "new456";

        userService = spy(userService);

        doNothing().when(userService).authenticate(username, oldPassword);
        when(userService.findByUsername()).thenReturn(ignored -> null);
        when(userService.extractUser(null)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> userService.changePassword(username, oldPassword, newPassword));
    }

    @Test
    void toggleActiveStatus_success() {
        String username = "test.user";
        String password = "pass123";
        User user = new User();
        user.setPassword(password);
        user.setIsActive(false);

        userService = spy(userService);

        doNothing().when(userService).authenticate(username, password);
        when(userService.findByUsername()).thenReturn(ignored -> user);
        when(userService.extractUser(any(User.class))).thenReturn(user);

        userService.toggleActiveStatus(username, password);

        assertTrue(user.getIsActive());
    }

    @Test
    void toggleActiveStatus_userNotFound() {
        String username = "unknown";
        String password = "pass123";

        userService = spy(userService);

        doNothing().when(userService).authenticate(username, password);
        when(userService.findByUsername()).thenReturn(ignored -> null);
        when(userService.extractUser(null)).thenReturn(null);

        assertThrows(NotFoundException.class,
                () -> userService.toggleActiveStatus(username, password));
    }

    @Test
    void validateEntity_success() {
        User user = new User();

        when(validator.validate(user)).thenReturn(Collections.emptySet());

        assertDoesNotThrow(() -> userService.validateEntity(user));
    }

    @Test
    void validateEntity_violation() {
        User user = new User();

        @SuppressWarnings("unchecked")
        ConstraintViolation<User> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("username must not be empty");
        when(validator.validate(user)).thenReturn(Set.of(violation));

        assertThrows(ValidationException.class,
                () -> userService.validateEntity(user));
    }

    // -------------------------------------------------------------------------
    // Minimal concrete implementation for testing abstract class
    // -------------------------------------------------------------------------
    static class TestUserService extends AbstractUserService<User> {

        public TestUserService(UserRepository userRepository,
                               UsernameGenerator usernameGenerator,
                               PasswordGenerator passwordGenerator,
                               Validator validator) {
            super(userRepository, usernameGenerator, passwordGenerator, validator);
        }

        @Override
        protected Function<String, User> findByUsername() {
            // Default stub — will be overridden with when(...).thenReturn(...) in tests
            return username -> null;
        }

        @Override
        protected User extractUser(User entity) {
            return entity;
        }
    }
}