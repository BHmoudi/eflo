package com.eflo.config.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security Configuration for Config Server
 *
 * Configures security settings for the Config Server:
 * - Allows public access to configuration endpoints for service clients
 * - Protects encrypt/decrypt endpoints with authentication
 * - Allows public access to actuator endpoints for health monitoring
 * - Disables CSRF for RESTful API operations
 * - Configures HTTP Basic authentication for sensitive operations
 *
 * Security Considerations:
 * - In production, consider using OAuth2/JWT for service-to-service auth
 * - Implement API key validation for additional security
 * - Use HTTPS/TLS for all communications
 * - Rotate encryption keys regularly
 * - Implement rate limiting for encrypt/decrypt endpoints
 * - Monitor and audit access to sensitive endpoints
 *
 * Access Rules:
 * - /actuator/** - Public (for health checks and monitoring)
 * - /encrypt/** - Authenticated (for encrypting sensitive data)
 * - /decrypt/** - Authenticated (for decrypting sensitive data)
 * - /** - Public (for configuration retrieval by services)
 *
 * @author Eflo Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests
     *
     * @param http HttpSecurity configuration object
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for REST API operations
            .csrf(csrf -> csrf.disable())

            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to actuator endpoints for health checks
                .requestMatchers("/actuator/**").permitAll()

                // Require authentication for encrypt/decrypt endpoints
                .requestMatchers("/encrypt/**").authenticated()
                .requestMatchers("/decrypt/**").authenticated()

                // Allow public access to configuration endpoints
                // Services need to fetch configurations without authentication
                .anyRequest().permitAll()
            )

            // Enable HTTP Basic authentication for encrypt/decrypt endpoints
            .httpBasic(withDefaults());

        return http.build();
    }
}
