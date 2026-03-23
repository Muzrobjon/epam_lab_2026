package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
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

import java.lang.reflect.Field;
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

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();
    }

    /**
     * Helper method to set the currentUser field via reflection
     */
    private void setCurrentUser(User user) throws Exception {
        Field currentUserField = UserService.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(userService, user);
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_WithValidData_ReturnsCreatedUser() {
            // Arrange
            String generatedPassword = "generatedPass123";
            String generatedUsername = "john.doe";

            when(passwordGenerator.generatePassword()).thenReturn(generatedPassword);
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(generatedUsername);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            // Act
            User result = userService.createUser(FIRST_NAME, LAST_NAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(result.getLastName()).isEqualTo(LAST_NAME);
            assertThat(result.getUsername()).isEqualTo(generatedUsername);
            assertThat(result.getPassword()).isEqualTo(generatedPassword);
            assertThat(result.getIsActive()).isTrue();

            verify(passwordGenerator).generatePassword();
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getFirstName()).isEqualTo(FIRST_NAME);
            assertThat(capturedUser.getLastName()).isEqualTo(LAST_NAME);
        }

        @Test
        @DisplayName("Should set isActive to true by default")
        void createUser_SetsIsActiveTrue() {
            // Arrange
            when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(USERNAME);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User result = userService.createUser(FIRST_NAME, LAST_NAME);

            // Assert
            assertThat(result.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should use generated username from UsernameGenerator")
        void createUser_UsesGeneratedUsername() {
            // Arrange
            String uniqueUsername = "john.doe1";
            when(passwordGenerator.generatePassword()).thenReturn(PASSWORD);
            when(usernameGenerator.generateUsername(any(User.class), any())).thenReturn(uniqueUsername);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            User result = userService.createUser(FIRST_NAME, LAST_NAME);

            // Assert
            assertThat(result.getUsername()).isEqualTo(uniqueUsername);
        }
    }

    @Nested
    @DisplayName("findByUsername Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when found")
        void findByUsername_UserExists_ReturnsUser() {
            // Arrange
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // Act
            User result = userService.findByUsername(USERNAME);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(USERNAME);
            assertThat(result.getFirstName()).isEqualTo(FIRST_NAME);
            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void findByUsername_UserNotExists_ThrowsNotFoundException() {
            // Arrange
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found: nonexistent");

            verify(userRepository).findByUsername("nonexistent");
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate successfully with valid credentials")
        void authenticate_WithValidCredentials_Success() throws Exception {
            // Arrange
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // Act
            userService.authenticate(USERNAME, PASSWORD);

            // Assert - no exception thrown
            verify(userRepository).findByUsername(USERNAME);

            // Verify currentUser is set
            Field currentUserField = UserService.class.getDeclaredField("currentUser");
            currentUserField.setAccessible(true);
            User currentUser = (User) currentUserField.get(userService);
            assertThat(currentUser).isEqualTo(testUser);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when username not found")
        void authenticate_WithInvalidUsername_ThrowsAuthenticationException() {
            // Arrange
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.authenticate("invalid", PASSWORD))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository).findByUsername("invalid");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is wrong")
        void authenticate_WithWrongPassword_ThrowsAuthenticationException() {
            // Arrange
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.authenticate(USERNAME, "wrongPassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw AuthenticationException when both credentials are wrong")
        void authenticate_WithBothCredentialsWrong_ThrowsAuthenticationException() {
            // Arrange
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.authenticate("wrong", "wrong"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_WithValidOldPassword_Success() throws Exception {
            // Arrange
            String newPassword = "newPassword456";
            setCurrentUser(testUser);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.changePassword(USERNAME, PASSWORD, newPassword);

            // Assert
            verify(userRepository, times(2)).findByUsername(USERNAME);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("Should throw exception when user not authenticated")
        void changePassword_UserNotAuthenticated_ThrowsException() {
            // Arrange - currentUser is null by default

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(USERNAME, PASSWORD, "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when old password is wrong")
        void changePassword_WithWrongOldPassword_ThrowsException() throws Exception {
            // Arrange
            setCurrentUser(testUser);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(USERNAME, "wrongOldPass", "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when authenticated as different user")
        void changePassword_AuthenticatedAsDifferentUser_ThrowsException() throws Exception {
            // Arrange
            User differentUser = User.builder()
                    .id(2L)
                    .username("different.user")
                    .password("pass")
                    .build();
            setCurrentUser(differentUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.changePassword(USERNAME, PASSWORD, "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("setActiveStatus Tests")
    class SetActiveStatusTests {

        @Test
        @DisplayName("Should activate user successfully")
        void setActiveStatus_ActivateUser_Success() throws Exception {
            // Arrange
            testUser.setIsActive(false);
            setCurrentUser(testUser);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.setActiveStatus(USERNAME, true);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate user successfully")
        void setActiveStatus_DeactivateUser_Success() throws Exception {
            // Arrange
            setCurrentUser(testUser);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // Act
            userService.setActiveStatus(USERNAME, false);

            // Assert
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when user not authenticated")
        void setActiveStatus_UserNotAuthenticated_ThrowsException() {
            // Arrange - currentUser is null by default

            // Act & Assert
            assertThatThrownBy(() -> userService.setActiveStatus(USERNAME, true))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void setActiveStatus_UserNotFound_ThrowsException() throws Exception {
            // Arrange
            setCurrentUser(testUser);
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.setActiveStatus(USERNAME, true))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateUserBasicInfo Tests")
    class UpdateUserBasicInfoTests {

        @Test
        @DisplayName("Should update all fields when all provided")
        void updateUserBasicInfo_AllFieldsProvided_UpdatesAll() {
            // Arrange
            User user = User.builder()
                    .firstName("Old")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // Act
            userService.updateUserBasicInfo(user, "New", "LastName", true);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("New");
            assertThat(user.getLastName()).isEqualTo("LastName");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only firstName when others are null")
        void updateUserBasicInfo_OnlyFirstName_UpdatesFirstName() {
            // Arrange
            User user = User.builder()
                    .firstName("Old")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // Act
            userService.updateUserBasicInfo(user, "New", null, null);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("New");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only lastName when others are null")
        void updateUserBasicInfo_OnlyLastName_UpdatesLastName() {
            // Arrange
            User user = User.builder()
                    .firstName("Old")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // Act
            userService.updateUserBasicInfo(user, null, "NewLast", null);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("Old");
            assertThat(user.getLastName()).isEqualTo("NewLast");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only isActive when others are null")
        void updateUserBasicInfo_OnlyIsActive_UpdatesIsActive() {
            // Arrange
            User user = User.builder()
                    .firstName("Old")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // Act
            userService.updateUserBasicInfo(user, null, null, true);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("Old");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should not update anything when all null")
        void updateUserBasicInfo_AllNull_NoUpdates() {
            // Arrange
            User user = User.builder()
                    .firstName("Old")
                    .lastName("Name")
                    .isActive(false)
                    .build();

            // Act
            userService.updateUserBasicInfo(user, null, null, null);

            // Assert
            assertThat(user.getFirstName()).isEqualTo("Old");
            assertThat(user.getLastName()).isEqualTo("Name");
            assertThat(user.getIsActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("isAuthenticated Tests")
    class IsAuthenticatedTests {

        @Test
        @DisplayName("Should pass when user is authenticated")
        void isAuthenticated_UserAuthenticated_NoException() throws Exception {
            // Arrange
            setCurrentUser(testUser);

            // Act & Assert - no exception
            userService.isAuthenticated(USERNAME);
        }

        @Test
        @DisplayName("Should throw exception when currentUser is null")
        void isAuthenticated_CurrentUserNull_ThrowsException() {
            // Arrange - currentUser is null by default

            // Act & Assert
            assertThatThrownBy(() -> userService.isAuthenticated(USERNAME))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated: " + USERNAME);
        }

        @Test
        @DisplayName("Should throw exception when username doesn't match")
        void isAuthenticated_UsernameMismatch_ThrowsException() throws Exception {
            // Arrange
            setCurrentUser(testUser);

            // Act & Assert
            assertThatThrownBy(() -> userService.isAuthenticated("different.user"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessageContaining("User is not authenticated: different.user");
        }
    }
}