package com.eflo.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security Configuration for User Service
 *
 * This configuration enables OAuth2 Resource Server with JWT token validation
 * and role-based access control using Keycloak.
 *
 * Features:
 * - JWT token validation
 * - Role extraction from Keycloak realm_access
 * - Method-level security annotations
 * - Actuator endpoints security
 * - Custom authorization rules
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // User endpoints - role-based access
                .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "SALESPERSON", "VIEWER", "EXTERNAL_API_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("SUPER_ADMIN")

                // Business unit endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "VIEWER", "EXTERNAL_API_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/business-units/**").hasRole("SUPER_ADMIN")

                // User-Business unit assignment endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/user-business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "VIEWER", "EXTERNAL_API_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/user-business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/user-business-units/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")

                // Hierarchy endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/hierarchies/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "VIEWER", "EXTERNAL_API_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/hierarchies/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.PUT, "/api/v1/hierarchies/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "EXTERNAL_API_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/hierarchies/**").hasAnyRole("SUPER_ADMIN", "ADMIN_LOCAL", "EXTERNAL_API_WRITE")

                // Keycloak sync - admin only
                .requestMatchers("/api/v1/keycloak-sync/**").hasRole("SUPER_ADMIN")

                // Actuator endpoints - admin only
                .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * JWT Authentication Converter
     * Extracts roles from Keycloak realm_access claim (same as order-service)
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // Set principal claim name (username from token)
        converter.setPrincipalClaimName("preferred_username");

        // Extract roles from realm_access, roles claim, and resource_access
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = new java.util.ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                Object r = realmAccess.get("roles");
                if (r instanceof List<?>) {
                    for (Object o : (List<?>) r) if (o instanceof String s) roles.add(s);
                }
            }

            List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) roles.addAll(directRoles);

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(val -> {
                    if (val instanceof Map<?, ?> m) {
                        Object rr = m.get("roles");
                        if (rr instanceof List<?>) {
                            for (Object o : (List<?>) rr) if (o instanceof String s) roles.add(s);
                        }
                    }
                });
            }

            if (roles.contains("SUPER_ADMIN")) {
                roles.addAll(java.util.List.of(
                        "ADMIN", "ADMIN_LOCAL", "SALES_MANAGER", "SALESPERSON", "VIEWER",
                        "DOCUMENT_MANAGER", "DOCUMENT_UPLOADER", "VALIDATOR", "DOCUMENT_VALIDATOR",
                        "ANALYTICS_VIEWER", "COMPLIANCE_OFFICER", "SYSTEM_IMPORT",
                        "EXTERNAL_API_READ", "EXTERNAL_API_WRITE", "USER", "SECRETARY", "ACCOUNTANT"
                ));
            }

            return roles.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        });
        return converter;
    }
}
