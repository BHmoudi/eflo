package com.eflo.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Config Server Application
 *
 * This is the main entry point for the Spring Cloud Config Server.
 * It provides centralized configuration management for all microservices
 * in the Eflo architecture with Git-backed storage, encryption support,
 * and hot refresh capabilities via Spring Cloud Bus.
 *
 * Features:
 * - Centralized configuration management
 * - Git-backed configuration storage
 * - Environment-specific configurations (dev, test, prod)
 * - Encryption/decryption of sensitive data
 * - Hot configuration refresh via Spring Cloud Bus
 * - Service discovery integration with Eureka
 * - REST API for configuration retrieval
 * - Health monitoring and metrics
 *
 * Endpoints:
 * - /{application}/{profile} - Get configuration for application and profile
 * - /{application}/{profile}/{label} - Get configuration for specific Git branch/tag
 * - /encrypt - Encrypt sensitive data (POST)
 * - /decrypt - Decrypt sensitive data (POST)
 * - /actuator/health - Health check endpoint
 * - /actuator/bus-refresh - Trigger configuration refresh across all services
 *
 * @author Eflo Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
