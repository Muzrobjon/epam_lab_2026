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

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Captor
    private ArgumentCaptor<Function<String, Boolean>> functionCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .username("john.doe")
                .password("password123")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("generatedPass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("john.doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            User result = userService.createUser("John", "Doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john.doe");

            verify(passwordGenerator).generatePassword();
            verify(usernameGenerator).generateUsername(any(User.class), any());
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getFirstName()).isEqualTo("John");
            assertThat(capturedUser.getLastName()).isEqualTo("Doe");
            assertThat(capturedUser.getPassword()).isEqualTo("generatedPass123");
            assertThat(capturedUser.getIsActive()).isTrue();
            assertThat(capturedUser.getUsername()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should set user as active by default")
        void shouldSetUserAsActiveByDefault() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("jane.smith");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            userService.createUser("Jane", "Smith");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should pass existsByUsername function to username generator")
        void shouldPassExistsByUsernameFunctionToUsernameGenerator() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("john.doe");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userRepository.existsByUsername("test.user")).thenReturn(true);
            when(userRepository.existsByUsername("new.user")).thenReturn(false);

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(usernameGenerator).generateUsername(any(User.class), functionCaptor.capture());

            Function<String, Boolean> capturedFunction = functionCaptor.getValue();

            // Test that the captured function correctly delegates to repository
            assertThat(capturedFunction.apply("test.user")).isTrue();
            assertThat(capturedFunction.apply("new.user")).isFalse();

            verify(userRepository).existsByUsername("test.user");
            verify(userRepository).existsByUsername("new.user");
        }

        @Test
        @DisplayName("Should generate unique username with serial number")
        void shouldGenerateUniqueUsernameWithSerialNumber() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("john.doe1");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getUsername()).isEqualTo("john.doe1");
        }

        @Test
        @DisplayName("Should use generated password from PasswordGenerator")
        void shouldUseGeneratedPasswordFromPasswordGenerator() {
            // Given
            String generatedPassword = "SecureP@ss123!";
            when(passwordGenerator.generatePassword()).thenReturn(generatedPassword);
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("john.doe");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            userService.createUser("John", "Doe");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo(generatedPassword);
        }
    }

    @Nested
    @DisplayName("findByUsername Tests")
    class FindByUsernameTests {

        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When
            User result = userService.findByUsername("john.doe");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john.doe");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            verify(userRepository).findByUsername("john.doe");
        }

        @Test
        @DisplayName("Should throw NotFoundException when user not found")
        void shouldThrowNotFoundExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.findByUsername("nonexistent"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found: nonexistent");
        }
    }

    @Nested
    @DisplayName("authenticate Tests")
    class AuthenticateTests {

        @Test
        @DisplayName("Should authenticate successfully with correct credentials")
        void shouldAuthenticateSuccessfullyWithCorrectCredentials() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatCode(() -> userService.authenticate("john.doe", "password123"))
                    .doesNotThrowAnyException();

            verify(userRepository).findByUsername("john.doe");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user not found")
        void shouldThrowAuthenticationExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("nonexistent", "password123"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrect() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("john.doe", "wrongpassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should not reveal whether username or password is wrong")
        void shouldNotRevealWhetherUsernameOrPasswordIsWrong() {
            // Given
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then - Both should have the same error message
            assertThatThrownBy(() -> userService.authenticate("nonexistent", "password123"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            assertThatThrownBy(() -> userService.authenticate("john.doe", "wrongpassword"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");
        }

        @Test
        @DisplayName("Should authenticate with case-sensitive password")
        void shouldAuthenticateWithCaseSensitivePassword() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.authenticate("john.doe", "PASSWORD123"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.changePassword("john.doe", "password123", "newPassword456");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("newPassword456");
        }

        @Test
        @DisplayName("Should throw AuthenticationException when old password is incorrect")
        void shouldThrowAuthenticationExceptionWhenOldPasswordIsIncorrect() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.changePassword("john.doe", "wrongOldPassword", "newPassword456"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user not found")
        void shouldThrowAuthenticationExceptionWhenUserNotFoundOnChangePassword() {
            // Given
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.changePassword("nonexistent", "oldPass", "newPass"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow changing to same password")
        void shouldAllowChangingToSamePassword() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.changePassword("john.doe", "password123", "password123");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getPassword()).isEqualTo("password123");
        }
    }

    @Nested
    @DisplayName("setActiveStatus Tests")
    class SetActiveStatusTests {

        @Test
        @DisplayName("Should set active status to true")
        void shouldSetActiveStatusToTrue() {
            // Given
            testUser.setIsActive(false);
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.setActiveStatus("john.doe", "password123", true);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should set active status to false")
        void shouldSetActiveStatusToFalse() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.setActiveStatus("john.doe", "password123", false);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw AuthenticationException when password is incorrect")
        void shouldThrowAuthenticationExceptionWhenPasswordIsIncorrectOnSetActiveStatus() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus("john.doe", "wrongpassword", true))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw AuthenticationException when user not found")
        void shouldThrowAuthenticationExceptionWhenUserNotFoundOnSetActiveStatus() {
            // Given
            when(userRepository.findByUsername("nonexistent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> userService.setActiveStatus("nonexistent", "password", true))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage("Invalid username or password");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should allow setting same active status")
        void shouldAllowSettingSameActiveStatus() {
            // Given
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            userService.setActiveStatus("john.doe", "password123", true);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("updateUserBasicInfo Tests")
    class UpdateUserBasicInfoTests {

        @Test
        @DisplayName("Should update all fields when all provided")
        void shouldUpdateAllFieldsWhenAllProvided() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "Jane", "Smith", false);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update only firstName when others are null")
        void shouldUpdateOnlyFirstNameWhenOthersAreNull() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "Jane", null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only lastName when others are null")
        void shouldUpdateOnlyLastNameWhenOthersAreNull() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, "Smith", null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update only isActive when others are null")
        void shouldUpdateOnlyIsActiveWhenOthersAreNull() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, null, false);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should not update any field when all are null")
        void shouldNotUpdateAnyFieldWhenAllAreNull() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(true)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, null, null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update firstName and lastName only")
        void shouldUpdateFirstNameAndLastNameOnly() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "Jane", "Smith", null);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should update firstName and isActive only")
        void shouldUpdateFirstNameAndIsActiveOnly() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, "Jane", null, true);

            // Then
            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should update lastName and isActive only")
        void shouldUpdateLastNameAndIsActiveOnly() {
            // Given
            User user = User.builder()
                    .id(1L)
                    .firstName("John")
                    .lastName("Doe")
                    .isActive(false)
                    .build();

            // When
            userService.updateUserBasicInfo(user, null, "Smith", true);

            // Then
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Smith");
            assertThat(user.getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty password during authentication")
        void shouldHandleEmptyPasswordDuringAuthentication() {
            // Given
            testUser.setPassword("");
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatCode(() -> userService.authenticate("john.doe", ""))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle special characters in password")
        void shouldHandleSpecialCharactersInPassword() {
            // Given
            String specialPassword = "P@ss!w0rd#$%^&*()";
            testUser.setPassword(specialPassword);
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatCode(() -> userService.authenticate("john.doe", specialPassword))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should handle unicode characters in names")
        void shouldHandleUnicodeCharactersInNames() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("josé.müller");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            userService.createUser("José", "Müller");

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo("José");
            assertThat(userCaptor.getValue().getLastName()).isEqualTo("Müller");
        }

        @Test
        @DisplayName("Should handle very long names")
        void shouldHandleVeryLongNames() {
            // Given
            String longFirstName = "A".repeat(100);
            String longLastName = "B".repeat(100);

            when(passwordGenerator.generatePassword()).thenReturn("pass123");
            when(usernameGenerator.generateUsername(any(User.class), any()))
                    .thenReturn("long.name");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            userService.createUser(longFirstName, longLastName);

            // Then
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getFirstName()).isEqualTo(longFirstName);
            assertThat(userCaptor.getValue().getLastName()).isEqualTo(longLastName);
        }

        @Test
        @DisplayName("Should handle whitespace in password")
        void shouldHandleWhitespaceInPassword() {
            // Given
            String passwordWithSpaces = "pass word 123";
            testUser.setPassword(passwordWithSpaces);
            when(userRepository.findByUsername("john.doe"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatCode(() -> userService.authenticate("john.doe", passwordWithSpaces))
                    .doesNotThrowAnyException();

            assertThatThrownBy(() -> userService.authenticate("john.doe", "password123"))
                    .isInstanceOf(AuthenticationException.class);
        }
    }
}