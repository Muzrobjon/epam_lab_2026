package com.epam.gym.exception;

import lombok.Getter;

@Getter
public class BadLoginException extends RuntimeException {

    private final int remainingAttempts;
    private final Integer blockDurationMinutes;

    public BadLoginException(String message, int remainingAttempts, Integer blockDurationMinutes) {
        super(message);
        this.remainingAttempts = remainingAttempts;
        this.blockDurationMinutes = blockDurationMinutes;
    }
}
