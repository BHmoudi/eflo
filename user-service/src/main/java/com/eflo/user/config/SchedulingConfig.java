package com.eflo.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling Configuration
 *
 * Enables scheduling for @Scheduled tasks like Keycloak sync
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Scheduling is automatically configured
}
