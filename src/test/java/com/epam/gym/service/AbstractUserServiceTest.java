package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private Validator validator;

    private TestUserService testService;

    // Test uchun konkret implementation
    static class TestEntity {
        private User user;

        TestEntity(User user) {
            this.user = user;
        }

        User getUser() {
            return user;
        }
    }

    class TestUserService extends AbstractUserService<TestEntity> {
        private Function<String, TestEntity> findByUsernameFunc;

        public TestUserService(UserRepository userRepository,
                               UsernameGenerator usernameGenerator,
                               PasswordGenerator passwordGenerator,
                               Validator validator) {
            super(userRepository, usernameGenerator, passwordGenerator, validator);
        }

        void setFindByUsernameFunc(Function<String, TestEntity> func) {
            this.findByUsernameFunc = func;
        }

        @Override
        protected Function<String, TestEntity> findByUsername() {
            return findByUsernameFunc;
        }

        @Override
        protected User extractUser(TestEntity entity) {
            return entity != null ? entity.getUser() : null;
        }

        public void callValidateEntity(TestEntity entity) {
            validateEntity(entity);
        }
    }

    @BeforeEach
    void setUp() {
        testService = new TestUserService(userRepository, usernameGenerator,
                passwordGenerator, validator);
    }

    // ============ authenticate ============

    @Test
    void authenticate_success() {
        User user = User.builder()
                .username("john.doe")
                .password("password123")
                .build();
        TestEntity entity = new TestEntity(user);

        testService.setFindByUsernameFunc(username -> entity);

        testService.authenticate("john.doe", "password123");
    }

    @Test
    void authenticate_wrongPassword_throwsException() {
        User user = User.builder()
                .username("john.doe")
                .password("password123")
                .build();
        TestEntity entity = new TestEntity(user);

        testService.setFindByUsernameFunc(username -> entity);

        assertThatThrownBy(() -> testService.authenticate("john.doe", "wrongpass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid password");
    }

    @Test
    void authenticate_nullUser_throwsException() {
        TestEntity entity = new TestEntity(null);

        testService.setFindByUsernameFunc(username -> entity);

        assertThatThrownBy(() -> testService.authenticate("john.doe", "pass"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User not found");
    }

    // ============ changePassword ============

    @Test
    void changePassword_success() {
        User user = User.builder()
                .username("john.doe")
                .password("oldPass")
                .build();
        TestEntity entity = new TestEntity(user);

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        testService.setFindByUsernameFunc(username -> entity);

        testService.changePassword("john.doe", "oldPass", "newPass");

        assertThat(user.getPassword()).isEqualTo("newPass");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        User user = User.builder()
                .username("john.doe")
                .password("oldPass")
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                testService.changePassword("john.doe", "wrongOld", "newPass"))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_userNotFound_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                testService.changePassword("nonexistent", "old", "new"))
                .isInstanceOf(AuthenticationException.class);
    }

    // ============ toggleActiveStatus ============

    @Test
    void toggleActiveStatus_fromActiveToInactive() {
        User user = User.builder()
                .username("john.doe")
                .password("password")
                .isActive(true)
                .build();
        TestEntity entity = new TestEntity(user);

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        testService.setFindByUsernameFunc(username -> entity);

        testService.toggleActiveStatus("john.doe", "password");

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void toggleActiveStatus_fromInactiveToActive() {
        User user = User.builder()
                .username("john.doe")
                .password("password")
                .isActive(false)
                .build();
        TestEntity entity = new TestEntity(user);

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        testService.setFindByUsernameFunc(username -> entity);

        testService.toggleActiveStatus("john.doe", "password");

        assertThat(user.getIsActive()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void toggleActiveStatus_wrongPassword_throwsException() {
        User user = User.builder()
                .username("john.doe")
                .password("password")
                .isActive(true)
                .build();

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                testService.toggleActiveStatus("john.doe", "wrongpass"))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository, never()).save(any());
    }

    // ============ validateEntity ============

    @Test
    void validateEntity_valid_noException() {
        TestEntity entity = new TestEntity(new User());

        when(validator.validate(entity)).thenReturn(Collections.emptySet());

        testService.callValidateEntity(entity);
    }

    @Test
    void validateEntity_invalid_throwsValidationException() {
        TestEntity entity = new TestEntity(new User());

        @SuppressWarnings("unchecked")
        ConstraintViolation<TestEntity> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Field is required");
        when(validator.validate(entity)).thenReturn(Set.of(violation));

        assertThatThrownBy(() -> testService.callValidateEntity(entity))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("Field is required");
    }
}