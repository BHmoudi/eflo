package com.eflo.external.gateway.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTestResult {
    private boolean success;
    private int statusCode;
    private String response;
    private String message;
}
