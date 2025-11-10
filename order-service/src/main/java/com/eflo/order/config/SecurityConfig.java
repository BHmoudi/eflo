package com.eflo.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = new java.util.ArrayList<>();

            // 1) Realm roles: realm_access.roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null) {
                Object r = realmAccess.get("roles");
                if (r instanceof List<?>) {
                    for (Object o : (List<?>) r) if (o instanceof String s) roles.add(s);
                }
            }

            // 2) Direct roles claim (custom mapper): roles: [..]
            List<String> directRoles = jwt.getClaimAsStringList("roles");
            if (directRoles != null) roles.addAll(directRoles);

            // 3) Client roles: resource_access.{client}.roles
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
