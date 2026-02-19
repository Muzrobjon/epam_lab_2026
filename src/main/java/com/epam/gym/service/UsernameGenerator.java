package com.epam.gym.service;

import com.epam.gym.model.User;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class UsernameGenerator {
    public String generateUsername(User user, Predicate<String> usernameExists) {
        String base = normalize(user.getFirstName()) + "." + normalize(user.getLastName());
        int counter = 0;
        String candidate = base;

        while (usernameExists.test(candidate)) {
            candidate = base + ++counter;
        }
        return candidate;
    }

    private String normalize(String s) {
        return s.trim()
                .replaceAll("\\s+", "");
    }
}
