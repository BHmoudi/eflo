package com.eflo.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 *
 * This is the main entry point for the Eureka Service Discovery Server.
 * It provides service registration and discovery capabilities for all
 * microservices in the Eflo architecture.
 *
 * Features:
 * - Service registration and discovery
 * - Health monitoring
 * - Load balancing support
 * - High availability with peer-to-peer replication
 * - REST API for service registry
 *
 * @author Eflo Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
