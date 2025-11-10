package com.eflo.order.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Configuration
@Slf4j
public class FeignClientConfiguration {

    @Value("${spring.security.oauth2.client.registration.workflow-service.client-id:order-service-client}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.workflow-service.client-secret:order-service-secret}")
    private String clientSecret;

    private String cachedToken;
    private long tokenExpirationTime = 0;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Try to get user JWT first
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                    String token = jwt.getTokenValue();
                    template.header("Authorization", "Bearer " + token);
                    log.debug("Using user JWT for Feign request: {}", template.url());
                } else {
                    // Fall back to service account token
                    String serviceToken = getServiceAccountToken();
                    if (serviceToken != null) {
                        template.header("Authorization", "Bearer " + serviceToken);
                        log.debug("Using service account token for Feign request: {}", template.url());
                    } else {
                        log.error("No authentication available for Feign request: {}", template.url());
                    }
                }
            }
        };
    }

    private String getServiceAccountToken() {
        // Check if cached token is still valid
        if (cachedToken != null && System.currentTimeMillis() < tokenExpirationTime) {
            return cachedToken;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:8180/realms/eflo/protocol/openid-connect/token",
                request,
                Map.class
            );

            if (response.getBody() != null) {
                cachedToken = (String) response.getBody().get("access_token");
                Integer expiresIn = (Integer) response.getBody().get("expires_in");
                tokenExpirationTime = System.currentTimeMillis() + (expiresIn * 1000L) - 60000; // Refresh 1 min early
                log.info("Obtained service account token, expires in {} seconds", expiresIn);
                return cachedToken;
            }
        } catch (Exception e) {
            log.error("Failed to obtain service account token: {}", e.getMessage());
        }

        return null;
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
    }
}
