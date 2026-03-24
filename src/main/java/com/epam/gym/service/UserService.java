package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.metrics.UserMetrics;
import com.epam.gym.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final UserMetrics userMetrics;

    private User currentUser;

    @Timed(value = "gym_user_create_seconds", description = "Time to create user")
    @Transactional
    public User createUser(String firstName, String lastName) {
        log.debug("Creating user: {} {}", firstName, lastName);

        String rawPassword = passwordGenerator.generatePassword();

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .password(rawPassword)
                .build();

        String username = usernameGenerator.generateUsername(user, userRepository::existsByUsername);
        user.setUsername(username);

        User savedUser = userRepository.save(user);
        userMetrics.incrementRegistrations();

        log.info("User created: {}", username);
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Timed(value = "gym_authenticate_seconds", description = "Authentication time")
    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    userMetrics.incrementLoginFailure();
                    log.warn("Login failed - user not found: {}", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!user.getPassword().equals(password)) {
            userMetrics.incrementLoginFailure();
            log.warn("Login failed - invalid password: {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        currentUser = user;
        userMetrics.incrementLoginSuccess();
        log.info("User authenticated: {}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        isAuthenticated(username);
        authenticate(username, oldPassword);

        User user = findByUsername(username);
        user.setPassword(newPassword);
        userRepository.save(user);

        userMetrics.incrementPasswordChanges();
        log.info("Password changed for user: {}", username);
    }

    @Transactional
    public void setActiveStatus(String username, Boolean isActive) {
        log.info("Setting active status for user: {} to {}", username, isActive);

        isAuthenticated(username);

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

    public void isAuthenticated(String username) {
        if (currentUser == null || !currentUser.getUsername().equals(username)) {
            throw new AuthenticationException("User is not authenticated: " + username);
        }
    }
}