package com.eflo.external.gateway.filters;

import lombok.extern.slf4j.Slf4j;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ClientCredentialsFilter implements GlobalFilter, Ordered {

    private final ReactiveJwtDecoder jwtDecoder;

    public ClientCredentialsFilter(ReactiveJwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip authentication for health endpoints
        if (isHealthEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());

        if (token == null) {
            log.warn("Missing Authorization header for external request: {}", exchange.getRequest().getPath());
            auditLog(exchange, "MISSING_AUTH", "Missing Authorization header");
            return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
        }

        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    // Validate this is a client credentials token
                    if (!isClientCredentialsToken(jwt)) {
                        log.warn("Invalid token type for external request: {}", exchange.getRequest().getPath());
                        auditLog(exchange, "INVALID_TOKEN_TYPE", "Token is not client credentials");
                        return onError(exchange, "Invalid token type", HttpStatus.UNAUTHORIZED);
                    }

                    String clientId = jwt.getSubject();
                    List<String> scopes = extractScopes(jwt);
                    String audience = extractAudience(jwt);

                    // Add client context to request headers
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-Client-Id", clientId)
                    .header("X-Client-Scopes", String.join(",", scopes))
                    .header("X-Audience", audience)
                            .header("X-Authenticated", "true")
                            .header("X-Token-Type", "client-credentials")
                            .build();

                    log.debug("Client authenticated: {} with scopes: {}", clientId, scopes);

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(JwtException.class, e -> {
                    log.error("Invalid JWT token for external request: {}", exchange.getRequest().getPath(), e);
                    auditLog(exchange, "INVALID_JWT", "Invalid JWT token: " + e.getMessage());
                    return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private boolean isClientCredentialsToken(Jwt jwt) {
        String grantType = jwt.getClaimAsString("grant_type");
        return "client_credentials".equals(grantType);
    }

    private List<String> extractScopes(Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if (scope != null && !scope.isEmpty()) {
            return Arrays.asList(scope.split(" "));
        }
        return Collections.emptyList();
    }

    private String extractAudience(Jwt jwt) {
        List<String> aud = jwt.getClaimAsStringList("aud");
        return aud != null && !aud.isEmpty() ? aud.get(0) : "";
    }

    private boolean isHealthEndpoint(ServerHttpRequest request) {
        String path = request.getPath().value();
        return path.startsWith("/actuator/") || path.equals("/health");
    }

    private void auditLog(ServerWebExchange exchange, String event, String message) {
        log.info("AUDIT: event={}, message={}, path={}, ip={}",
                event, message, exchange.getRequest().getPath(), getClientIp(exchange.getRequest()));
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        String errorResponse = String.format("""
            {
              "timestamp": "%s",
              "status": %d,
              "error": "%s",
              "message": "%s",
              "path": "%s",
              "requestId": "%s"
            }
            """,
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath(),
                exchange.getRequest().getHeaders().getFirst("X-Request-Id")
        );

        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        response.getHeaders().add("Content-Type", "application/json");

        return response.writeWith(Mono.just(buffer));
    }
}
