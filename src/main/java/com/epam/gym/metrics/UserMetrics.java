package com.epam.gym.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserMetrics {

    private final Counter registrations;
    private final Counter loginSuccess;
    private final Counter loginFailure;
    private final Counter passwordChanges;

    public UserMetrics(MeterRegistry registry) {
        this.registrations = Counter.builder("gym_user_registrations_total")
                .description("Total user registrations")
                .tag("type", "user")
                .register(registry);

        this.loginSuccess = Counter.builder("gym_login_success_total")
                .description("Total successful logins")
                .tag("type", "auth")
                .register(registry);

        this.loginFailure = Counter.builder("gym_login_failure_total")
                .description("Total failed logins")
                .tag("type", "auth")
                .register(registry);

        this.passwordChanges = Counter.builder("gym_password_changes_total")
                .description("Total password changes")
                .tag("type", "auth")
                .register(registry);

        log.info("User metrics initialized");
    }

    public void incrementRegistrations() {
        registrations.increment();
    }

    public void incrementLoginSuccess() {
        loginSuccess.increment();
    }

    public void incrementLoginFailure() {
        loginFailure.increment();
    }

    public void incrementPasswordChanges() {
        passwordChanges.increment();
    }
}