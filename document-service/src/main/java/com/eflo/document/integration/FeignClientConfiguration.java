package com.eflo.document.integration;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Configuration class for Feign clients.
 * Provides custom error decoder, request interceptor, and logging configuration.
 *
 * <p>This configuration is used by all Feign clients to ensure consistent
 * behavior across service-to-service communication.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@Configuration
public class FeignClientConfiguration {

    /**
     * Configures the error decoder for Feign clients.
     * Converts HTTP errors into appropriate exceptions.
     *
     * @return the error decoder
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder();
    }

    /**
     * Configures request interceptor to propagate JWT tokens.
     * Ensures that authentication context is passed to downstream services.
     *
     * @return the request interceptor
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + token);
            }

            // Add correlation ID for request tracing
            requestTemplate.header("X-Correlation-ID", generateCorrelationId());

            // Add service identification
            requestTemplate.header("X-Service-Name", "document-service");
        };
    }

    /**
     * Configures Feign logger level.
     * Set to FULL for comprehensive logging (adjust for production).
     *
     * @return the logger level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Generates a correlation ID for request tracking.
     * Uses the existing correlation ID from request context if available,
     * otherwise generates a new UUID.
     *
     * @return correlation ID
     */
    private String generateCorrelationId() {
        // TODO: Extract from request context if available
        return java.util.UUID.randomUUID().toString();
    }
}
