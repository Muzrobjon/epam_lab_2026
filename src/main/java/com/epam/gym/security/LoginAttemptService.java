package com.epam.gym.security;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginAttemptService {

    private final int maxAttempts;

    @Getter
    private final int blockDurationMinutes;

    private final LoadingCache<String, Integer> attemptsCache;

    // TODO:
    //  It's better to use injected config though, not hardcoded constants.
    //  Constructor can accept @Value parameters and initialize private final class fields with them
    public LoginAttemptService(
            @Value("${security.brute-force.max-attempts:3}") int maxAttempts,
            @Value("${security.brute-force.block-duration-minutes:5}") int blockDurationMinutes) {

        this.maxAttempts = maxAttempts;
        this.blockDurationMinutes = blockDurationMinutes;
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(blockDurationMinutes, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Nonnull
                    @Override
                    public Integer load(@Nonnull String key) {
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
                log.warn("User {} is blocked due to {} failed login attempts",
                        username, maxAttempts);
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
}



