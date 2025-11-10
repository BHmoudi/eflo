package com.eflo.user.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Helper
 *
 * Utility class to extract user information and custom claims from JWT tokens
 * in Eflo microservices.
 *
 * Usage in Controllers:
 * <pre>
 * {@code
 * @Autowired
 * private JwtAuthenticationHelper jwtHelper;
 *
 * public void someMethod() {
 *     String username = jwtHelper.getCurrentUsername();
 *     String employeeNumber = jwtHelper.getEmployeeNumber();
 *     List<Integer> businessUnits = jwtHelper.getBusinessUnitIds();
 * }
 * }
 * </pre>
 */
@Component
public class JwtAuthenticationHelper {

    /**
     * Get the current JWT token from security context
     */
    public Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) authentication).getToken();
        }

        return null;
    }

    /**
     * Get the current username (email)
     */
    public String getCurrentUsername() {
        Jwt jwt = getCurrentJwt();
        if (jwt == null) {
            return null;
        }

        // Try preferred_username first
        String username = jwt.getClaimAsString("preferred_username");
        if (username != null && !username.isEmpty()) {
            return username;
        }

        // Fallback to email
        username = jwt.getClaimAsString("email");
        if (username != null && !username.isEmpty()) {
            return username;
        }

        // Fallback to sub (subject)
        return jwt.getClaimAsString("sub");
    }

    /**
     * Get the user's email
     */
    public String getCurrentUserEmail() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("email") : null;
    }

    /**
     * Get the user's full name
     */
    public String getCurrentUserFullName() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("name") : null;
    }

    /**
     * Get the user's first name
     */
    public String getCurrentUserFirstName() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("given_name") : null;
    }

    /**
     * Get the user's last name
     */
    public String getCurrentUserLastName() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("family_name") : null;
    }

    /**
     * Get the user's employee number
     */
    public String getEmployeeNumber() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("employeeNumber") : null;
    }

    /**
     * Get the user's business unit IDs
     * Returns as List<Integer> by parsing the JSON array string
     */
    public List<Integer> getBusinessUnitIds() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            String businessUnitsStr = jwt.getClaimAsString("businessUnitIds");
            if (businessUnitsStr != null) {
                // Parse JSON array string "[1, 2, 3]" to List<Integer>
                return parseIntegerList(businessUnitsStr);
            }
        }
        return List.of();
    }

    /**
     * Get the user's sales manager IDs
     */
    public List<Integer> getSalesManagerIds() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            String salesManagersStr = jwt.getClaimAsString("salesmanagerIds");
            if (salesManagersStr != null) {
                return parseIntegerList(salesManagersStr);
            }
        }
        return List.of();
    }

    /**
     * Get the user's region
     */
    public String getRegion() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("region") : null;
    }

    /**
     * Get the user's department
     */
    public String getDepartment() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaimAsString("department") : null;
    }

    /**
     * Get the user's roles
     */
    public List<String> getUserRoles() {
        Jwt jwt = getCurrentJwt();
        if (jwt != null) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                return roles;
            }
        }
        return List.of();
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return getUserRoles().contains(role);
    }

    /**
     * Check if user is a Super Admin
     */
    public boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }

    /**
     * Check if user is an Admin (SUPER_ADMIN or ADMIN_LOCAL)
     */
    public boolean isAdmin() {
        return hasRole("SUPER_ADMIN") || hasRole("ADMIN_LOCAL");
    }

    /**
     * Check if user is a Sales Manager
     */
    public boolean isSalesManager() {
        return hasRole("SALES_MANAGER") || isSuperAdmin();
    }

    /**
     * Check if user is a Salesperson
     */
    public boolean isSalesperson() {
        return hasRole("SALESPERSON") || isSalesManager();
    }

    /**
     * Check if user has access to a specific business unit
     */
    public boolean hasAccessToBusinessUnit(Integer businessUnitId) {
        if (isSuperAdmin()) {
            return true; // Super admin has access to all business units
        }
        return getBusinessUnitIds().contains(businessUnitId);
    }

    /**
     * Get user ID (subject claim)
     */
    public String getUserId() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getSubject() : null;
    }

    /**
     * Get all claims as a Map
     */
    public Map<String, Object> getAllClaims() {
        Jwt jwt = getCurrentJwt();
        return jwt != null ? jwt.getClaims() : Map.of();
    }

    /**
     * Helper method to parse integer list from JSON string
     */
    private List<Integer> parseIntegerList(String jsonArrayStr) {
        try {
            // Remove brackets and spaces
            String cleaned = jsonArrayStr.replaceAll("[\\[\\]\\s]", "");

            if (cleaned.isEmpty()) {
                return List.of();
            }

            // Split by comma and convert to integers
            String[] parts = cleaned.split(",");
            return java.util.Arrays.stream(parts)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            // Log error and return empty list
            System.err.println("Failed to parse integer list from: " + jsonArrayStr);
            return List.of();
        }
    }
}
