package com.eflo.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Internal API Gateway Application
 *
 * This is the main entry point for the Internal API Gateway that serves as the
 * single point of entry for all frontend client requests to the Eflo microservices.
 *
 * Key Features:
 * - Dynamic routing to microservices via Eureka service discovery
 * - JWT authentication and authorization with Keycloak
 * - Redis-backed rate limiting per user and per IP
 * - Circuit breaker protection with Resilience4j
 * - CORS configuration for frontend clients
 * - Request/response logging and monitoring
 * - Graceful fallback responses for unavailable services
 * - Load balancing across service instances
 *
 * Supported Routes:
 * - /api/v1/orders/** → Order Service (8081)
 * - /api/v1/workflow/** → Workflow Service (8082)
 * - /api/v1/commissions/** → Commission Service (8083)
 * - /api/v1/documents/** → Document Service (8084)
 * - /api/v1/users/** → User Service (8085)
 * - /api/v1/references/** → Reference Service (8086)
 * - /api/v1/reports/** → Reporting Service (8087)
 * - /api/v1/alerts/** → Alert Service (8088)
 * - /api/v1/external/** → External API Service (8089)
 * - /api/v1/invoices/** → Invoice Service (8090)
 *
 * Security:
 * - All requests (except public endpoints) require valid JWT token
 * - User context is extracted from JWT and propagated to downstream services
 * - Rate limiting prevents abuse and ensures fair resource allocation
 * - Circuit breakers protect against cascading failures
 *
 * @author Eflo Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class InternalGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternalGatewayApplication.class, args);
    }
}
