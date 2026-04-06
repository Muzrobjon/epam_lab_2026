package com.epam.gym.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TokenBlacklistService {

    // Token -> Expiration time
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, LocalDateTime expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        log.info("Token blacklisted until: {}", expirationTime);

        // Clean up expired tokens
        cleanupExpiredTokens();
    }

    public boolean isBlacklisted(String token) {
        if (blacklistedTokens.containsKey(token)) {
            LocalDateTime expiration = blacklistedTokens.get(token);
            if (LocalDateTime.now().isBefore(expiration)) {
                return true;
            } else {
                // Token expired, remove from blacklist
                blacklistedTokens.remove(token);
            }
        }
        return false;
    }

    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        log.debug("Cleaned up expired tokens. Current blacklist size: {}", blacklistedTokens.size());
    }

}