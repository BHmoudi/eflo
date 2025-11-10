package com.eflo.external.gateway.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRequest {
    private String requestId;
    private String clientId;
    private String sourceIp;
    private String userAgent;
    private String method;
    private String path;
    private String queryParams;
    private Map<String, String> headers;
    private Instant timestamp;
}
