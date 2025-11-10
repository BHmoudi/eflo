package com.eflo.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for Keycloak Admin Client
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties {

    private String authServerUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private Admin admin = new Admin();

    @Data
    public static class Admin {
        private String username;
        private String password;
        private String clientId = "admin-cli";
    }
}
