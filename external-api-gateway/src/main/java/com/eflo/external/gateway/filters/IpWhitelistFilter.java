package com.eflo.external.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

@Component
@Slf4j
public class IpWhitelistFilter implements GlobalFilter, Ordered {

    private final Map<String, List<String>> clientIpWhitelist;

    public IpWhitelistFilter(@Value("#{${eflo.external-gateway.ip-whitelist.clients:{}}}") Map<String, Object> clients) {
        this.clientIpWhitelist = parseIpWhitelistConfig(clients);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Skip IP check for health endpoints
        if (isHealthEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        // Extract client ID from JWT (will be available after authentication)
        String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");

        if (clientId == null) {
            // Client ID will be set by authentication filter
            return chain.filter(exchange);
        }

        // Get client IP address
        String clientIp = getClientIp(exchange.getRequest());

        // Check if IP is whitelisted for this client
        List<String> allowedIps = clientIpWhitelist.get(clientId);

        if (allowedIps == null || !allowedIps.contains(clientIp)) {
            log.warn("IP not whitelisted: clientId={}, ip={}, path={}",
                    clientId, clientIp, exchange.getRequest().getPath());

            auditLog(exchange, "IP_NOT_WHITELISTED", "IP address not authorized");
            return onError(exchange, "IP address not authorized", HttpStatus.FORBIDDEN);
        }

        log.debug("IP whitelist check passed: clientId={}, ip={}", clientId, clientIp);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -200;
    }

    private Map<String, List<String>> parseIpWhitelistConfig(Map<String, Object> clients) {
        Map<String, List<String>> whitelist = new HashMap<>();

        if (clients == null || clients.isEmpty()) {
            log.info("No IP whitelist configuration found. All IPs will be allowed.");
            return whitelist;
        }

        for (Map.Entry<String, Object> entry : clients.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> clientConfig = (Map<String, Object>) entry.getValue();

            if (clientConfig.containsKey("ips")) {
                @SuppressWarnings("unchecked")
                List<String> ips = (List<String>) clientConfig.get("ips");
                whitelist.put(entry.getKey(), new ArrayList<>(ips));
            }
        }

        return whitelist;
    }

    private boolean isHealthEndpoint(ServerHttpRequest request) {
        String path = request.getPath().value();
        return path.startsWith("/actuator/") || path.equals("/health");
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private void auditLog(ServerWebExchange exchange, String event, String message) {
        log.info("AUDIT: event={}, message={}, path={}, ip={}",
                event, message, exchange.getRequest().getPath(), getClientIp(exchange.getRequest()));
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
