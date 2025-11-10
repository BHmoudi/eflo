package com.eflo.user.config;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Configuration for Feign clients.
 * Sets up interceptors and logging for inter-service communication.
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Request interceptor to propagate JWT token to downstream services.
     *
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String tokenValue = jwt.getTokenValue();
                requestTemplate.header("Authorization", "Bearer " + tokenValue);
                log.debug("Propagating JWT token to downstream service");
            }
        };
    }

    /**
     * Feign logger level configuration.
     *
     * @return Logger.Level
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Request interceptor for adding common headers.
     *
     * @return RequestInterceptor
     */
    @Bean
    public RequestInterceptor requestCommonHeadersInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Service-Name", "user-service");
            requestTemplate.header("Accept", "application/json");
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}
