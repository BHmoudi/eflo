package com.eflo.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration
 *
 * Enables JPA auditing for automatic @CreatedDate and @LastModifiedDate handling
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.eflo.user.domain.repository")
@EnableTransactionManagement
public class JpaConfig {
    // JPA auditing is automatically configured
}
