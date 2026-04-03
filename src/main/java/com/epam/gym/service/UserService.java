package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.WeakPasswordException;
import com.epam.gym.metrics.UserMetrics;
import com.epam.gym.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordService passwordService;
    private final UserMetrics userMetrics;

    @Timed(value = "gym_user_create_seconds", description = "Time to create user")
    @Transactional
    public User createUser(String firstName, String lastName) {
        log.debug("Creating user: {} {}", firstName, lastName);

        // Generate random password for new user
        String rawPassword = passwordService.generateRandomPassword();

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .password(passwordService.encodePassword(rawPassword)) // Encode password
                .build();

        String username = usernameGenerator.generateUsername(user, userRepository::existsByUsername);
        user.setUsername(username);

        User savedUser = userRepository.save(user);

        // TODO:
        //  What is the use case of returning raw password in response?
        savedUser.setPassword(rawPassword);

        userMetrics.incrementRegistrations();

        log.info("User created: {}", username);
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        verifyResourceOwnership(username);

        User user = findByUsername(username);

        // Verify old password
        if (!passwordService.matches(oldPassword, user.getPassword())) {
            userMetrics.incrementLoginFailure();
            throw new AuthenticationException("Invalid old password");
        }

        // Validate new password strength (optional)
        if (!passwordService.isPasswordStrong(newPassword)) {
            // TODO:
            //  How will the response look like if password does not meet strength requirements and what will the http status be?
            //  I assume with the current impl it will be 500 Internal Server Error, but 400 with details feels more appropriate.
            throw new WeakPasswordException(
                    "Password must be at least 8 characters and contain uppercase, lowercase, digit, and special character"
            );
        }

        // Encode and save new password
        user.setPassword(passwordService.encodePassword(newPassword));
        userRepository.save(user);

        userMetrics.incrementPasswordChanges();
        log.info("Password changed for user: {}", username);
    }

    @Transactional
    public void setActiveStatus(String username, Boolean isActive) {
        log.info("Setting active status for user: {} to {}", username, isActive);

        verifyResourceOwnership(username);

        User user = findByUsername(username);
        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("User {} active status set to: {}", username, isActive);
    }

    public void updateUserBasicInfo(User existingUser, String firstName, String lastName, Boolean isActive) {
        if (firstName != null) {
            existingUser.setFirstName(firstName);
        }
        if (lastName != null) {
            existingUser.setLastName(lastName);
        }
        if (isActive != null) {
            existingUser.setIsActive(isActive);
        }
    }

    // TODO:
    //  Is this check necessary, if JWT authentication flow you've implemented already enforces security?
    public void verifyResourceOwnership(String username) {
        String authenticatedUsername = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getName();

        if (!authenticatedUsername.equals(username)) {
            throw new AuthenticationException(
                    "Access denied: you can only modify your own resources");
        }
    }
}