package com.eflo.workflow.web;

import com.eflo.workflow.service.TaskManagementService;
import com.eflo.workflow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook endpoints for receiving callbacks from n8n workflows
 * Handles results from:
 * - AI document analysis
 * - Task validation
 * - Notification delivery status
 * - File processing completion
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/n8n")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "n8n", name = "enabled", havingValue = "true", matchIfMissing = false)
public class N8nWebhookController {

    private final TaskManagementService taskManagementService;
    private final NotificationService notificationService;

    @Value("${n8n.webhook-secret:}")
    private String webhookSecret;

    /**
     * Callback from n8n when AI document analysis is completed
     *
     * Payload example:
     * {
     *   "documentId": "doc-123",
     *   "taskId": "task-456",
     *   "status": "completed",
     *   "analysisResult": {
     *     "invoiceNumber": "INV-2025-001",
     *     "totalAmount": 1500.00,
     *     ...
     *   }
     * }
     */
    @PostMapping("/document-analysis/callback")
    public ResponseEntity<Map<String, Object>> handleDocumentAnalysisCallback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody Map<String, Object> payload) {

        // Validate webhook secret
        if (!validateWebhookSecret(secret)) {
            log.warn("Invalid webhook secret for document analysis callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        try {
            String documentId = (String) payload.get("documentId");
            String taskId = (String) payload.get("taskId");
            String status = (String) payload.get("status");
            Map<String, Object> analysisResult = (Map<String, Object>) payload.get("analysisResult");

            log.info("Received document analysis callback - Document: {}, Task: {}, Status: {}",
                    documentId, taskId, status);

            if ("completed".equals(status) && analysisResult != null) {
                // Update task with analysis results
                taskManagementService.updateTaskWithAnalysisResult(taskId, analysisResult);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Document analysis result processed"
                ));
            } else if ("error".equals(status)) {
                String errorMessage = (String) payload.getOrDefault("error", "Unknown error");
                log.error("Document analysis failed for document {}: {}", documentId, errorMessage);

                return ResponseEntity.ok(Map.of(
                        "status", "error_logged",
                        "message", "Analysis error logged"
                ));
            }

            return ResponseEntity.ok(Map.of("status", "acknowledged"));

        } catch (Exception e) {
            log.error("Error processing document analysis callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Callback from n8n when task validation is completed
     *
     * Payload example:
     * {
     *   "taskId": "task-123",
     *   "status": "VALID" or "INVALID",
     *   "allValid": true/false,
     *   "validations": [...],
     *   "errors": [...]
     * }
     */
    @PostMapping("/task-validation/callback")
    public ResponseEntity<Map<String, Object>> handleTaskValidationCallback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody Map<String, Object> payload) {

        if (!validateWebhookSecret(secret)) {
            log.warn("Invalid webhook secret for task validation callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        try {
            String taskId = (String) payload.get("taskId");
            String status = (String) payload.get("status");
            Boolean allValid = (Boolean) payload.get("allValid");

            log.info("Received task validation callback - Task: {}, Status: {}, Valid: {}",
                    taskId, status, allValid);

            if (Boolean.TRUE.equals(allValid)) {
                // Task data is valid, proceed with workflow
                taskManagementService.completeTaskValidation(taskId, payload);
            } else {
                // Task data has validation errors
                taskManagementService.rejectTaskWithValidationErrors(taskId, payload);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Validation result processed"
            ));

        } catch (Exception e) {
            log.error("Error processing task validation callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Callback from n8n when notification is sent
     *
     * Payload example:
     * {
     *   "taskId": "task-123",
     *   "userId": "user-456",
     *   "channel": "WHATSAPP",
     *   "status": "SENT" or "FAILED",
     *   "messageSid": "SM...",
     *   "timestamp": "2025-10-10T10:00:00Z"
     * }
     */
    @PostMapping("/notification/callback")
    public ResponseEntity<Map<String, Object>> handleNotificationCallback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody Map<String, Object> payload) {

        if (!validateWebhookSecret(secret)) {
            log.warn("Invalid webhook secret for notification callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        try {
            String taskId = (String) payload.get("taskId");
            String userId = (String) payload.get("userId");
            String channel = (String) payload.get("channel");
            String status = (String) payload.get("status");
            String messageSid = (String) payload.get("messageSid");

            log.info("Received notification callback - Task: {}, User: {}, Channel: {}, Status: {}",
                    taskId, userId, channel, status);

            // Log notification delivery status
            notificationService.logNotificationStatus(taskId, userId, channel, status, messageSid);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Notification status logged"
            ));

        } catch (Exception e) {
            log.error("Error processing notification callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Callback from n8n when file processing is completed
     *
     * Payload example:
     * {
     *   "source": "whatsapp",
     *   "documentId": "doc-789",
     *   "workflowInstanceId": "wf-123",
     *   "status": "completed",
     *   "fileMetadata": {...}
     * }
     */
    @PostMapping("/file-processing/callback")
    public ResponseEntity<Map<String, Object>> handleFileProcessingCallback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody Map<String, Object> payload) {

        if (!validateWebhookSecret(secret)) {
            log.warn("Invalid webhook secret for file processing callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        try {
            String source = (String) payload.get("source");
            String documentId = (String) payload.get("documentId");
            String workflowInstanceId = (String) payload.get("workflowInstanceId");
            String status = (String) payload.get("status");

            log.info("Received file processing callback - Source: {}, Document: {}, Status: {}",
                    source, documentId, status);

            // Handle file processing completion
            // This might trigger next workflow step or update task status

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "File processing result acknowledged"
            ));

        } catch (Exception e) {
            log.error("Error processing file processing callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Generic callback endpoint for custom n8n workflows
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleGenericCallback(
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret,
            @RequestBody Map<String, Object> payload) {

        if (!validateWebhookSecret(secret)) {
            log.warn("Invalid webhook secret for generic callback");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid webhook secret"));
        }

        log.info("Received generic n8n callback: {}", payload);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Callback received"
        ));
    }

    /**
     * Health check endpoint for n8n to verify webhook connectivity
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "n8n-webhook-receiver",
                "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Validate webhook secret for security
     */
    private boolean validateWebhookSecret(String providedSecret) {
        // If no secret is configured, allow all requests (dev mode)
        if (webhookSecret == null || webhookSecret.isEmpty()) {
            log.warn("n8n webhook secret not configured - allowing all requests");
            return true;
        }

        // Validate provided secret matches configured secret
        return webhookSecret.equals(providedSecret);
    }
}
