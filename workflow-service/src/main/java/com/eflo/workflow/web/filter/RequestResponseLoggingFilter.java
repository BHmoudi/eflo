package com.eflo.workflow.web.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String USERNAME_MDC_KEY = "username";

    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/actuator",
        "/swagger-ui",
        "/v3/api-docs",
        "/swagger-resources",
        "/webjars"
    );

    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
        "authorization",
        "x-api-key",
        "cookie",
        "set-cookie"
    );

    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password",
        "secret",
        "token",
        "apikey",
        "api_key",
        "creditcard",
        "ssn"
    );

    private final ObjectMapper objectMapper;

    @Value("${logging.request-response.enabled:true}")
    private boolean loggingEnabled;

    @Value("${logging.request-response.include-headers:true}")
    private boolean includeHeaders;

    @Value("${logging.request-response.include-payload:true}")
    private boolean includePayload;

    @Value("${logging.request-response.max-payload-length:1000}")
    private int maxPayloadLength;

    public RequestResponseLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !loggingEnabled || EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Add correlation ID to MDC
        String correlationId = getOrCreateCorrelationId(request);
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Add user info to MDC if available
        addUserInfoToMDC();

        // Wrap request and response for caching
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(requestWrapper, correlationId);

            // Continue filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            logResponse(responseWrapper, correlationId, duration);

            // Copy response body to actual response
            responseWrapper.copyBodyToResponse();

            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Clear MDC
            MDC.clear();
        }
    }

    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void addUserInfoToMDC() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    String userId = jwt.getSubject();
                    String username = jwt.getClaim("preferred_username");

                    if (userId != null) {
                        MDC.put(USER_ID_MDC_KEY, userId);
                    }
                    if (username != null) {
                        MDC.put(USERNAME_MDC_KEY, username);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract user info from security context", e);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        Map<String, Object> logData = new LinkedHashMap<>();

        logData.put("type", "REQUEST");
        logData.put("correlationId", correlationId);
        logData.put("method", request.getMethod());
        logData.put("uri", request.getRequestURI());
        logData.put("queryString", request.getQueryString());
        logData.put("remoteAddr", request.getRemoteAddr());

        // Add user info if available
        String userId = MDC.get(USER_ID_MDC_KEY);
        String username = MDC.get(USERNAME_MDC_KEY);
        if (userId != null || username != null) {
            Map<String, String> userInfo = new LinkedHashMap<>();
            if (userId != null) userInfo.put("userId", userId);
            if (username != null) userInfo.put("username", username);
            logData.put("user", userInfo);
        }

        // Add headers
        if (includeHeaders) {
            logData.put("headers", getFilteredHeaders(request));
        }

        // Add request body
        if (includePayload && request.getContentLength() > 0) {
            String payload = getRequestPayload(request);
            if (payload != null && !payload.isEmpty()) {
                logData.put("body", maskSensitiveData(payload));
            }
        }

        try {
            log.info("HTTP Request: {}", objectMapper.writeValueAsString(logData));
        } catch (Exception e) {
            log.error("Error logging request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, String correlationId, long duration) {
        Map<String, Object> logData = new LinkedHashMap<>();

        logData.put("type", "RESPONSE");
        logData.put("correlationId", correlationId);
        logData.put("status", response.getStatus());
        logData.put("durationMs", duration);

        // Add headers
        if (includeHeaders) {
            logData.put("headers", getFilteredHeaders(response));
        }

        // Add response body
        if (includePayload && response.getContentSize() > 0) {
            String payload = getResponsePayload(response);
            if (payload != null && !payload.isEmpty()) {
                logData.put("body", maskSensitiveData(payload));
            }
        }

        try {
            String logMessage = "HTTP Response: " + objectMapper.writeValueAsString(logData);
            if (response.getStatus() >= 500) {
                log.error(logMessage);
            } else if (response.getStatus() >= 400) {
                log.warn(logMessage);
            } else {
                log.info(logMessage);
            }
        } catch (Exception e) {
            log.error("Error logging response", e);
        }
    }

    private Map<String, String> getFilteredHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Collections.list(request.getHeaderNames()).forEach(headerName -> {
            String headerValue = request.getHeader(headerName);
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "***MASKED***");
            } else {
                headers.put(headerName, headerValue);
            }
        });
        return headers;
    }

    private Map<String, String> getFilteredHeaders(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        response.getHeaderNames().forEach(headerName -> {
            String headerValue = response.getHeader(headerName);
            if (isSensitiveHeader(headerName)) {
                headers.put(headerName, "***MASKED***");
            } else {
                headers.put(headerName, headerValue);
            }
        });
        return headers;
    }

    private boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.stream()
            .anyMatch(sensitive -> sensitive.equalsIgnoreCase(headerName));
    }

    private String getRequestPayload(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String payload = new String(buf, 0, Math.min(buf.length, maxPayloadLength),
                    request.getCharacterEncoding());
                return payload.length() < buf.length ? payload + "..." : payload;
            } catch (UnsupportedEncodingException e) {
                log.debug("Error reading request payload", e);
                return "[UNREADABLE]";
            }
        }
        return null;
    }

    private String getResponsePayload(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String payload = new String(buf, 0, Math.min(buf.length, maxPayloadLength),
                    response.getCharacterEncoding());
                return payload.length() < buf.length ? payload + "..." : payload;
            } catch (UnsupportedEncodingException e) {
                log.debug("Error reading response payload", e);
                return "[UNREADABLE]";
            }
        }
        return null;
    }

    private String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        String maskedData = data;
        for (String sensitiveField : SENSITIVE_FIELDS) {
            // Mask JSON fields like "password": "value" -> "password": "***MASKED***"
            maskedData = maskedData.replaceAll(
                "([\"']" + sensitiveField + "[\"']\\s*:\\s*[\"'])([^\"']+)([\"'])",
                "$1***MASKED***$3"
            );
        }
        return maskedData;
    }
}
