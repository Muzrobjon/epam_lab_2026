package com.epam.gym.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    @Value("${security.brute-force.max-attempts:3}")
    private int maxAttempts;

    @Value("${security.brute-force.block-duration-minutes:5}")
    private int blockDurationMinutes;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // Fixed: use constant instead of field
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String username) {
        attemptsCache.invalidate(username);
        log.debug("Login succeeded for user: {}, attempts cache cleared", username);
    }

    public void loginFailed(String username) {
        int attempts = 0;
        try {
            attempts = attemptsCache.get(username);
        } catch (ExecutionException e) {
            log.error("Error getting login attempts for user: {}", username, e);
        }
        attempts++;
        attemptsCache.put(username, attempts);
        log.warn("Login failed for user: {}, attempts: {}/{}", username, attempts, maxAttempts);
    }

    public boolean isBlocked(String username) {
        try {
            boolean blocked = attemptsCache.get(username) >= maxAttempts;
            if (blocked) {
                log.warn("User {} is blocked due to {} failed login attempts", username, maxAttempts);
            }
            return blocked;
        } catch (ExecutionException e) {
            return false;
        }
    }

    public int getRemainingAttempts(String username) {
        try {
            int attempts = attemptsCache.get(username);
            return Math.max(0, maxAttempts - attempts);
        } catch (ExecutionException e) {
            return maxAttempts;
        }
    }

    public int getBlockDurationMinutes() {
        return blockDurationMinutes;
    }
}