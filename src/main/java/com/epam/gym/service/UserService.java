package com.epam.gym.service;

import com.epam.gym.entity.User;
import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.repository.UserRepository;
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

    @Transactional
    public User createUser(String firstName, String lastName) {
        log.debug("Creating user for {} {}", firstName, lastName);

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
        log.debug("Created user: {} with username: {}", savedUser.getId(), username);

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password");
        }

        log.info("User authenticated successfully: {}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        authenticate(username, oldPassword);

        User user = findByUsername(username);
        user.setPassword(newPassword);
        userRepository.save(user);

        log.info("Password changed for user: {}", username);
    }

    @Transactional
    public void setActiveStatus(String username, String password, Boolean isActive) {
        log.info("Setting active status for user: {} to {}", username, isActive);

        authenticate(username, password);

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
}