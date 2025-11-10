package com.eflo.workflow.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for User Service
 *
 * Provides integration with the User Service for user information and role management.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@FeignClient(name = "user-service", url = "${eflo.workflow.integration.user-service.url}")
public interface UserServiceClient {

    /**
     * Get user details by ID
     *
     * @param userId User ID
     * @return User details
     */
    @GetMapping("/api/v1/users/{userId}")
    Map<String, Object> getUserById(@PathVariable Long userId);

    /**
     * Get users by role
     *
     * @param role Role name
     * @return List of users with the specified role
     */
    @GetMapping("/api/v1/users/by-role")
    List<Map<String, Object>> getUsersByRole(@RequestParam String role);

    /**
     * Get user's manager
     *
     * @param userId User ID
     * @return Manager details
     */
    @GetMapping("/api/v1/users/{userId}/manager")
    Map<String, Object> getUserManager(@PathVariable Long userId);

    /**
     * Get user email by ID
     *
     * @param userId User ID
     * @return User email
     */
    @GetMapping("/api/v1/users/{userId}/email")
    String getUserEmail(@PathVariable Long userId);

    /**
     * Get user phone by ID
     *
     * @param userId User ID
     * @return User phone
     */
    @GetMapping("/api/v1/users/{userId}/phone")
    String getUserPhone(@PathVariable Long userId);
}
