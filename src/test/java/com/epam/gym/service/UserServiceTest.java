package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.WeakPasswordException;
import com.epam.gym.metrics.UserMetrics;
import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordService passwordService;

    @Mock
    private UserMetrics userMetrics;

    @InjectMocks
    private UserService userService;

    private static final String USERNAME = "John.Doe";
    private static final String RAW_PASSWORD = "RawPass@123";
    private static final String ENCODED_PASSWORD = "$2a$12$encodedHash";

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of())
        );
        SecurityContextHolder.setContext(context);
    }

    // ==================== CREATE USER TESTS ====================

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_Success() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            User result = userService.createUser("John", "Doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getPassword()).isEqualTo(RAW_PASSWORD);

            verify(passwordService).generateRandomPassword();
            verify(passwordService).encodePassword(RAW_PASSWORD);
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(any(User.class));
            verify(userMetrics).incrementRegistrations();
        }

        @Test
        @DisplayName("Should set user as active by default")
        void createUser_SetsActiveByDefault() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertThat(saved.getIsActive()).isTrue();
                saved.setId(1L);
                return saved;
            });

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void createUser_EncodesPasswordBeforeSaving() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertThat(saved.getPassword()).isEqualTo(ENCODED_PASSWORD);
                return saved;
            });

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(passwordService).encodePassword(RAW_PASSWORD);
        }

        @Test
        @DisplayName("Should return raw password in saved user")
        void createUser_ReturnsRawPassword() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            User result = userService.createUser("John", "Doe");

            // Then
            assertThat(result.getPassword()).isEqualTo(RAW_PASSWORD);
        }

        @Test
        @DisplayName("Should generate username using generator")
        void createUser_GeneratesUsername() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertThat(saved.getUsername()).isEqualTo("John.Doe");
                return saved;
            });

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(usernameGenerator).generateUsername(any(User.class), any());
        }

        @Test
        @DisplayName("Should increment registration metrics")
        void createUser_IncrementsMetrics() {
            // Given
            when(passwordService.generateRandomPassword()).thenReturn(RAW_PASSWORD);
            when(passwordService.encodePassword(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userMetrics).incrementRegistrations();
        }
    }

    // ==================== FIND BY USERNAME TESTS ====================

    @Nested
    @DisplayName("Find By Username Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should find user by username successfully")
        void findByUsername_Success() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

            // When
            User result = userService.findByUsername(USERNAME);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");

            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void findByUsername_NotFound_ThrowsNotFoundException() {
            // Given
            when(userRepository.findByUsername("NonExistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findByUsername("NonExistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found: NonExistent");
        }
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        private static final String OLD_PASSWORD = "OldPass@123";
        private static final String NEW_PASSWORD = "NewPass@456";
        private static final String NEW_ENCODED = "$2a$12$newEncodedHash";

        @BeforeEach
        void setUp() {
            setAuthenticatedUser(USERNAME);
        }

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordService.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(passwordService.isPasswordStrong(NEW_PASSWORD)).thenReturn(true);
            when(passwordService.encodePassword(NEW_PASSWORD)).thenReturn(NEW_ENCODED);
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

            // Then
            verify(passwordService).matches(OLD_PASSWORD, ENCODED_PASSWORD);
            verify(passwordService).isPasswordStrong(NEW_PASSWORD);
            verify(passwordService).encodePassword(NEW_PASSWORD);
            verify(userRepository).save(user);
            verify(userMetrics).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should set encoded new password on user")
        void changePassword_SetsEncodedPassword() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordService.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(passwordService.isPasswordStrong(NEW_PASSWORD)).thenReturn(true);
            when(passwordService.encodePassword(NEW_PASSWORD)).thenReturn(NEW_ENCODED);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                assertThat(saved.getPassword()).isEqualTo(NEW_ENCODED);
                return saved;
            });

            // When
            userService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD);

            // Then
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when old password is wrong")
        void changePassword_WrongOldPassword_ThrowsAuthException() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordService.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Invalid old password");

            verify(userMetrics).incrementLoginFailure();
            verify(passwordService, never()).isPasswordStrong(anyString());
            verify(passwordService, never()).encodePassword(anyString());
            verify(userRepository, never()).save(any());
            verify(userMetrics, never()).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should throw WeakPasswordException when new password is weak")
        void changePassword_WeakNewPassword_ThrowsWeakPasswordException() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(passwordService.matches(OLD_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
            when(passwordService.isPasswordStrong("weak")).thenReturn(false);

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword(USERNAME, OLD_PASSWORD, "weak"))
                    .isInstanceOf(WeakPasswordException.class)
                    .hasMessageContaining("Password must be at least 8 characters");

            verify(passwordService, never()).encodePassword(anyString());
            verify(userRepository, never()).save(any());
            verify(userMetrics, never()).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void changePassword_NotOwner_ThrowsAuthException() {
            // Given
            setAuthenticatedUser("Other.User");

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Access denied");

            verify(userRepository, never()).findByUsername(anyString());
            verify(passwordService, never()).matches(anyString(), anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void changePassword_UserNotFound_ThrowsNotFoundException() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword(USERNAME, OLD_PASSWORD, NEW_PASSWORD))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(passwordService, never()).matches(anyString(), anyString());
        }
    }

    // ==================== SET ACTIVE STATUS TESTS ====================

    @Nested
    @DisplayName("Set Active Status Tests")
    class SetActiveStatusTests {

        @BeforeEach
        void setUp() {
            setAuthenticatedUser(USERNAME);
        }

        @Test
        @DisplayName("Should activate user successfully")
        void setActiveStatus_Activate_Success() {
            // Given
            user.setIsActive(false);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.setActiveStatus(USERNAME, true);

            // Then
            assertThat(user.getIsActive()).isTrue();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should deactivate user successfully")
        void setActiveStatus_Deactivate_Success() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            // When
            userService.setActiveStatus(USERNAME, false);

            // Then
            assertThat(user.getIsActive()).isFalse();
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not resource owner")
        void setActiveStatus_NotOwner_ThrowsAuthException() {
            // Given
            setAuthenticatedUser("Other.User");

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus(USERNAME, true))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Access denied");

            verify(userRepository, never()).findByUsername(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void setActiveStatus_UserNotFound_ThrowsNotFoundException() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus(USERNAME, true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found");

            verify(userRepository, never()).save(any());
        }
    }

    // ==================== UPDATE USER BASIC INFO TESTS ====================

    @Nested
    @DisplayName("Update User Basic Info Tests")
    class UpdateUserBasicInfoTests {

        @Test
        @DisplayName("Should update all fields")
        void updateUserBasicInfo_AllFields_Success() {
            // When
            userService.updateUserBasicInfo(user, "Jane", "Smith", false);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only first name when others are null")
        void updateUserBasicInfo_OnlyFirstName() {
            // When
            userService.updateUserBasicInfo(user, "Jane", null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only last name when others are null")
        void updateUserBasicInfo_OnlyLastName() {
            // When
            userService.updateUserBasicInfo(user, null, "Smith", null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only active status when others are null")
        void updateUserBasicInfo_OnlyActiveStatus() {
            // When
            userService.updateUserBasicInfo(user, null, null, false);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should not change anything when all params are null")
        void updateUserBasicInfo_AllNull_NoChanges() {
            // When
            userService.updateUserBasicInfo(user, null, null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should not interact with any dependencies")
        void updateUserBasicInfo_NoServiceInteraction() {
            // When
            userService.updateUserBasicInfo(user, "Jane", "Smith", false);

            // Then
            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordService);
            verifyNoInteractions(usernameGenerator);
            verifyNoInteractions(userMetrics);
        }
    }

    // ==================== VERIFY RESOURCE OWNERSHIP TESTS ====================

    @Nested
    @DisplayName("Verify Resource Ownership Tests")
    class VerifyResourceOwnershipTests {

        @Test
        @DisplayName("Should pass when authenticated user matches username")
        void verifyResourceOwnership_MatchingUser_Success() {
            // Given
            setAuthenticatedUser(USERNAME);

            // When & Then (no exception)
            userService.verifyResourceOwnership(USERNAME);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when users don't match")
        void verifyResourceOwnership_DifferentUser_ThrowsAuthException() {
            // Given
            setAuthenticatedUser("Other.User");

            // When & Then
            assertThatThrownBy(() -> userService.verifyResourceOwnership(USERNAME))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("Access denied: you can only modify your own resources");
        }

        @Test
        @DisplayName("Should throw NullPointerException when no authentication")
        void verifyResourceOwnership_NoAuthentication_ThrowsException() {
            // Given — SecurityContext is empty (cleared in @AfterEach)
            SecurityContextHolder.clearContext();

            // When & Then
            assertThatThrownBy(() -> userService.verifyResourceOwnership(USERNAME))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}