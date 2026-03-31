package com.epam.gym.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;

    public String generateRandomPassword() {
        String password = passwordGenerator.generatePassword();
        log.debug("Generated random password");
        return password;
    }



    public String encodePassword(String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.debug("Password encoded successfully");
        return encodedPassword;
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.debug("Password match result: {}", matches);
        return matches;
    }

    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUpperCase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowerCase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch ->
                "$%&@#".indexOf(ch) >= 0
        );

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar;
    }
}