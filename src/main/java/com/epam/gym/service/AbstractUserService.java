package com.epam.gym.service;

import com.epam.gym.exception.AuthenticationException;
import com.epam.gym.exception.NotFoundException;
import com.epam.gym.exception.ValidationException;
import com.epam.gym.entity.User;
import com.epam.gym.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.function.Function;

@Slf4j
public abstract class AbstractUserService<T> {

    protected final UserRepository userRepository;
    protected final UsernameGenerator usernameGenerator;
    protected final PasswordGenerator passwordGenerator;
    protected final Validator validator;

    public AbstractUserService(
            UserRepository userRepository,
            UsernameGenerator usernameGenerator,
            PasswordGenerator passwordGenerator,
            Validator validator
    ) {
        this.userRepository = userRepository;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);

        T entity = findByUsername().apply(username);
        User user = extractUser(entity);

        if (user == null) {
            throw new AuthenticationException("User not found: " + username);
        }

        String storedPassword = user.getPassword();
        log.debug("Stored password: {}, Input password: {}", storedPassword, password);

        if (!storedPassword.equals(password)) {
            throw new AuthenticationException("Invalid password for user: " + username);
        }

        log.info("User authenticated successfully: {}", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);

        authenticate(username, oldPassword);

        T entity = findByUsername().apply(username);
        User user = extractUser(entity);

        if (user == null) {
            throw new NotFoundException("User not found: " + username);
        }

        user.setPassword(newPassword);

        log.info("Password changed for user: {}", username);
    }

    @Transactional
    public void toggleActiveStatus(String username, String password) {
        log.info("Toggling active status for user: {}", username);

        authenticate(username, password);

        T entity = findByUsername().apply(username);
        User user = extractUser(entity);

        if (user == null) {
            throw new NotFoundException("User not found: " + username);
        }

        user.setIsActive(!user.getIsActive());

        log.info("User {} is now {}", username, user.getIsActive() ? "active" : "inactive");
    }

    protected void validateEntity(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new ValidationException("Validation failed: " + message);
        }
    }

    protected abstract Function<String, T> findByUsername();

    protected abstract User extractUser(T entity);
}