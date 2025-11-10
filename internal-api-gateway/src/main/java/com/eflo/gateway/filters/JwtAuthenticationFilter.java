package com.eflo.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JWT Authentication Filter
 *
 * Global filter that validates JWT tokens from Keycloak and extracts user context.
 * User information is propagated to downstream services via custom headers.
 *
 * Features:
 * - JWT token validation with Keycloak
 * - User context extraction (ID, roles, employee number, business units)
 * - Custom headers for downstream services
 * - Public endpoint bypass
 * - Comprehensive error handling
 *
 * @author Eflo Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final ReactiveJwtDecoder jwtDecoder;
    private final boolean enforceAuth;

    public JwtAuthenticationFilter(ReactiveJwtDecoder jwtDecoder,
                                   @Value("${gateway.auth.enforce:false}") boolean enforceAuth) {
        this.jwtDecoder = jwtDecoder;
        this.enforceAuth = enforceAuth;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.getPath().value())) {
            log.debug("Public endpoint accessed: {}", request.getPath());
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String token = extractToken(request);

        if (token == null) {
            if (enforceAuth) {
                log.warn("Missing Authorization header for request: {}", request.getPath());
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }
            // Pass-through mode: don't block, just continue
            return chain.filter(exchange);
        }

        // Validate and decode JWT reactively
        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    // Extract user information
                    String userId = jwt.getSubject();
                    List<String> roles = extractRoles(jwt);
                    String employeeNumber = jwt.getClaimAsString("employeeNumber");
                    String businessUnitIds = jwt.getClaimAsString("businessUnitIds");
                    String email = jwt.getClaimAsString("email");
                    String username = jwt.getClaimAsString("preferred_username");

                    // Add user context to request headers for downstream services
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header("X-User-Id", userId != null ? userId : "")
                            .header("X-User-Roles", String.join(",", roles))
                            .header("X-Employee-Number", employeeNumber != null ? employeeNumber : "")
                            .header("X-Business-Units", businessUnitIds != null ? businessUnitIds : "")
                            .header("X-User-Email", email != null ? email : "")
                            .header("X-Username", username != null ? username : "")
                            .header("X-Authenticated", "true")
                            .build();

                    log.debug("User authenticated: {} with roles: {}", userId, roles);

                    // Continue filter chain with mutated request
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(JwtException.class, e -> {
                    log.error("Invalid JWT token for request: {}", request.getPath(), e);
                    if (enforceAuth) {
                        return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
                    }
                    // Pass-through mode: do not block on decode errors
                    return chain.filter(exchange);
                });
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            return (List<String>) realmAccess.get("roles");
        }
        return Collections.emptyList();
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/actuator/") ||
               path.startsWith("/fallback/") ||
               path.equals("/health") ||
               path.equals("/info");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorJson = String.format("{\"error\":\"%s\",\"status\":%d,\"timestamp\":\"%s\"}",
                message, status.value(), java.time.Instant.now());

        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // Execute early in the filter chain
    }
}
