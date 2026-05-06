package com.epam.gym.service;

import com.epam.gym.entity.User;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UsernameGenerator {

    public String generateUsername(User user, Function<String, Boolean> existsChecker) {
        String baseUsername = user.getFirstName() + "." + user.getLastName();
        String username = baseUsername;
        int suffix = 1;

        while (existsChecker.apply(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }
}