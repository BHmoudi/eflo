package com.eflo.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Eureka Server
 * Implements HTTP Basic authentication to protect service registry endpoints
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security with Basic Authentication
     *
     * Security rules:
     * - Public access: Eureka static resources (CSS, JS, fonts)
     * - Public access: Actuator health and info endpoints
     * - Protected: All other Eureka endpoints require authentication
     *
     * @param http the HttpSecurity to modify
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for service-to-service communication
            .authorizeHttpRequests(auth -> auth
                // Allow public access to Eureka static resources
                .requestMatchers("/eureka/css/**").permitAll()
                .requestMatchers("/eureka/js/**").permitAll()
                .requestMatchers("/eureka/fonts/**").permitAll()

                // Allow public access to health and info actuator endpoints
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()

                // Require authentication for all other /eureka/** endpoints
                .requestMatchers("/eureka/**").authenticated()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> {}); // Enable HTTP Basic authentication

        return http.build();
    }
}
