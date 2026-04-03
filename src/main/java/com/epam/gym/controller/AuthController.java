package com.epam.gym.controller;


import com.epam.gym.dto.response.LoginResponse;
import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.response.MessageResponse;
import com.epam.gym.dto.response.UserInfoResponse;
import com.epam.gym.exception.AccountLockedException;
import com.epam.gym.exception.BadLoginException;
import com.epam.gym.exception.LogoutException;
import com.epam.gym.security.*;
import com.epam.gym.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;


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
    private final JwtTokenExtractor jwtTokenExtractor;


    // TODO:
    //  Overall the flow is good, but I would improve a few things:
    //  1) In stateless JWT login, setting SecurityContextHolder is usually unnecessary unless this request needs auth context later.
    //  2) authenticate(...) can throw more than BadCredentialsException, so handling AuthenticationException
    //  (or specific subtypes) would make the endpoint more robust.
    //  3) Returning Map<String, Object> and ResponseEntity<?> makes the API contract weak.
    //  centralized exception handling would be cleaner.
    //  4) assert on principal is not a reliable runtime check and can be removed: a) assertions can be disabled in prod
    //  and b) after successful authentication, principal should be present.
    @Operation(summary = "User login", description = "Authenticate user and get JWT token")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();
        log.info("Login attempt for user: {}", username);

        // Check if user is blocked
        if (loginAttemptService.isBlocked(username)) {
            log.warn("Login attempt for blocked user: {}", username);
            throw new AccountLockedException(loginAttemptService.getBlockDurationMinutes());
        }

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );


            // Generate JWT token
            String jwt = jwtProvider.generateToken(authentication);

            // Clear failed attempts on successful login
            loginAttemptService.loginSucceeded(username);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            LoginResponse response = LoginResponse.builder()
                    .accessToken(jwt)
                    .tokenType("Bearer")
                    .username(userPrincipal.getUsername())
                    .build();

            log.info("User {} logged in successfully", username);
            return ResponseEntity.ok(response);

        } catch (org.springframework.security.core.AuthenticationException e) {
            // Record failed attempt
            loginAttemptService.loginFailed(username);

            int remainingAttempts = loginAttemptService.getRemainingAttempts(username);

            log.warn("Login failed for user: {}. Remaining attempts: {}", username, remainingAttempts);

            throw new BadLoginException(
                    "Username or password is incorrect",
                    remainingAttempts,
                    remainingAttempts == 0
                        ? loginAttemptService.getBlockDurationMinutes()
                            :null
            );
        }
    }

    @Operation(summary = "Logout", description = "Logout current user and blacklist token")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        String jwt = jwtTokenExtractor.extract(request);

        if (jwt != null) {
            try {
                LocalDateTime expiration = jwtProvider.getExpirationFromToken(jwt);
                tokenBlacklistService.blacklistToken(jwt, expiration);
                log.info("User logged out successfully");
            } catch (Exception e) {
                log.error("Error during logout: {}", e.getMessage());
                throw new LogoutException("Failed to process logout" + e.getMessage());
            }
        }

        // TODO:
        //  If an error was caught in try-catch block do we still respond with 200 "Logged out successfully"?
        return ResponseEntity.ok(new MessageResponse("User logged out successfully"));
    }

    @Operation(summary = "Change password", description = "Change user password")
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.getUsername());


            userService.changePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            log.info("Password changed successfully for user: {}", request.getUsername());
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));

    }


    @Operation(summary = "Get current user",
            description = "Get current authenticated user information")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(
            @CurrentUser UserPrincipal currentUser) {
        // TODO:
        //  Can unauthenticated users even reach this endpoint?
        UserInfoResponse userInfo = UserInfoResponse.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .authorities(currentUser.getAuthorities())
                .build();

        return ResponseEntity.ok(userInfo);
    }

    // TODO:
    //  Duplicated method
    //deleted
}