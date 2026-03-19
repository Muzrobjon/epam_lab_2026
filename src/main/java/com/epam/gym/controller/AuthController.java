package com.epam.gym.controller;

import com.epam.gym.dto.request.ChangePasswordRequest;
import com.epam.gym.dto.request.LoginRequest;
import com.epam.gym.dto.request.ToggleActiveRequest;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and password management APIs")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "User login", description = "Authenticate user with username and password")
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        userService.authenticate(request.getUsername(), request.getPassword());

        log.info("User {} logged in successfully", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change password", description = "Change user password")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", request.getUsername());

        userService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        log.info("Password changed successfully for user: {}", request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate/Deactivate user", description = "Change user's active status")
    @PatchMapping("/users/{username}/status")
    public ResponseEntity<Void> toggleUserStatus(
            @Parameter(description = "Username of the user", required = true)
            @PathVariable String username,
            @Valid @RequestBody ToggleActiveRequest request) {

        log.info("Toggling active status for user: {}", username);

        if (!username.equals(request.getUsername())) {
            throw new ValidationException("Username in path does not match username in request body");
        }

        userService.setActiveStatus(username, request.getPassword(), request.getIsActive());

        log.info("Active status changed for user: {}", username);
        return ResponseEntity.ok().build();
    }
}