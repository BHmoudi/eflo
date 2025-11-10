package com.eflo.external.gateway.filters;

import com.eflo.external.gateway.models.AuditRequest;
import com.eflo.external.gateway.models.AuditResponse;
import com.eflo.external.gateway.services.AuditLoggingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AuditLoggingFilter implements GlobalFilter, Ordered {

    private final AuditLoggingService auditService;

    public AuditLoggingFilter(AuditLoggingService auditService) {
        this.auditService = auditService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        long startTime = System.currentTimeMillis();

        // Capture request details
        AuditRequest auditRequest = AuditRequest.builder()
                .requestId(request.getHeaders().getFirst("X-Request-Id"))
                .clientId(request.getHeaders().getFirst("X-Client-Id"))
                .sourceIp(getClientIp(request))
                .userAgent(request.getHeaders().getFirst("User-Agent"))
                .method(request.getMethod().name())
                .path(request.getPath().value())
                .queryParams(request.getQueryParams().toString())
                .headers(extractRelevantHeaders(request))
                .timestamp(Instant.now())
                .build();

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long processingTime = System.currentTimeMillis() - startTime;
                    ServerHttpResponse response = exchange.getResponse();

                    AuditResponse auditResponse = AuditResponse.builder()
                            .status(response.getStatusCode() != null ? response.getStatusCode().value() : 0)
                            .processingTimeMs(processingTime)
                            .timestamp(Instant.now())
                            .build();

                    // Log audit event
                    auditService.logAuditEvent(auditRequest, auditResponse);
                })
        );
    }

    @Override
    public int getOrder() {
        return 1000;
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private Map<String, String> extractRelevantHeaders(ServerHttpRequest request) {
        Map<String, String> headers = new HashMap<>();

        // Extract relevant headers for audit
        String[] relevantHeaders = {
                "Authorization", "Content-Type", "X-API-Key", "X-Client-Version",
                "X-Webhook-Signature", "Accept", "User-Agent"
        };

        for (String headerName : relevantHeaders) {
            String value = request.getHeaders().getFirst(headerName);
            if (value != null) {
                // Mask sensitive headers
                if ("Authorization".equals(headerName)) {
                    headers.put(headerName, maskAuthorization(value));
                } else {
                    headers.put(headerName, value);
                }
            }
        }

        return headers;
    }

    private String maskAuthorization(String auth) {
        if (auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (token.length() > 10) {
                return "Bearer " + token.substring(0, 10) + "...";
            }
        }
        return "Bearer [MASKED]";
    }
}
