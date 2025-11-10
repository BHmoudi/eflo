package com.eflo.docgen.service;

import com.eflo.docgen.domain.entity.DocumentTemplate;
import com.eflo.docgen.domain.entity.GeneratedDocument;
import com.eflo.docgen.domain.enums.GenerationStatus;
import com.eflo.docgen.domain.enums.TemplateFormat;
import com.eflo.docgen.domain.repository.DocumentTemplateRepository;
import com.eflo.docgen.domain.repository.GeneratedDocumentRepository;
import com.eflo.docgen.integration.OrderServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Document Generation Service
 *
 * Core service for generating documents from templates
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final DocumentTemplateRepository templateRepository;
    private final GeneratedDocumentRepository generatedDocRepository;
    private final PlaceholderResolverService placeholderResolver;
    private final PdfRenderingService pdfRenderer;
    private final OrderServiceClient orderServiceClient;

    /**
     * Generate document from template for an order
     */
    @Transactional
    public GeneratedDocument generateDocument(String templateCode, Long orderId, Long workflowInstanceId, String generatedBy) {
        log.info("Generating document: template={}, orderId={}, workflowInstanceId={}", templateCode, orderId, workflowInstanceId);

        // Load template
        DocumentTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateCode));

        if (!template.getIsActive()) {
            throw new RuntimeException("Template is not active: " + templateCode);
        }

        // Create generation record
        GeneratedDocument genDoc = GeneratedDocument.builder()
                .template(template)
                .orderId(orderId)
                .workflowInstanceId(workflowInstanceId)
                .generationStatus(GenerationStatus.PROCESSING)
                .requiresApproval(template.getRequiresApproval())
                .approvalRoles(template.getApprovalRoles())
                .generatedBy(generatedBy != null ? generatedBy : "SYSTEM")
                .build();

        generatedDocRepository.save(genDoc);

        try {
            // Fetch order data
            Map<String, Object> orderData = fetchOrderData(orderId);

            // Generate document content
            byte[] content = generateContent(template, orderData);

            // Determine filename and mime type
            String filename = generateFilename(template, orderId);
            String mimeType = template.getTemplateFormat().getMimeType();

            // Mark as completed
            genDoc.markAsCompleted(content, filename, mimeType);
            genDoc.setVariablesUsed(orderData);

            generatedDocRepository.save(genDoc);

            log.info("Document generated successfully: id={}, filename={}, size={} bytes",
                    genDoc.getId(), filename, content.length);

            return genDoc;

        } catch (Exception e) {
            log.error("Document generation failed for template: {}, order: {}", templateCode, orderId, e);
            genDoc.markAsFailed(e.getMessage());
            generatedDocRepository.save(genDoc);
            throw new RuntimeException("Document generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch order data for template rendering
     */
    private Map<String, Object> fetchOrderData(Long orderId) {
        try {
            // Fetch from order-service via Feign
            return orderServiceClient.getOrderDataForTemplate(orderId);
        } catch (Exception e) {
            log.warn("Failed to fetch order data from service, using sample data", e);
            // Fallback to sample data for testing
            return placeholderResolver.createSampleData();
        }
    }

    /**
     * Generate content based on template format
     */
    private byte[] generateContent(DocumentTemplate template, Map<String, Object> data) throws Exception {
        byte[] templateContent = template.getTemplateContent();

        if (templateContent == null || templateContent.length == 0) {
            throw new RuntimeException("Template content is empty");
        }

        switch (template.getTemplateFormat()) {
            case DOCX:
                return pdfRenderer.processDocxTemplate(templateContent, data);

            case HTML:
                String htmlContent = new String(templateContent);
                String resolvedHtml = placeholderResolver.resolvePlaceholders(htmlContent, data);
                String styledHtml = pdfRenderer.generateStyledHtml(resolvedHtml, template.getTemplateName());
                return pdfRenderer.generatePdfFromHtml(styledHtml);

            case PDF:
                // For PDF templates, we can't modify them. Return as-is or overlay text
                log.warn("PDF template modification not supported. Returning original PDF.");
                return templateContent;

            default:
                throw new RuntimeException("Unsupported template format: " + template.getTemplateFormat());
        }
    }

    /**
     * Generate filename for generated document
     */
    private String generateFilename(DocumentTemplate template, Long orderId) {
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("%s_Order_%d_%s.pdf",
                template.getTemplateCode(),
                orderId,
                timestamp
        );
    }

    /**
     * Approve a generated document
     */
    @Transactional
    public void approveDocument(Long documentId, String approvedBy, String approvedByRole, String comments) {
        GeneratedDocument doc = generatedDocRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Generated document not found: " + documentId));

        if (!doc.getRequiresApproval()) {
            throw new RuntimeException("This document does not require approval");
        }

        // Validate approver has the required role
        if (doc.getApprovalRoles() != null && !doc.getApprovalRoles().isEmpty()) {
            if (!doc.getApprovalRoles().contains(approvedByRole)) {
                throw new RuntimeException("User role '" + approvedByRole + "' is not authorized to approve this document. Required roles: " + doc.getApprovalRoles());
            }
        }

        doc.markAsApproved(approvedBy, approvedByRole, comments);
        generatedDocRepository.save(doc);

        log.info("Document approved: id={}, approvedBy={}, role={}", documentId, approvedBy, approvedByRole);
    }

    /**
     * Get generated documents for an order
     */
    public java.util.List<GeneratedDocument> getDocumentsForOrder(Long orderId) {
        return generatedDocRepository.findByOrderId(orderId);
    }

    /**
     * Check if all required documents are approved for an order
     */
    public boolean areAllDocumentsApproved(Long orderId) {
        return generatedDocRepository.areAllRequiredDocsApproved(orderId);
    }
}
