package com.epam.gym.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends RuntimeException {
    private final int blockDurationMinutes;
    public AccountLockedException(int blockDurationMinutes) {
        super(String.format(
                "Too many failed login attempts. Account is locked for %d minutes.", blockDurationMinutes
        ));
        this.blockDurationMinutes = blockDurationMinutes;
    }
}
