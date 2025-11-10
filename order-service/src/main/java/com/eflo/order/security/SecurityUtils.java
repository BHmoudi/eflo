package com.eflo.order.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {

    private static final long DEFAULT_TEST_USER_ID = 1L;
    private static final String DEFAULT_TEST_USERNAME = "john.doe@eflo.com";

    private SecurityUtils() {}

    public static Long getUserId(Jwt jwt) {
        if (jwt == null) {
            return DEFAULT_TEST_USER_ID;
        }

        Object employeeNumber = jwt.getClaim("employeeNumber");
        if (employeeNumber instanceof Number) {
            return ((Number) employeeNumber).longValue();
        }
        if (employeeNumber instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {}
        }

        Object userId = jwt.getClaim("user_id");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }

        String sub = jwt.getSubject();
        if (sub != null) {
            return (long) sub.hashCode();
        }

        return DEFAULT_TEST_USER_ID;
    }

    public static String getUsername(Jwt jwt) {
        if (jwt == null) {
            return DEFAULT_TEST_USERNAME;
        }
        String preferred = jwt.getClaim("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        String email = jwt.getClaim("email");
        if (email != null && !email.isBlank()) {
            return email;
        }
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) {
            return sub;
        }
        return DEFAULT_TEST_USERNAME;
    }

    public static Jwt getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        return getUserId(getCurrentJwt());
    }

    public static String getCurrentUsername() {
        return getUsername(getCurrentJwt());
    }
}

