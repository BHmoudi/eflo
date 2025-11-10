package com.eflo.document;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Document Service.
 *
 * This service provides comprehensive document management capabilities including:
 * - Document upload, storage, and retrieval
 * - Document type configuration and management
 * - Document validation workflows
 * - Version control and history tracking
 * - Virus scanning integration
 * - Document expiration management
 * - Advanced search and analytics
 * - Event-driven integration with other microservices
 *
 * @author Eflo Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class DocumentServiceApplication {

    /**
     * Main entry point for the Document Service application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApplication.class, args);
    }
}
