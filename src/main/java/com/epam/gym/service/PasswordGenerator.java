package com.epam.gym.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#";
    private static final int DEFAULT_LENGTH = 10;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generatePassword() {
        return generatePassword(DEFAULT_LENGTH);
    }

    public String generatePassword(int length) {
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(CHARACTERS.charAt(secureRandom.nextInt(CHARACTERS.length())));
        }
        return password.toString();
    }
}

