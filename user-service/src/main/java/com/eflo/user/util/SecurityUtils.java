package com.eflo.user.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for security-related operations.
 */
@UtilityClass
public class SecurityUtils {

    /**
     * Get current authenticated user ID from JWT token.
     *
     * @return user ID or null if not authenticated
     */
    public static String getCurrentUserId() {
        return getAuthentication()
                .map(auth -> {
                    if (auth.getPrincipal() instanceof Jwt jwt) {
                        return jwt.getSubject();
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * Get current authenticated username.
     *
     * @return username or null if not authenticated
     */
    public static String getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName)
                .orElse(null);
    }

    /**
     * Get current authentication object.
     *
     * @return Optional containing Authentication or empty if not authenticated
     */
    public static Optional<Authentication> getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return Optional.of(auth);
        }
        return Optional.empty();
    }

    /**
     * Get JWT token from current authentication.
     *
     * @return Optional containing Jwt or empty if not available
     */
    public static Optional<Jwt> getJwtToken() {
        return getAuthentication()
                .filter(auth -> auth.getPrincipal() instanceof Jwt)
                .map(auth -> (Jwt) auth.getPrincipal());
    }

    /**
     * Get claim from JWT token.
     *
     * @param claimName the name of the claim
     * @return claim value or null if not found
     */
    public static Object getJwtClaim(String claimName) {
        return getJwtToken()
                .map(jwt -> jwt.getClaim(claimName))
                .orElse(null);
    }

    /**
     * Get string claim from JWT token.
     *
     * @param claimName the name of the claim
     * @return claim value or null if not found
     */
    public static String getJwtClaimAsString(String claimName) {
        return getJwtToken()
                .map(jwt -> jwt.getClaimAsString(claimName))
                .orElse(null);
    }

    /**
     * Get list claim from JWT token.
     *
     * @param claimName the name of the claim
     * @return list of claim values or empty list if not found
     */
    @SuppressWarnings("unchecked")
    public static List<String> getJwtClaimAsList(String claimName) {
        return getJwtToken()
                .map(jwt -> {
                    Object claim = jwt.getClaim(claimName);
                    if (claim instanceof List) {
                        return (List<String>) claim;
                    }
                    return Collections.<String>emptyList();
                })
                .orElse(Collections.emptyList());
    }

    /**
     * Get current user's authorities/roles.
     *
     * @return collection of authorities
     */
    public static Collection<? extends GrantedAuthority> getCurrentAuthorities() {
        return getAuthentication()
                .map(Authentication::getAuthorities)
                .orElse(Collections.emptyList());
    }

    /**
     * Check if current user has a specific role.
     *
     * @param role the role to check
     * @return true if user has the role
     */
    public static boolean hasRole(String role) {
        return getCurrentAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role) || auth.getAuthority().equals(role));
    }

    /**
     * Check if current user has any of the specified roles.
     *
     * @param roles the roles to check
     * @return true if user has any of the roles
     */
    public static boolean hasAnyRole(String... roles) {
        Collection<? extends GrantedAuthority> authorities = getCurrentAuthorities();
        for (String role : roles) {
            if (authorities.stream().anyMatch(auth ->
                    auth.getAuthority().equals("ROLE_" + role) || auth.getAuthority().equals(role))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has a specific authority.
     *
     * @param authority the authority to check
     * @return true if user has the authority
     */
    public static boolean hasAuthority(String authority) {
        return getCurrentAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }

    /**
     * Check if user is authenticated.
     *
     * @return true if authenticated
     */
    public static boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }

    /**
     * Get email from JWT token.
     *
     * @return email or null if not found
     */
    public static String getCurrentUserEmail() {
        return getJwtClaimAsString("email");
    }

    /**
     * Get preferred username from JWT token.
     *
     * @return preferred username or null if not found
     */
    public static String getPreferredUsername() {
        return getJwtClaimAsString("preferred_username");
    }

    /**
     * Get given name from JWT token.
     *
     * @return given name or null if not found
     */
    public static String getGivenName() {
        return getJwtClaimAsString("given_name");
    }

    /**
     * Get family name from JWT token.
     *
     * @return family name or null if not found
     */
    public static String getFamilyName() {
        return getJwtClaimAsString("family_name");
    }
}
