package com.eflo.external.gateway.controllers;

import com.eflo.external.gateway.models.WebhookSubscription;
import com.eflo.external.gateway.models.WebhookSubscriptionRequest;
import com.eflo.external.gateway.models.WebhookTestRequest;
import com.eflo.external.gateway.models.WebhookTestResult;
import com.eflo.external.gateway.services.WebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/external/webhook")
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<WebhookSubscription> subscribeWebhook(
            @RequestBody WebhookSubscriptionRequest request,
            @RequestHeader("X-Client-Id") String clientId) {

        log.info("Webhook subscription request from client: {}", clientId);

        try {
            WebhookSubscription subscription = webhookService.subscribeWebhook(clientId, request);
            return ResponseEntity.ok(subscription);

        } catch (Exception e) {
            log.error("Failed to subscribe webhook for client: {}", clientId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<WebhookSubscription>> getWebhookSubscriptions(
            @RequestHeader("X-Client-Id") String clientId) {

        List<WebhookSubscription> subscriptions = webhookService.getWebhookSubscriptions(clientId);
        return ResponseEntity.ok(subscriptions);
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> unsubscribeWebhook(
            @PathVariable String subscriptionId,
            @RequestHeader("X-Client-Id") String clientId) {

        try {
            webhookService.unsubscribeWebhook(clientId, subscriptionId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Failed to unsubscribe webhook: {}", subscriptionId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/test")
    public ResponseEntity<WebhookTestResult> testWebhook(
            @RequestBody WebhookTestRequest request,
            @RequestHeader("X-Client-Id") String clientId) {

        try {
            WebhookTestResult result = webhookService.testWebhook(clientId, request);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Failed to test webhook for client: {}", clientId, e);
            return ResponseEntity.badRequest().build();
        }
    }
}
