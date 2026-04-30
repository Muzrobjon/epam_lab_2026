package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.metrics.UserMetrics;
import com.epam.gym.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UsernameGenerator usernameGenerator;
    @Mock private PasswordService passwordService;
    @Mock private UserMetrics userMetrics;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john.doe")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword")
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(String username) {
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    // ============ createUser ============

    @Test
    @DisplayName("createUser: creates user with random password and returns raw password")
    @SuppressWarnings("unchecked")
    void createUser_success() {
        when(passwordService.generateRandomPassword()).thenReturn("rawPwd");
        when(passwordService.encodePassword("rawPwd")).thenReturn("encodedPwd");
        when(usernameGenerator.generateUsername(any(User.class), any(Function.class)))
                .thenReturn("john.doe");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User result = userService.createUser("John", "Doe");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("john.doe");
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getIsActive()).isTrue();

        // Returned user has raw password
        assertThat(result.getPassword()).isEqualTo("rawPwd");
        verify(userMetrics).incrementRegistrations();
    }

    // ============ findByUsername ============

    @Test
    @DisplayName("findByUsername: returns user when found")
    void findByUsername_found() {
        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("john.doe");

        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("findByUsername: throws NotFoundException when not found")
    void findByUsername_notFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found: unknown");
    }

    // ============ changePassword ============

    @Test
    @DisplayName("changePassword: success")
    void changePassword_success() {
        setAuthenticatedUser("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordService.matches("oldPwd", "encodedPassword")).thenReturn(true);
        when(passwordService.isPasswordStrong("NewStrongPwd1!")).thenReturn(true);
        when(passwordService.encodePassword("NewStrongPwd1!")).thenReturn("newEncoded");

        userService.changePassword("john.doe", "oldPwd", "NewStrongPwd1!");

        assertThat(user.getPassword()).isEqualTo("newEncoded");
        verify(userRepository).save(user);
        verify(userMetrics).incrementPasswordChanges();
    }

    @Test
    @DisplayName("changePassword: throws AuthenticationException on wrong old password")
    void changePassword_wrongOldPassword() {
        setAuthenticatedUser("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordService.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john.doe", "wrong", "NewPwd1!"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid old password");

        verify(userMetrics).incrementLoginFailure();
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: throws IllegalArgumentException for weak password")
    void changePassword_weakPassword() {
        setAuthenticatedUser("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));
        when(passwordService.matches("oldPwd", "encodedPassword")).thenReturn(true);
        when(passwordService.isPasswordStrong("weak")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("john.doe", "oldPwd", "weak"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 8 characters");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("changePassword: throws AuthenticationException when not authenticated")
    void changePassword_notAuthenticated() {
        setAuthenticatedUser("different.user");

        assertThatThrownBy(() -> userService.changePassword("john.doe", "old", "new"))
                .isInstanceOf(AuthenticationException.class);

        verify(userRepository, never()).save(any());
    }

    // ============ setActiveStatus ============

    @Test
    @DisplayName("setActiveStatus: updates status when authenticated")
    void setActiveStatus_success() {
        setAuthenticatedUser("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.of(user));

        userService.setActiveStatus("john.doe", false);

        assertThat(user.getIsActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("setActiveStatus: throws when user not found")
    void setActiveStatus_userNotFound() {
        setAuthenticatedUser("john.doe");

        when(userRepository.findByUsername("john.doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.setActiveStatus("john.doe", true))
                .isInstanceOf(NotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("setActiveStatus: throws AuthenticationException when not authenticated")
    void setActiveStatus_notAuthenticated() {
        setAuthenticatedUser("other.user");

        assertThatThrownBy(() -> userService.setActiveStatus("john.doe", true))
                .isInstanceOf(AuthenticationException.class);
    }

    // ============ updateUserBasicInfo ============

    @Test
    @DisplayName("updateUserBasicInfo: updates all fields when provided")
    void updateUserBasicInfo_allFields() {
        userService.updateUserBasicInfo(user, "NewFirst", "NewLast", false);

        assertThat(user.getFirstName()).isEqualTo("NewFirst");
        assertThat(user.getLastName()).isEqualTo("NewLast");
        assertThat(user.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("updateUserBasicInfo: keeps fields when nulls passed")
    void updateUserBasicInfo_nullValuesIgnored() {
        userService.updateUserBasicInfo(user, null, null, null);

        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("updateUserBasicInfo: partial update")
    void updateUserBasicInfo_partial() {
        userService.updateUserBasicInfo(user, "NewFirst", null, null);

        assertThat(user.getFirstName()).isEqualTo("NewFirst");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getIsActive()).isTrue();
    }

    // ============ isAuthenticated ============

    @Test
    @DisplayName("isAuthenticated: passes when username matches")
    void isAuthenticated_success() {
        setAuthenticatedUser("john.doe");

        assertThatCode(() -> userService.isAuthenticated("john.doe"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("isAuthenticated: throws when username mismatch")
    void isAuthenticated_mismatch() {
        setAuthenticatedUser("other.user");

        assertThatThrownBy(() -> userService.isAuthenticated("john.doe"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("User is not authenticated");
    }

    @Test
    @DisplayName("isAuthenticated: throws NullPointerException when no authentication")
    void isAuthenticated_noAuthentication() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> userService.isAuthenticated("john.doe"))
                .isInstanceOf(NullPointerException.class);
    }
}