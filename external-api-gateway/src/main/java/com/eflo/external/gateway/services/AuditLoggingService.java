package com.eflo.external.gateway.services;

import com.eflo.external.gateway.models.AuditEvent;
import com.eflo.external.gateway.models.AuditRequest;
import com.eflo.external.gateway.models.AuditResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditLoggingService {

    private final ObjectMapper objectMapper;

    public AuditLoggingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public void logAuditEvent(AuditRequest request, AuditResponse response) {
        try {
            AuditEvent auditEvent = AuditEvent.builder()
                    .timestamp(request.getTimestamp())
                    .requestId(request.getRequestId())
                    .clientId(request.getClientId())
                    .sourceIp(request.getSourceIp())
                    .userAgent(request.getUserAgent())
                    .method(request.getMethod())
                    .path(request.getPath())
                    .queryParams(request.getQueryParams())
                    .headers(request.getHeaders())
                    .responseStatus(response.getStatus())
                    .processingTimeMs(response.getProcessingTimeMs())
                    .build();

            String auditJson = objectMapper.writeValueAsString(auditEvent);
            log.info("AUDIT_EVENT: {}", auditJson);

        } catch (Exception e) {
            log.error("Failed to log audit event", e);
        }
    }
}
