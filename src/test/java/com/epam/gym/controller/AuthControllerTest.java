package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.dto.response.MessageResponse;
import com.epam.gym.dto.response.UserInfoResponse;
import com.epam.gym.exception.AccountLockedException;
import com.epam.gym.exception.BadLoginException;
import com.epam.gym.exception.LogoutException;
import com.epam.gym.security.JwtProvider;
import com.epam.gym.security.JwtTokenExtractor;
import com.epam.gym.security.LoginAttemptService;
import com.epam.gym.security.TokenBlacklistService;
import com.epam.gym.security.UserPrincipal;
import com.epam.gym.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private JwtTokenExtractor jwtTokenExtractor;

    @InjectMocks
    private AuthController authController;

    private static final String USERNAME = "john.doe";
    private static final String PASSWORD = "Password@123";
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.token";
    private static final int BLOCK_DURATION = 15;

    private UserPrincipal userPrincipal;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        userPrincipal = UserPrincipal.builder()
                .id(1L)
                .username(USERNAME)
                .password(PASSWORD)
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setUsername(USERNAME);
        loginRequest.setPassword(PASSWORD);
    }

    // ==================== LOGIN TESTS ====================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully and return JWT token")
        void login_Success() {
            // Given
            Authentication authentication = mock(Authentication.class);

            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // When
            ResponseEntity<LoginResponse> response = authController.login(loginRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            LoginResponse loginResponse = response.getBody();
            assertThat(loginResponse.getAccessToken()).isEqualTo(JWT_TOKEN);
            assertThat(loginResponse.getTokenType()).isEqualTo("Bearer");
            assertThat(loginResponse.getUsername()).isEqualTo(USERNAME);

            verify(loginAttemptService).isBlocked(USERNAME);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtProvider).generateToken(authentication);
            verify(loginAttemptService).loginSucceeded(USERNAME);
        }

        @Test
        @DisplayName("Should throw AccountLockedException when user is blocked")
        void login_UserBlocked_ThrowsAccountLockedException() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(true);
            when(loginAttemptService.getBlockDurationMinutes()).thenReturn(BLOCK_DURATION);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(AccountLockedException.class);

            verify(loginAttemptService).isBlocked(USERNAME);
            verify(loginAttemptService).getBlockDurationMinutes();
            verify(authenticationManager, never()).authenticate(any());
            verify(loginAttemptService, never()).loginFailed(anyString());
        }

        @Test
        @DisplayName("Should throw BadLoginException on BadCredentialsException")
        void login_BadCredentials_ThrowsBadLoginException() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(2);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class)
                    .hasMessageContaining("Username or password is incorrect")
                    .satisfies(ex -> {
                        BadLoginException badLogin = (BadLoginException) ex;
                        assertThat(badLogin.getRemainingAttempts()).isEqualTo(2);
                        assertThat(badLogin.getBlockDurationMinutes()).isNull();
                    });

            verify(loginAttemptService).loginFailed(USERNAME);
            verify(loginAttemptService).getRemainingAttempts(USERNAME);
            verify(jwtProvider, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should include block duration when remaining attempts is 0")
        void login_NoRemainingAttempts_IncludesBlockDuration() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(0);
            when(loginAttemptService.getBlockDurationMinutes()).thenReturn(BLOCK_DURATION);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class)
                    .satisfies(ex -> {
                        BadLoginException badLogin = (BadLoginException) ex;
                        assertThat(badLogin.getRemainingAttempts()).isEqualTo(0);
                        assertThat(badLogin.getBlockDurationMinutes()).isEqualTo(BLOCK_DURATION);
                    });

            verify(loginAttemptService).loginFailed(USERNAME);
            verify(loginAttemptService).getBlockDurationMinutes();
        }

        @Test
        @DisplayName("Should handle DisabledException")
        void login_DisabledAccount_ThrowsBadLoginException() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("Account is disabled"));
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(1);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class)
                    .hasMessageContaining("Username or password is incorrect");

            verify(loginAttemptService).loginFailed(USERNAME);
        }

        @Test
        @DisplayName("Should handle LockedException")
        void login_LockedAccount_ThrowsBadLoginException() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new LockedException("Account is locked"));
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(1);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class)
                    .hasMessageContaining("Username or password is incorrect");

            verify(loginAttemptService).loginFailed(USERNAME);
        }

        @Test
        @DisplayName("Should pass correct credentials to AuthenticationManager")
        void login_PassesCorrectCredentials() {
            // Given
            Authentication authentication = mock(Authentication.class);

            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // When
            authController.login(loginRequest);

            // Then
            verify(authenticationManager).authenticate(
                    argThat(auth -> {
                        UsernamePasswordAuthenticationToken token =
                                (UsernamePasswordAuthenticationToken) auth;
                        return USERNAME.equals(token.getPrincipal())
                                && PASSWORD.equals(token.getCredentials());
                    })
            );
        }

        @Test
        @DisplayName("Should handle ClassCastException when principal is not UserPrincipal")
        void login_InvalidPrincipalType_ThrowsClassCastException() {
            // Given
            Authentication authentication = mock(Authentication.class);
            String invalidPrincipal = "not-a-user-principal";

            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(invalidPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(ClassCastException.class);

            // The controller calls loginSucceeded BEFORE casting, so it should be called
            verify(loginAttemptService).loginSucceeded(USERNAME);
        }

        @Test
        @DisplayName("Should reset login attempts on successful authentication")
        void login_Success_ResetsLoginAttempts() {
            // Given
            Authentication authentication = mock(Authentication.class);

            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // When
            authController.login(loginRequest);

            // Then
            verify(loginAttemptService).loginSucceeded(USERNAME);
        }

        @Test
        @DisplayName("Should handle generic AuthenticationException")
        void login_GenericAuthenticationException_ThrowsBadLoginException() {
            // Given
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new AuthenticationException("Generic auth error") {});
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(1);

            // When & Then
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class)
                    .hasMessageContaining("Username or password is incorrect");

            verify(loginAttemptService).loginFailed(USERNAME);
        }
    }

    // ==================== LOGOUT TESTS ====================

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Mock
        private HttpServletRequest httpServletRequest;

        @Test
        @DisplayName("Should logout successfully with valid token")
        void logout_WithValidToken_Success() {
            // Given
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(JWT_TOKEN);
            when(jwtProvider.getExpirationFromToken(JWT_TOKEN)).thenReturn(expiration);

            // When
            ResponseEntity<MessageResponse> response = authController.logout(httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("User logged out successfully");

            verify(jwtTokenExtractor).extract(httpServletRequest);
            verify(jwtProvider).getExpirationFromToken(JWT_TOKEN);
            verify(tokenBlacklistService).blacklistToken(JWT_TOKEN, expiration);
        }

        @Test
        @DisplayName("Should return OK when no token present")
        void logout_WithoutToken_ReturnsOk() {
            // Given
            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(null);

            // When
            ResponseEntity<MessageResponse> response = authController.logout(httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("User logged out successfully");

            verify(jwtProvider, never()).getExpirationFromToken(anyString());
            verify(tokenBlacklistService, never()).blacklistToken(anyString(), any());
        }

        @Test
        @DisplayName("Should throw LogoutException when token processing fails")
        void logout_TokenProcessingFails_ThrowsLogoutException() {
            // Given
            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(JWT_TOKEN);
            when(jwtProvider.getExpirationFromToken(JWT_TOKEN))
                    .thenThrow(new RuntimeException("Invalid token"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(httpServletRequest))
                    .isInstanceOf(LogoutException.class)
                    .hasMessageContaining("Failed to process logout")
                    .hasMessageContaining("Invalid token");

            verify(tokenBlacklistService, never()).blacklistToken(anyString(), any());
        }

        @Test
        @DisplayName("Should throw LogoutException when blacklisting fails")
        void logout_BlacklistingFails_ThrowsLogoutException() {
            // Given
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(JWT_TOKEN);
            when(jwtProvider.getExpirationFromToken(JWT_TOKEN)).thenReturn(expiration);
            doThrow(new RuntimeException("Blacklist error"))
                    .when(tokenBlacklistService).blacklistToken(JWT_TOKEN, expiration);

            // When & Then
            assertThatThrownBy(() -> authController.logout(httpServletRequest))
                    .isInstanceOf(LogoutException.class)
                    .hasMessageContaining("Failed to process logout")
                    .hasMessageContaining("Blacklist error");
        }

        @Test
        @DisplayName("Should handle expired token gracefully")
        void logout_WithExpiredToken_HandlesGracefully() {
            // Given
            LocalDateTime expiration = LocalDateTime.now().minusHours(1); // Already expired

            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(JWT_TOKEN);
            when(jwtProvider.getExpirationFromToken(JWT_TOKEN)).thenReturn(expiration);

            // When
            ResponseEntity<MessageResponse> response = authController.logout(httpServletRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getMessage()).isEqualTo("User logged out successfully");

            verify(tokenBlacklistService).blacklistToken(JWT_TOKEN, expiration);
        }

        @Test
        @DisplayName("Should handle empty token string")
        void logout_WithEmptyToken_ReturnsOk() {
            // Given - Empty string is treated as non-null, so it will try to process
            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn("");
            when(jwtProvider.getExpirationFromToken("")).thenThrow(new RuntimeException("Invalid token"));

            // When & Then - Empty token should cause LogoutException
            assertThatThrownBy(() -> authController.logout(httpServletRequest))
                    .isInstanceOf(LogoutException.class)
                    .hasMessageContaining("Failed to process logout");

            verify(jwtProvider).getExpirationFromToken("");
            verify(tokenBlacklistService, never()).blacklistToken(anyString(), any());
        }

        @Test
        @DisplayName("Should handle whitespace token")
        void logout_WithWhitespaceToken_ReturnsOk() {
            // Given - Whitespace string is treated as non-null
            String whitespaceToken = "   ";
            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(whitespaceToken);
            when(jwtProvider.getExpirationFromToken(whitespaceToken)).thenThrow(new RuntimeException("Invalid token"));

            // When & Then
            assertThatThrownBy(() -> authController.logout(httpServletRequest))
                    .isInstanceOf(LogoutException.class)
                    .hasMessageContaining("Failed to process logout");
        }
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        private ChangePasswordRequest changePasswordRequest;

        @BeforeEach
        void setUp() {
            changePasswordRequest = new ChangePasswordRequest();
            changePasswordRequest.setUsername(USERNAME);
            changePasswordRequest.setOldPassword("OldPass@123");
            changePasswordRequest.setNewPassword("NewPass@456");
        }

        @Test
        @DisplayName("Should change password successfully")
        void changePassword_Success() {
            // Given
            doNothing().when(userService).changePassword(
                    eq(USERNAME), eq("OldPass@123"), eq("NewPass@456")
            );

            // When
            ResponseEntity<MessageResponse> response =
                    authController.changePassword(changePasswordRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).isEqualTo("Password changed successfully");

            verify(userService).changePassword(USERNAME, "OldPass@123", "NewPass@456");
        }

        @Test
        @DisplayName("Should propagate exception when password change fails")
        void changePassword_ServiceThrows_PropagatesException() {
            // Given
            doThrow(new RuntimeException("Invalid old password"))
                    .when(userService).changePassword(anyString(), anyString(), anyString());

            // When & Then
            assertThatThrownBy(() -> authController.changePassword(changePasswordRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid old password");

            verify(userService).changePassword(USERNAME, "OldPass@123", "NewPass@456");
        }

        @Test
        @DisplayName("Should pass correct parameters to UserService")
        void changePassword_PassesCorrectParameters() {
            // Given
            doNothing().when(userService).changePassword(anyString(), anyString(), anyString());

            // When
            authController.changePassword(changePasswordRequest);

            // Then
            verify(userService).changePassword(
                    eq(USERNAME),
                    eq("OldPass@123"),
                    eq("NewPass@456")
            );
        }

        @Test
        @DisplayName("Should handle different username")
        void changePassword_DifferentUsername_Success() {
            // Given
            String differentUsername = "jane.doe";
            changePasswordRequest.setUsername(differentUsername);

            doNothing().when(userService).changePassword(anyString(), anyString(), anyString());

            // When
            ResponseEntity<MessageResponse> response =
                    authController.changePassword(changePasswordRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(userService).changePassword(differentUsername, "OldPass@123", "NewPass@456");
        }

        @Test
        @DisplayName("Should handle service throwing authentication exception")
        void changePassword_AuthenticationException_PropagatesException() {
            // Given
            doThrow(new com.epam.gym.exception.AuthenticationException("Wrong old password"))
                    .when(userService).changePassword(anyString(), anyString(), anyString());

            // When & Then
            assertThatThrownBy(() -> authController.changePassword(changePasswordRequest))
                    .isInstanceOf(com.epam.gym.exception.AuthenticationException.class)
                    .hasMessageContaining("Wrong old password");
        }
    }

    // ==================== GET CURRENT USER TESTS ====================

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should return current user info")
        void getCurrentUser_Success() {
            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(userPrincipal);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            UserInfoResponse userInfo = response.getBody();
            assertThat(userInfo.getId()).isEqualTo(1L);
            assertThat(userInfo.getUsername()).isEqualTo(USERNAME);
            assertThat(userInfo.getFirstName()).isEqualTo("John");
            assertThat(userInfo.getLastName()).isEqualTo("Doe");
            assertThat(userInfo.getAuthorities()).isNotEmpty();
            assertThat(userInfo.getAuthorities()).hasSize(1);
        }

        @Test
        @DisplayName("Should return correct authorities")
        void getCurrentUser_ReturnsCorrectAuthorities() {
            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(userPrincipal);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("Should handle user with multiple authorities")
        void getCurrentUser_MultipleAuthorities() {
            // Given
            UserPrincipal multiRoleUser = UserPrincipal.builder()
                    .id(2L)
                    .username("admin.user")
                    .firstName("Admin")
                    .lastName("User")
                    .isActive(true)
                    .authorities(Arrays.asList(
                            new SimpleGrantedAuthority("ROLE_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_TRAINER")
                    ))
                    .build();

            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(multiRoleUser);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAuthorities()).hasSize(3);
            assertThat(response.getBody().getAuthorities())
                    .extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN", "ROLE_TRAINER");
        }

        @Test
        @DisplayName("Should handle user with no authorities")
        void getCurrentUser_NoAuthorities() {
            // Given
            UserPrincipal noAuthUser = UserPrincipal.builder()
                    .id(3L)
                    .username("no.auth.user")
                    .firstName("No")
                    .lastName("Auth")
                    .isActive(true)
                    .authorities(Collections.emptyList())
                    .build();

            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(noAuthUser);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getAuthorities()).isEmpty();
        }

        @Test
        @DisplayName("Should return user info with correct mapping")
        void getCurrentUser_CorrectMapping() {
            // Given
            UserPrincipal customUser = UserPrincipal.builder()
                    .id(999L)
                    .username("custom.user")
                    .firstName("Custom")
                    .lastName("User")
                    .isActive(false)
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOM")))
                    .build();

            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(customUser);

            // Then
            UserInfoResponse userInfo = response.getBody();
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.getId()).isEqualTo(999L);
            assertThat(userInfo.getUsername()).isEqualTo("custom.user");
            assertThat(userInfo.getFirstName()).isEqualTo("Custom");
            assertThat(userInfo.getLastName()).isEqualTo("User");
            assertThat(userInfo.getAuthorities()).hasSize(1);
            assertThat(userInfo.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_CUSTOM");
        }

        @Test
        @DisplayName("Should not call any service methods")
        void getCurrentUser_NoServiceCalls() {
            // When
            authController.getCurrentUser(userPrincipal);

            // Then
            verifyNoInteractions(userService);
            verifyNoInteractions(authenticationManager);
            verifyNoInteractions(jwtProvider);
            verifyNoInteractions(loginAttemptService);
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        @DisplayName("Should handle null fields gracefully")
        void getCurrentUser_NullFields() {
            // Given
            UserPrincipal userWithNulls = UserPrincipal.builder()
                    .id(4L)
                    .username("null.fields.user")
                    .firstName(null)
                    .lastName(null)
                    .isActive(true)
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            // When
            ResponseEntity<UserInfoResponse> response =
                    authController.getCurrentUser(userWithNulls);

            // Then
            UserInfoResponse userInfo = response.getBody();
            assertThat(userInfo).isNotNull();
            assertThat(userInfo.getId()).isEqualTo(4L);
            assertThat(userInfo.getUsername()).isEqualTo("null.fields.user");
            assertThat(userInfo.getFirstName()).isNull();
            assertThat(userInfo.getLastName()).isNull();
        }
    }

    // ==================== INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete login-logout flow")
        void completeLoginLogoutFlow() {
            // Given - Login setup
            Authentication authentication = mock(Authentication.class);
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            LocalDateTime expiration = LocalDateTime.now().plusHours(1);

            // Mock login
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // Mock logout
            when(jwtTokenExtractor.extract(httpServletRequest)).thenReturn(JWT_TOKEN);
            when(jwtProvider.getExpirationFromToken(JWT_TOKEN)).thenReturn(expiration);

            // When - Login
            ResponseEntity<LoginResponse> loginResponse = authController.login(loginRequest);

            // Then - Verify login
            assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(loginResponse.getBody().getAccessToken()).isEqualTo(JWT_TOKEN);

            // When - Logout
            ResponseEntity<MessageResponse> logoutResponse = authController.logout(httpServletRequest);

            // Then - Verify logout
            assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(logoutResponse.getBody().getMessage()).isEqualTo("User logged out successfully");

            // Verify interactions
            verify(loginAttemptService).loginSucceeded(USERNAME);
            verify(tokenBlacklistService).blacklistToken(JWT_TOKEN, expiration);
        }

        @Test
        @DisplayName("Should handle failed login followed by successful login")
        void failedThenSuccessfulLogin() {
            // Given - First attempt (failure)
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));
            when(loginAttemptService.getRemainingAttempts(USERNAME)).thenReturn(2);

            // When - First attempt
            assertThatThrownBy(() -> authController.login(loginRequest))
                    .isInstanceOf(BadLoginException.class);

            // Given - Second attempt (success)
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // When - Second attempt
            ResponseEntity<LoginResponse> response = authController.login(loginRequest);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // Verify interactions
            verify(loginAttemptService).loginFailed(USERNAME);
            verify(loginAttemptService).loginSucceeded(USERNAME);
        }

        @Test
        @DisplayName("Should handle password change after successful login")
        void loginThenChangePassword() {
            // Given - Login
            Authentication authentication = mock(Authentication.class);
            when(loginAttemptService.isBlocked(USERNAME)).thenReturn(false);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userPrincipal);
            when(jwtProvider.generateToken(authentication)).thenReturn(JWT_TOKEN);

            // Given - Password change
            ChangePasswordRequest changeRequest = new ChangePasswordRequest();
            changeRequest.setUsername(USERNAME);
            changeRequest.setOldPassword(PASSWORD);
            changeRequest.setNewPassword("NewPassword@123");

            doNothing().when(userService).changePassword(anyString(), anyString(), anyString());

            // When - Login
            ResponseEntity<LoginResponse> loginResponse = authController.login(loginRequest);

            // When - Change password
            ResponseEntity<MessageResponse> changeResponse = authController.changePassword(changeRequest);

            // Then
            assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(changeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(changeResponse.getBody().getMessage()).isEqualTo("Password changed successfully");

            verify(loginAttemptService).loginSucceeded(USERNAME);
            verify(userService).changePassword(USERNAME, PASSWORD, "NewPassword@123");
        }
    }
}