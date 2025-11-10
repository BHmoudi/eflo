package com.eflo.document.integration;

import com.eflo.document.integration.dto.UserDetailsResponse;
import com.eflo.document.integration.dto.UserPermissionsResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client for communicating with the User Service.
 * Provides methods to retrieve user information, permissions, and role-based queries.
 *
 * <p>This client includes circuit breaker configuration for resilience and fault tolerance.
 * Fallback methods are provided to handle service unavailability gracefully.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@FeignClient(
    name = "user-service",
    path = "/api/v1/users",
    configuration = FeignClientConfiguration.class
)
public interface UserServiceClient {

    /**
     * Retrieves detailed information about a specific user.
     *
     * @param userId the ID of the user to retrieve
     * @return the user details
     */
    @GetMapping("/{userId}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserDetailsFallback")
    UserDetailsResponse getUserDetails(@PathVariable("userId") Long userId);

    /**
     * Retrieves the permissions for a specific user.
     *
     * <p>This endpoint is used to verify if a user has the necessary permissions
     * to perform document operations (e.g., validate documents, delete documents).</p>
     *
     * @param userId the ID of the user
     * @return the user's permissions
     */
    @GetMapping("/{userId}/permissions")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserPermissionsFallback")
    UserPermissionsResponse getUserPermissions(@PathVariable("userId") Long userId);

    /**
     * Retrieves all users with a specific role.
     *
     * <p>This endpoint is useful for finding users who can perform certain actions,
     * such as document validators or document approvers.</p>
     *
     * @param role the role to search for
     * @return list of users with the specified role
     */
    @GetMapping("/role/{role}")
    @CircuitBreaker(name = "user-service", fallbackMethod = "getUsersByRoleFallback")
    List<UserDetailsResponse> getUsersByRole(@PathVariable("role") String role);

    /**
     * Fallback method for getUserDetails when the user service is unavailable.
     *
     * @param userId the user ID
     * @param throwable the exception that triggered the fallback
     * @return null to indicate service unavailability
     */
    default UserDetailsResponse getUserDetailsFallback(Long userId, Throwable throwable) {
        // Log the error appropriately
        return null;
    }

    /**
     * Fallback method for getUserPermissions when the user service is unavailable.
     *
     * @param userId the user ID
     * @param throwable the exception that triggered the fallback
     * @return empty permissions to fail safely
     */
    default UserPermissionsResponse getUserPermissionsFallback(Long userId, Throwable throwable) {
        // Log the error appropriately
        // Return empty permissions for security
        return UserPermissionsResponse.builder()
            .userId(userId)
            .permissions(List.of())
            .build();
    }

    /**
     * Fallback method for getUsersByRole when the user service is unavailable.
     *
     * @param role the role
     * @param throwable the exception that triggered the fallback
     * @return empty list to indicate service unavailability
     */
    default List<UserDetailsResponse> getUsersByRoleFallback(String role, Throwable throwable) {
        // Log the error appropriately
        return List.of();
    }
}
