package com.eflo.document.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the Document Service.
 *
 * Configures:
 * - OAuth2 Resource Server with JWT tokens
 * - Role-based access control
 * - CORS settings
 * - Public endpoints for health checks
 * - Stateless session management
 *
 * @author Eflo Platform Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     *
     * @param http HttpSecurity configuration
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Configure authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Document upload - requires authentication
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/upload").hasAnyRole("SUPER_ADMIN", "USER", "DOCUMENT_UPLOADER")
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/upload-multiple").hasAnyRole("USER", "DOCUMENT_UPLOADER")

                // Document retrieval - requires authentication
                .requestMatchers(HttpMethod.GET, "/api/v1/documents/**").hasAnyRole("SUPER_ADMIN", "USER", "VIEWER", "DOCUMENT_MANAGER")

                // Document modification - requires specific roles
                .requestMatchers(HttpMethod.PUT, "/api/v1/documents/**").hasAnyRole("SUPER_ADMIN", "DOCUMENT_MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/**").hasAnyRole("SUPER_ADMIN", "DOCUMENT_MANAGER", "ADMIN")

                // Document validation - requires validator role
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/*/validate").hasAnyRole("SUPER_ADMIN", "VALIDATOR", "DOCUMENT_VALIDATOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/documents/*/reject").hasAnyRole("SUPER_ADMIN", "VALIDATOR", "DOCUMENT_VALIDATOR", "ADMIN")

                // Document type management - requires admin
                .requestMatchers(HttpMethod.POST, "/api/v1/document-types").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/document-types/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/document-types/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/document-types/**").hasAnyRole("SUPER_ADMIN", "USER", "VIEWER", "ADMIN")

                // Reports - requires specific roles
                .requestMatchers("/api/v1/documents/report/**").hasAnyRole("SUPER_ADMIN", "ANALYTICS_VIEWER", "COMPLIANCE_OFFICER", "ADMIN")
                .requestMatchers("/api/v1/documents/stats").hasAnyRole("SUPER_ADMIN", "ANALYTICS_VIEWER", "ADMIN")
                .requestMatchers("/api/v1/documents/dashboard").hasAnyRole("SUPER_ADMIN", "ANALYTICS_VIEWER", "ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )

            // Configure OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )

            // Stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    /**
     * Configures JWT authentication converter to extract roles from Keycloak tokens.
     *
     * @return JwtAuthenticationConverter with role extraction
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            java.util.List<String> roles = new java.util.ArrayList<>();

            // realm_access.roles
            java.util.Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                Object r = realmAccess.get("roles");
                if (r instanceof java.util.List<?>) {
                    for (Object o : (java.util.List<?>) r) if (o instanceof String s) roles.add(s);
                }
            }

            // direct roles claim
            java.util.List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) roles.addAll(directRoles);

            // resource_access.*.roles
            java.util.Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                resourceAccess.values().forEach(val -> {
                    if (val instanceof java.util.Map<?, ?> m) {
                        Object rr = m.get("roles");
                        if (rr instanceof java.util.List<?>) {
                            for (Object o : (java.util.List<?>) rr) if (o instanceof String s) roles.add(s);
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
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
        });
        return converter;
    }

    /**
     * Configures CORS settings for cross-origin requests.
     *
     * @return CorsConfigurationSource with allowed origins and methods
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (configure in production)
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:3001", "https://eflo.app", "https://*.eflo.app"));

        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose headers
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition",
            "X-Total-Count",
            "X-Correlation-Id"
        ));

        // Max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
