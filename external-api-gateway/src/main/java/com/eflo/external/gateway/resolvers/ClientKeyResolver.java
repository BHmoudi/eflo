package com.eflo.external.gateway.resolvers;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Client Key Resolver for Rate Limiting
 *
 * Resolves the rate limit key based on client ID from authentication
 * or falls back to IP address for unauthenticated requests.
 */
@Component
public class ClientKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        // Get client ID from authentication filter
        String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");

        if (clientId != null && !clientId.isEmpty()) {
            return Mono.just("client:" + clientId);
        }

        // Fallback to IP address for unauthenticated requests
        String ipAddress = getClientIpAddress(exchange);
        return Mono.just("ip:" + ipAddress);
    }

    private String getClientIpAddress(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null ?
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
}
