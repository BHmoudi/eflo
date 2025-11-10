package com.eflo.external.gateway.services;

import com.eflo.external.gateway.models.WebhookSubscription;
import com.eflo.external.gateway.models.WebhookSubscriptionRequest;
import com.eflo.external.gateway.models.WebhookTestRequest;
import com.eflo.external.gateway.models.WebhookTestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WebhookService {

    private final Map<String, List<WebhookSubscription>> clientSubscriptions = new ConcurrentHashMap<>();
    private final WebClient webClient;

    public WebhookService() {
        this.webClient = WebClient.builder().build();
    }

    public WebhookSubscription subscribeWebhook(String clientId, WebhookSubscriptionRequest request) {
        log.info("Creating webhook subscription for client: {}", clientId);

        WebhookSubscription subscription = WebhookSubscription.builder()
                .id(UUID.randomUUID().toString())
                .clientId(clientId)
                .events(request.getEvents())
                .url(request.getUrl())
                .secret(request.getSecret())
                .active(true)
                .createdAt(Instant.now())
                .build();

        clientSubscriptions.computeIfAbsent(clientId, k -> new ArrayList<>()).add(subscription);

        log.info("Webhook subscription created: id={}, clientId={}, url={}",
                subscription.getId(), clientId, request.getUrl());

        return subscription;
    }

    public List<WebhookSubscription> getWebhookSubscriptions(String clientId) {
        return clientSubscriptions.getOrDefault(clientId, Collections.emptyList());
    }

    public void unsubscribeWebhook(String clientId, String subscriptionId) {
        List<WebhookSubscription> subscriptions = clientSubscriptions.get(clientId);
        if (subscriptions != null) {
            subscriptions.removeIf(sub -> sub.getId().equals(subscriptionId));
            log.info("Webhook subscription removed: id={}, clientId={}", subscriptionId, clientId);
        }
    }

    public WebhookTestResult testWebhook(String clientId, WebhookTestRequest request) {
        log.info("Testing webhook for client: {}", clientId);

        try {
            String payload = "{\"test\": true, \"message\": \"Webhook test\"}";
            String signature = generateSignature(payload, request.getSecret());

            String response = webClient.post()
                    .uri(request.getUrl())
                    .header("X-Webhook-Signature", signature)
                    .header("X-Event-Type", "test")
                    .header("Content-Type", "application/json")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return WebhookTestResult.builder()
                    .success(true)
                    .statusCode(200)
                    .response(response)
                    .message("Webhook test successful")
                    .build();

        } catch (Exception e) {
            log.error("Webhook test failed for client: {}", clientId, e);
            return WebhookTestResult.builder()
                    .success(false)
                    .statusCode(0)
                    .message("Webhook test failed: " + e.getMessage())
                    .build();
        }
    }

    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
