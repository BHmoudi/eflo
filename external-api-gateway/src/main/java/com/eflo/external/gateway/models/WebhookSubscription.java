package com.eflo.external.gateway.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookSubscription {
    private String id;
    private String clientId;
    private List<String> events;
    private String url;
    private String secret;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
