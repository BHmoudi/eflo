package com.eflo.external.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * External API Gateway Application
 *
 * Provides secure external partner integration with:
 * - OAuth2 Client Credentials authentication
 * - IP Whitelisting per client
 * - Redis-backed rate limiting
 * - Comprehensive audit logging
 * - Webhook management
 * - API versioning support
 *
 * @author Agent 5 - External API Gateway Specialist
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ExternalGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExternalGatewayApplication.class, args);
    }
}
