package com.eflo.user.integration;

import com.eflo.user.integration.dto.DocumentDTO;
import com.eflo.user.integration.dto.OrderDTO;
import com.eflo.user.integration.dto.WorkflowInstanceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for orchestrating calls to external microservices.
 * Provides a unified interface for accessing data from Order, Workflow, Document, and Commission services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalServiceIntegrationService {

    private final OrderServiceClient orderServiceClient;
    private final WorkflowServiceClient workflowServiceClient;
    private final DocumentServiceClient documentServiceClient;
    private final CommissionServiceClient commissionServiceClient;

    // ========== Order Service Integration ==========

    /**
     * Get all orders for a specific user.
     *
     * @param userId the user ID
     * @return list of orders
     */
    public List<OrderDTO> getUserOrders(String userId) {
        log.debug("Fetching orders for user: {}", userId);
        try {
            return orderServiceClient.getOrdersByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching orders for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch user orders", e);
        }
    }

    /**
     * Get orders for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of orders
     */
    public List<OrderDTO> getBusinessUnitOrders(String businessUnitId) {
        log.debug("Fetching orders for business unit: {}", businessUnitId);
        try {
            return orderServiceClient.getOrdersByBusinessUnitId(businessUnitId);
        } catch (Exception e) {
            log.error("Error fetching orders for business unit: {}", businessUnitId, e);
            throw new RuntimeException("Failed to fetch business unit orders", e);
        }
    }

    /**
     * Validate user has access to a specific order.
     *
     * @param userId  the user ID
     * @param orderId the order ID
     * @return true if user has access
     */
    public boolean validateUserOrderAccess(String userId, String orderId) {
        log.debug("Validating user {} access to order {}", userId, orderId);
        try {
            Boolean hasAccess = orderServiceClient.checkUserAccess(orderId, userId);
            return hasAccess != null && hasAccess;
        } catch (Exception e) {
            log.error("Error validating user order access: userId={}, orderId={}", userId, orderId, e);
            return false;
        }
    }

    // ========== Workflow Service Integration ==========

    /**
     * Get all workflows for a specific user.
     *
     * @param userId the user ID
     * @return list of workflow instances
     */
    public List<WorkflowInstanceDTO> getUserWorkflows(String userId) {
        log.debug("Fetching workflows for user: {}", userId);
        try {
            return workflowServiceClient.getWorkflowsByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching workflows for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch user workflows", e);
        }
    }

    /**
     * Get pending workflow tasks for a user.
     *
     * @param userId the user ID
     * @return list of pending workflow instances
     */
    public List<WorkflowInstanceDTO> getUserPendingWorkflows(String userId) {
        log.debug("Fetching pending workflows for user: {}", userId);
        try {
            return workflowServiceClient.getPendingWorkflowsByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching pending workflows for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch user pending workflows", e);
        }
    }

    /**
     * Get workflows for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of workflow instances
     */
    public List<WorkflowInstanceDTO> getBusinessUnitWorkflows(String businessUnitId) {
        log.debug("Fetching workflows for business unit: {}", businessUnitId);
        try {
            return workflowServiceClient.getWorkflowsByBusinessUnitId(businessUnitId);
        } catch (Exception e) {
            log.error("Error fetching workflows for business unit: {}", businessUnitId, e);
            throw new RuntimeException("Failed to fetch business unit workflows", e);
        }
    }

    /**
     * Validate user has access to a specific workflow.
     *
     * @param userId     the user ID
     * @param workflowId the workflow instance ID
     * @return true if user has access
     */
    public boolean validateUserWorkflowAccess(String userId, String workflowId) {
        log.debug("Validating user {} access to workflow {}", userId, workflowId);
        try {
            Boolean hasAccess = workflowServiceClient.checkUserAccess(workflowId, userId);
            return hasAccess != null && hasAccess;
        } catch (Exception e) {
            log.error("Error validating user workflow access: userId={}, workflowId={}", userId, workflowId, e);
            return false;
        }
    }

    // ========== Document Service Integration ==========

    /**
     * Get all documents for a specific user.
     *
     * @param userId the user ID
     * @return list of documents
     */
    public List<DocumentDTO> getUserDocuments(String userId) {
        log.debug("Fetching documents for user: {}", userId);
        try {
            return documentServiceClient.getDocumentsByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching documents for user: {}", userId, e);
            throw new RuntimeException("Failed to fetch user documents", e);
        }
    }

    /**
     * Get documents for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return list of documents
     */
    public List<DocumentDTO> getBusinessUnitDocuments(String businessUnitId) {
        log.debug("Fetching documents for business unit: {}", businessUnitId);
        try {
            return documentServiceClient.getDocumentsByBusinessUnitId(businessUnitId);
        } catch (Exception e) {
            log.error("Error fetching documents for business unit: {}", businessUnitId, e);
            throw new RuntimeException("Failed to fetch business unit documents", e);
        }
    }

    /**
     * Get documents for a specific order.
     *
     * @param orderId the order ID
     * @return list of documents
     */
    public List<DocumentDTO> getOrderDocuments(String orderId) {
        log.debug("Fetching documents for order: {}", orderId);
        try {
            return documentServiceClient.getDocumentsByOrderId(orderId);
        } catch (Exception e) {
            log.error("Error fetching documents for order: {}", orderId, e);
            throw new RuntimeException("Failed to fetch order documents", e);
        }
    }

    /**
     * Validate user has access to a specific document.
     *
     * @param userId     the user ID
     * @param documentId the document ID
     * @return true if user has access
     */
    public boolean validateUserDocumentAccess(String userId, String documentId) {
        log.debug("Validating user {} access to document {}", userId, documentId);
        try {
            Boolean hasAccess = documentServiceClient.checkUserAccess(documentId, userId);
            return hasAccess != null && hasAccess;
        } catch (Exception e) {
            log.error("Error validating user document access: userId={}, documentId={}", userId, documentId, e);
            return false;
        }
    }

    // ========== Commission Service Integration ==========

    /**
     * Get commission summary for a user.
     *
     * @param userId the user ID
     * @return commission summary map
     */
    public Map<String, BigDecimal> getUserCommissionSummary(String userId) {
        log.debug("Fetching commission summary for user: {}", userId);
        try {
            return commissionServiceClient.getCommissionSummaryByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching commission summary for user: {}", userId, e);
            Map<String, BigDecimal> emptySummary = new HashMap<>();
            emptySummary.put("total", BigDecimal.ZERO);
            emptySummary.put("pending", BigDecimal.ZERO);
            emptySummary.put("paid", BigDecimal.ZERO);
            return emptySummary;
        }
    }

    /**
     * Get total commissions for a user.
     *
     * @param userId the user ID
     * @return total commission amount
     */
    public BigDecimal getUserTotalCommissions(String userId) {
        log.debug("Fetching total commissions for user: {}", userId);
        try {
            return commissionServiceClient.getTotalCommissionsByUserId(userId);
        } catch (Exception e) {
            log.error("Error fetching total commissions for user: {}", userId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Get commission summary for a business unit.
     *
     * @param businessUnitId the business unit ID
     * @return commission summary map
     */
    public Map<String, BigDecimal> getBusinessUnitCommissionSummary(String businessUnitId) {
        log.debug("Fetching commission summary for business unit: {}", businessUnitId);
        try {
            return commissionServiceClient.getCommissionsByBusinessUnitId(businessUnitId);
        } catch (Exception e) {
            log.error("Error fetching commission summary for business unit: {}", businessUnitId, e);
            Map<String, BigDecimal> emptySummary = new HashMap<>();
            emptySummary.put("total", BigDecimal.ZERO);
            emptySummary.put("pending", BigDecimal.ZERO);
            emptySummary.put("paid", BigDecimal.ZERO);
            return emptySummary;
        }
    }

    /**
     * Check if user has pending commissions.
     *
     * @param userId the user ID
     * @return true if user has pending commissions
     */
    public boolean userHasPendingCommissions(String userId) {
        log.debug("Checking if user {} has pending commissions", userId);
        try {
            Boolean hasPending = commissionServiceClient.hasPendingCommissions(userId);
            return hasPending != null && hasPending;
        } catch (Exception e) {
            log.error("Error checking pending commissions for user: {}", userId, e);
            return false;
        }
    }

    // ========== Aggregated Data ==========

    /**
     * Get comprehensive user activity data across all services.
     *
     * @param userId the user ID
     * @return map with aggregated user data
     */
    public Map<String, Object> getUserActivityAggregation(String userId) {
        log.debug("Fetching aggregated user activity for user: {}", userId);

        Map<String, Object> aggregation = new HashMap<>();

        try {
            aggregation.put("orders", getUserOrders(userId));
        } catch (Exception e) {
            log.warn("Failed to fetch orders for user aggregation: {}", userId);
            aggregation.put("orders", List.of());
        }

        try {
            aggregation.put("workflows", getUserWorkflows(userId));
            aggregation.put("pendingWorkflows", getUserPendingWorkflows(userId));
        } catch (Exception e) {
            log.warn("Failed to fetch workflows for user aggregation: {}", userId);
            aggregation.put("workflows", List.of());
            aggregation.put("pendingWorkflows", List.of());
        }

        try {
            aggregation.put("documents", getUserDocuments(userId));
        } catch (Exception e) {
            log.warn("Failed to fetch documents for user aggregation: {}", userId);
            aggregation.put("documents", List.of());
        }

        try {
            aggregation.put("commissions", getUserCommissionSummary(userId));
        } catch (Exception e) {
            log.warn("Failed to fetch commissions for user aggregation: {}", userId);
            aggregation.put("commissions", Map.of());
        }

        return aggregation;
    }

    /**
     * Get comprehensive business unit activity data across all services.
     *
     * @param businessUnitId the business unit ID
     * @return map with aggregated business unit data
     */
    public Map<String, Object> getBusinessUnitActivityAggregation(String businessUnitId) {
        log.debug("Fetching aggregated business unit activity for: {}", businessUnitId);

        Map<String, Object> aggregation = new HashMap<>();

        try {
            aggregation.put("orders", getBusinessUnitOrders(businessUnitId));
        } catch (Exception e) {
            log.warn("Failed to fetch orders for business unit aggregation: {}", businessUnitId);
            aggregation.put("orders", List.of());
        }

        try {
            aggregation.put("workflows", getBusinessUnitWorkflows(businessUnitId));
        } catch (Exception e) {
            log.warn("Failed to fetch workflows for business unit aggregation: {}", businessUnitId);
            aggregation.put("workflows", List.of());
        }

        try {
            aggregation.put("documents", getBusinessUnitDocuments(businessUnitId));
        } catch (Exception e) {
            log.warn("Failed to fetch documents for business unit aggregation: {}", businessUnitId);
            aggregation.put("documents", List.of());
        }

        try {
            aggregation.put("commissions", getBusinessUnitCommissionSummary(businessUnitId));
        } catch (Exception e) {
            log.warn("Failed to fetch commissions for business unit aggregation: {}", businessUnitId);
            aggregation.put("commissions", Map.of());
        }

        return aggregation;
    }
}
