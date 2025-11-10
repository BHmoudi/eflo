package com.eflo.workflow.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for integrating with n8n workflow automation platform
 * Provides methods to trigger n8n workflows for:
 * - AI document analysis
 * - WhatsApp notifications
 * - Task validation
 * - File processing
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "n8n", name = "enabled", havingValue = "true", matchIfMissing = false)
public class N8nClient {

    @Value("${n8n.base-url:http://n8n:5678}")
    private String n8nBaseUrl;

    @Value("${n8n.webhook-secret:}")
    private String webhookSecret;

    @Value("${n8n.timeout:30000}")
    private int timeout;

    private final RestTemplate restTemplate;

    public N8nClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Trigger AI document analysis workflow in n8n
     *
     * @param documentId         Document identifier
     * @param documentUrl        URL to download document
     * @param workflowInstanceId Workflow instance ID
     * @param taskId            Task ID to update upon completion
     * @return Analysis result from n8n
     */
    public Map<String, Object> triggerDocumentAnalysis(String documentId,
                                                      String documentUrl,
                                                      String workflowInstanceId,
                                                      String taskId) {
        log.info("Triggering n8n document analysis for document: {}", documentId);

        String url = n8nBaseUrl + "/webhook/document-analysis";

        Map<String, Object> payload = new HashMap<>();
        payload.put("documentId", documentId);
        payload.put("documentUrl", documentUrl);
        payload.put("workflowInstanceId", workflowInstanceId);
        payload.put("taskId", taskId);

        try {
            return executeWebhookWithResponse(url, payload);
        } catch (Exception e) {
            log.error("Failed to trigger document analysis in n8n", e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /**
     * Send WhatsApp notification via n8n
     *
     * @param taskId      Task ID
     * @param taskName    Name of the task
     * @param taskUrl     URL to access the task
     * @param orderId     Associated order ID (optional)
     * @param priority    Task priority (optional)
     * @param dueDate     Task due date (optional)
     */
    public void sendWhatsAppNotification(String taskId,
                                        String taskName,
                                        String taskUrl,
                                        String orderId,
                                        String priority,
                                        String dueDate) {
        log.info("Triggering WhatsApp notification via n8n for task: {}", taskId);

        String url = n8nBaseUrl + "/webhook/whatsapp-send";

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("taskName", taskName);
        payload.put("taskUrl", taskUrl);

        if (orderId != null) payload.put("orderId", orderId);
        if (priority != null) payload.put("priority", priority);
        if (dueDate != null) payload.put("dueDate", dueDate);

        // Execute asynchronously - we don't wait for response
        executeWebhookAsync(url, payload);
    }

    /**
     * Validate task data using external APIs via n8n
     *
     * @param taskId   Task ID
     * @param taskData Map of task data to validate
     * @return Validation results
     */
    public Map<String, Object> validateTaskData(String taskId, Map<String, Object> taskData) {
        log.info("Triggering task validation via n8n for task: {}", taskId);

        String url = n8nBaseUrl + "/webhook/task-validation";

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("taskData", taskData);

        try {
            return executeWebhookWithResponse(url, payload);
        } catch (Exception e) {
            log.error("Failed to validate task in n8n", e);
            return Map.of(
                "status", "error",
                "message", e.getMessage(),
                "allValid", false
            );
        }
    }

    /**
     * Trigger file reception and processing workflow
     *
     * @param source             Source of file (whatsapp, email, upload)
     * @param fileUrl            URL to download file
     * @param fileName           Original file name
     * @param clientIdentifier   Client phone/email
     * @param workflowInstanceId Associated workflow instance
     * @return Processing result
     */
    public Map<String, Object> triggerFileReception(String source,
                                                   String fileUrl,
                                                   String fileName,
                                                   String clientIdentifier,
                                                   String workflowInstanceId) {
        log.info("Triggering file reception workflow via n8n: {}", fileName);

        String url = n8nBaseUrl + "/webhook/file-reception";

        Map<String, Object> payload = new HashMap<>();
        payload.put("source", source);
        payload.put("fileUrl", fileUrl);
        payload.put("fileName", fileName);
        payload.put("clientIdentifier", clientIdentifier);
        payload.put("workflowInstanceId", workflowInstanceId);

        try {
            return executeWebhookWithResponse(url, payload);
        } catch (Exception e) {
            log.error("Failed to trigger file reception in n8n", e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    /**
     * Send multi-channel notification (WhatsApp, Email, SMS)
     *
     * @param userId      User ID to notify
     * @param message     Message content
     * @param subject     Subject line (for email)
     * @param priority    Priority level
     * @param workflowId  Associated workflow ID
     */
    public void sendMultiChannelNotification(String userId,
                                            String message,
                                            String subject,
                                            String priority,
                                            String workflowId) {
        log.info("Triggering multi-channel notification via n8n for user: {}", userId);

        String url = n8nBaseUrl + "/webhook/multi-channel-notification";

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("message", message);
        payload.put("subject", subject);
        payload.put("priority", priority);
        payload.put("workflowId", workflowId);

        executeWebhookAsync(url, payload);
    }

    /**
     * Generic webhook execution with response
     */
    private Map<String, Object> executeWebhookWithResponse(String url, Map<String, Object> payload) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.warn("n8n webhook returned non-success status: {}", response.getStatusCode());
                return Map.of("status", "error", "message", "Webhook returned " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            log.error("Failed to execute n8n webhook: {}", url, e);
            throw e;
        }
    }

    /**
     * Asynchronous webhook execution (fire and forget)
     */
    private void executeWebhookAsync(String url, Map<String, Object> payload) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // Execute in separate thread to avoid blocking
        new Thread(() -> {
            try {
                restTemplate.postForObject(url, request, String.class);
                log.debug("Successfully triggered n8n webhook: {}", url);
            } catch (Exception e) {
                // Log error but don't fail - n8n workflows are async
                log.error("Failed to trigger n8n webhook: {}", url, e);
            }
        }).start();
    }

    /**
     * Create HTTP headers for n8n webhook requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add webhook secret for authentication if configured
        if (webhookSecret != null && !webhookSecret.isEmpty()) {
            headers.set("X-Webhook-Secret", webhookSecret);
        }

        return headers;
    }

    /**
     * Check if n8n is enabled and reachable
     */
    public boolean isEnabled() {
        try {
            String healthUrl = n8nBaseUrl + "/healthz";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("n8n health check failed", e);
            return false;
        }
    }
}
