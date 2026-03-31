package com.epam.gym.controller;


import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.security.*;
import com.epam.gym.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and password management APIs")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        log.info("Login attempt for user: {}", username);

        // Check if user is blocked
        if (loginAttemptService.isBlocked(username)) {
            int blockDuration = loginAttemptService.getBlockDurationMinutes();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Account is locked");
            errorResponse.put("message", String.format(
                    "Too many failed login attempts. Account is locked for %d minutes.",
                    blockDuration
            ));
            errorResponse.put("blockDurationMinutes", blockDuration);
            errorResponse.put("timestamp", LocalDateTime.now());

            log.warn("Login attempt for blocked user: {}", username);
            return ResponseEntity.status(HttpStatus.LOCKED).body(errorResponse);
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtProvider.generateToken(authentication);

            // Clear failed attempts on successful login
            loginAttemptService.loginSucceeded(username);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            assert userPrincipal != null;
            LoginResponse response = LoginResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .username(userPrincipal.getUsername())
                    .build();

            log.info("User {} logged in successfully", username);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // Record failed attempt
            loginAttemptService.loginFailed(username);

            int remainingAttempts = loginAttemptService.getRemainingAttempts(username);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            errorResponse.put("message", "Username or password is incorrect");
            errorResponse.put("remainingAttempts", remainingAttempts);
            errorResponse.put("timestamp", LocalDateTime.now());

            if (remainingAttempts == 0) {
                errorResponse.put("blocked", true);
                errorResponse.put("blockDurationMinutes", loginAttemptService.getBlockDurationMinutes());
            }

            log.warn("Login failed for user: {}. Remaining attempts: {}", username, remainingAttempts);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "Logout", description = "Logout current user and blacklist token")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);

        if (jwt != null) {
            try {
                LocalDateTime expiration = jwtProvider.getExpirationFromToken(jwt);
                tokenBlacklistService.blacklistToken(jwt, expiration);

                SecurityContextHolder.clearContext();

                log.info("User logged out successfully");

                Map<String, String> response = new HashMap<>();
                response.put("message", "Logged out successfully");
                response.put("timestamp", LocalDateTime.now().toString());
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                log.error("Error during logout: {}", e.getMessage());
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change password", description = "Change user password")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.getUsername());

        try {
            userService.changePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully");
            response.put("timestamp", LocalDateTime.now().toString());

            log.info("Password changed successfully for user: {}", request.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Password change failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", currentUser.getId());
        userInfo.put("username", currentUser.getUsername());
        userInfo.put("firstName", currentUser.getFirstName());
        userInfo.put("lastName", currentUser.getLastName());
        userInfo.put("isActive", currentUser.isActive());
        userInfo.put("authorities", currentUser.getAuthorities());

        return ResponseEntity.ok(userInfo);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}