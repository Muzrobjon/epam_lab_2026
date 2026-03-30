package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.metrics.UserMetrics;
import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UserMetrics userMetrics;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .password("password123")
                .isActive(true)
                .build();
    }

    private void setCurrentUser(User user) {
        ReflectionTestUtils.setField(userService, "currentUser", user);
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_Success() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("generatedPass123");
            // ✅ FIXED: Use any() instead of any(Predicate.class)
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User result = userService.createUser("John", "Doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("John.Doe");

            verify(passwordGenerator).generatePassword();
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(userCaptor.capture());
            verify(userMetrics).incrementRegistrations();

            User captured = userCaptor.getValue();
            assertThat(captured.getFirstName()).isEqualTo("John");
            assertThat(captured.getLastName()).isEqualTo("Doe");
            assertThat(captured.getPassword()).isEqualTo("generatedPass123");
            assertThat(captured.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should create user with generated username")
        void createUser_WithGeneratedUsername() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe1");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(1L);
                return u;
            });

            // When
            User result = userService.createUser("John", "Doe");

            // Then
            assertThat(result.getUsername()).isEqualTo("John.Doe1");
        }

        @Test
        @DisplayName("Should set user active by default")
        void createUser_ActiveByDefault() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should increment registration metrics")
        void createUser_IncrementsMetrics() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userMetrics).incrementRegistrations();
        }
    }

    @Nested
    @DisplayName("findByUsername Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when found")
        void findByUsername_Success() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When
            User result = userService.findByUsername("John.Doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("John.Doe");
            assertThat(result.getFirstName()).isEqualTo("John");
            verify(userRepository).findByUsername("John.Doe");
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void findByUsername_NotFound() {
            // Given
            when(userRepository.findByUsername("Unknown.User"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findByUsername("Unknown.User"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found: Unknown.User");
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate successfully with valid credentials")
        void authenticate_Success() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When
            userService.authenticate("John.Doe", "password123");

            // Then
            verify(userMetrics).incrementLoginSuccess();
            verify(userMetrics, never()).incrementLoginFailure();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user not found")
        void authenticate_UserNotFound() {
            // Given
            when(userRepository.findByUsername("Unknown.User"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("Unknown.User", "pass123"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userMetrics).incrementLoginFailure();
            verify(userMetrics, never()).incrementLoginSuccess();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is wrong")
        void authenticate_WrongPassword() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("John.Doe", "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userMetrics).incrementLoginFailure();
            verify(userMetrics, never()).incrementLoginSuccess();
        }

        @Test
        @DisplayName("Should set currentUser on successful authentication")
        void authenticate_SetsCurrentUser() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When
            userService.authenticate("John.Doe", "password123");

            // Then - verify currentUser is set
            userService.isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("Should not set currentUser on failed authentication")
        void authenticate_DoesNotSetCurrentUserOnFailure() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("John.Doe", "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class);

            assertThatThrownBy(() -> userService.isAuthenticated("John.Doe"))
                    .isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            // Given
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.changePassword("John.Doe", "password123", "newPassword456");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("newPassword456");
            verify(userMetrics).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not authenticated")
        void changePassword_NotAuthenticated() {
            // Given - no currentUser set

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword("John.Doe", "password123", "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(userRepository, never()).save(any());
            verify(userMetrics, never()).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when old password is wrong")
        void changePassword_WrongOldPassword() {
            // Given
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword("John.Doe", "wrongOldPassword", "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userMetrics).incrementLoginFailure();
            verify(userMetrics, never()).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when authenticated as different user")
        void changePassword_DifferentUser() {
            // Given
            User anotherUser = User.builder()
                    .id(2L)
                    .username("Another.User")
                    .password("pass")
                    .build();
            setCurrentUser(anotherUser);

            // When & Then
            assertThatThrownBy(() ->
                    userService.changePassword("John.Doe", "password123", "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated: John.Doe");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("setActiveStatus Tests")
    class SetActiveStatusTests {

        @Test
        @DisplayName("Should set active status to true")
        void setActiveStatus_ToTrue() {
            // Given
            testUser.setIsActive(false);
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.setActiveStatus("John.Doe", true);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should set active status to false")
        void setActiveStatus_ToFalse() {
            // Given
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.setActiveStatus("John.Doe", false);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when not authenticated")
        void setActiveStatus_NotAuthenticated() {
            // Given - no currentUser set

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus("John.Doe", true))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void setActiveStatus_UserNotFound() {
            // Given
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus("John.Doe", true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found: John.Doe");
        }
    }

    @Nested
    @DisplayName("updateUserBasicInfo Tests")
    class UpdateUserBasicInfoTests {

        @Test
        @DisplayName("Should update all fields when provided")
        void updateUserBasicInfo_AllFields() {
            // Given
            User user = User.builder()
                    .firstName("Original")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "NewFirst", "NewLast", true);

            // Then
            assertThat(user.getFirstName()).isEqualTo("NewFirst");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only firstName when others are null")
        void updateUserBasicInfo_OnlyFirstName() {
            // Given
            User user = User.builder()
                    .firstName("Original")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "NewFirst", null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("NewFirst");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only lastName when others are null")
        void updateUserBasicInfo_OnlyLastName() {
            // Given
            User user = User.builder()
                    .firstName("Original")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, "NewLast", null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Original");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only isActive when others are null")
        void updateUserBasicInfo_OnlyIsActive() {
            // Given
            User user = User.builder()
                    .firstName("Original")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, null, true);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Original");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should not change any field when all are null")
        void updateUserBasicInfo_AllNull() {
            // Given
            User user = User.builder()
                    .firstName("Original")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Original");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAuthenticated Tests")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("Should not throw when user is authenticated")
        void isAuthenticated_Success() {
            // Given
            setCurrentUser(testUser);

            // When & Then - should not throw
            userService.isAuthenticated("John.Doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when currentUser is null")
        void isAuthenticated_NullCurrentUser() {
            // Given - no currentUser set

            // When & Then
            assertThatThrownBy(() -> userService.isAuthenticated("John.Doe"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("User is not authenticated: John.Doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when username doesn't match")
        void isAuthenticated_DifferentUsername() {
            // Given
            setCurrentUser(testUser);

            // When & Then
            assertThatThrownBy(() -> userService.isAuthenticated("Different.User"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("User is not authenticated: Different.User");
        }

        @Test
        @DisplayName("Should be case sensitive for username")
        void isAuthenticated_CaseSensitive() {
            // Given
            setCurrentUser(testUser);

            // When & Then
            assertThatThrownBy(() -> userService.isAuthenticated("john.doe"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("User is not authenticated: john.doe");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle user with empty names")
        void createUser_EmptyNames() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn(".");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            // When
            User result = userService.createUser("", "");

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEmpty();
            assertThat(userCaptor.getValue().getLastName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in names")
        void createUser_SpecialCharacters() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("O'Brien.McDonald");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.createUser("O'Brien", "McDonald");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("O'Brien");
        }

        @Test
        @DisplayName("Should handle password with special characters")
        void authenticate_PasswordWithSpecialChars() {
            // Given
            testUser.setPassword("P@ss!w0rd#$%");
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When
            userService.authenticate("John.Doe", "P@ss!w0rd#$%");

            // Then
            verify(userMetrics).incrementLoginSuccess();
        }

        @Test
        @DisplayName("Should handle changing password to same password")
        void changePassword_SamePassword() {
            // Given
            setCurrentUser(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.changePassword("John.Doe", "password123", "password123");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("password123");
            verify(userMetrics).incrementPasswordChanges();
        }

        @Test
        @DisplayName("Should handle very long username")
        void findByUsername_LongUsername() {
            // Given
            String longUsername = "A".repeat(255) + ".User";
            when(userRepository.findByUsername(longUsername))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findByUsername(longUsername))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Integration Scenarios Tests")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("Should create user and then authenticate")
        void createAndAuthenticate() {
            // Given - Create
            when(passwordGenerator.generatePassword()).thenReturn("generatedPass");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("John.Doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));

            // When - Create
            userService.createUser("John", "Doe");

            // When - Authenticate
            testUser.setPassword("generatedPass");
            userService.authenticate("John.Doe", "generatedPass");

            // Then
            verify(userMetrics).incrementRegistrations();
            verify(userMetrics).incrementLoginSuccess();
        }

        @Test
        void authenticateAndChangePassword() {
            // Given
            String username = "John.Doe";
            String oldPassword = "password123";
            String newPassword = "newPassword456";

            // ✅ FIXED: Use testUser instead of undefined 'user'
            when(userRepository.findByUsername(username))
                    .thenReturn(Optional.of(testUser));

            // When - First authentication (explicit login)
            userService.authenticate(username, oldPassword);

            // Then - First success counted
            verify(userMetrics).incrementLoginSuccess();

            // When - Change password (includes re-authentication)
            userService.changePassword(username, oldPassword, newPassword);

            // Then - Second success counted (re-auth during password change)
            verify(userMetrics, times(2)).incrementLoginSuccess(); // Expect 2 total
            verify(userMetrics).incrementPasswordChanges();
            verify(userRepository).save(any(User.class));
        }
        @Test
        @DisplayName("Should authenticate and then update status")
        void authenticateAndUpdateStatus() {
            // Given
            when(userRepository.findByUsername("John.Doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When - Authenticate
            userService.authenticate("John.Doe", "password123");

            // When - Update status
            userService.setActiveStatus("John.Doe", false);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }
    }
}