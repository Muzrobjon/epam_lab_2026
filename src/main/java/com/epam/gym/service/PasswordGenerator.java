package com.epam.gym.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGenerator {
    private final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%&@#";
    private final SecureRandom rnd = new SecureRandom();
    public String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0 ; i < length ; i++) {
            sb.append(letters.charAt(rnd.nextInt(letters.length())));
        }
        return sb.toString();
    }
}
