package com.eflo.document.web;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentAccessLog;
import com.eflo.document.domain.enums.AccessAction;
import com.eflo.document.domain.model.DocumentStatisticsResponse;
import com.eflo.document.domain.repository.DocumentAccessLogRepository;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import com.eflo.document.service.DocumentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Reports", description = "APIs for document statistics, reports, and analytics")
@SecurityRequirement(name = "bearer-auth")
public class DocumentReportController {

    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentAccessLogRepository accessLogRepository;
    private final DocumentManagementService documentManagementService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER', 'VIEWER', 'ANALYTICS_VIEWER')")
    @Operation(summary = "Get document statistics",
               description = "Retrieves comprehensive document statistics with optional date range filtering")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = DocumentStatisticsResponse.class)))
    public ResponseEntity<DocumentStatisticsResponse> getDocumentStatistics(
            @Parameter(description = "Start date for statistics (ISO format)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "End date for statistics (ISO format)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Business unit ID filter")
            @RequestParam(value = "businessUnitId", required = false) Long businessUnitId) {

        log.info("Retrieving document statistics: fromDate={}, toDate={}, businessUnitId={}",
                fromDate, toDate, businessUnitId);

        List<Document> all = documentRepository.findAll();
        if (businessUnitId != null) {
            all = all.stream().filter(d -> Objects.equals(d.getBusinessUnitId(), businessUnitId)).toList();
        }
        if (fromDate != null) {
            all = all.stream().filter(d -> d.getUploadedAt() != null && !d.getUploadedAt().toLocalDate().isBefore(fromDate)).toList();
        }
        if (toDate != null) {
            all = all.stream().filter(d -> d.getUploadedAt() != null && !d.getUploadedAt().toLocalDate().isAfter(toDate)).toList();
        }

        long total = all.size();
        long active = all.stream().filter(Document::isActive).count();
        long pending = all.stream().filter(Document::isPending).count();
        long validated = all.stream().filter(Document::isValidated).count();
        long rejected = all.stream().filter(Document::isRejected).count();
        long expired = all.stream().filter(Document::isExpired).count();
        long archived = all.stream().filter(Document::isArchived).count();
        long deleted = all.stream().filter(Document::isDeleted).count();
        long expiringSoon = all.stream().filter(d -> d.isExpiringSoon(d.getDocumentType() != null ? d.getDocumentType().getExpirationWarningDays() : 30)).count();
        long scanPending = all.stream().filter(d -> d.getVirusScanStatus() == com.eflo.document.domain.enums.VirusScanStatus.PENDING).count();
        long infected = all.stream().filter(d -> d.getVirusScanStatus() == com.eflo.document.domain.enums.VirusScanStatus.INFECTED).count();
        long confidential = all.stream().filter(d -> Boolean.TRUE.equals(d.getIsConfidential())).count();

        long totalBytes = all.stream().map(Document::getFileSizeBytes).filter(Objects::nonNull).mapToLong(Long::longValue).sum();
        double totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0);
        List<Long> sizes = all.stream().map(Document::getFileSizeBytes).filter(Objects::nonNull).toList();
        double avgMB = sizes.isEmpty() ? 0.0 : sizes.stream().mapToLong(Long::longValue).average().orElse(0.0) / (1024.0 * 1024.0);
        double maxMB = sizes.isEmpty() ? 0.0 : (sizes.stream().mapToLong(Long::longValue).max().orElse(0L) / (1024.0 * 1024.0));
        double minMB = sizes.isEmpty() ? 0.0 : (sizes.stream().mapToLong(Long::longValue).min().orElse(0L) / (1024.0 * 1024.0));

        Map<String, Long> byStatus = all.stream().collect(Collectors.groupingBy(d -> d.getStatus().name(), Collectors.counting()));
        Map<String, Long> byType = all.stream().collect(Collectors.groupingBy(Document::getTypeCode, Collectors.counting()));
        Map<String, Long> byExt = all.stream().collect(Collectors.groupingBy(Document::getFileExtension, Collectors.counting()));

        DocumentStatisticsResponse response = DocumentStatisticsResponse.builder()
                .totalDocuments(total)
                .activeDocuments(active)
                .pendingDocuments(pending)
                .validatedDocuments(validated)
                .rejectedDocuments(rejected)
                .expiredDocuments(expired)
                .archivedDocuments(archived)
                .deletedDocuments(deleted)
                .expiringDocuments(expiringSoon)
                .virusScanPending(scanPending)
                .infectedDocuments(infected)
                .confidentialDocuments(confidential)
                .totalStorageSizeBytes(totalBytes)
                .totalStorageSizeGB(totalGB)
                .averageFileSizeMB(avgMB)
                .largestFileSizeMB(maxMB)
                .smallestFileSizeMB(minMB)
                .documentsByStatus(byStatus)
                .documentsByType(byType)
                .documentsByExtension(byExt)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/compliance")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMPLIANCE_OFFICER', 'DOCUMENT_MANAGER')")
    @Operation(summary = "Get compliance report",
               description = "Generates a compliance report showing mandatory document status and validation compliance")
    @ApiResponse(responseCode = "200", description = "Compliance report generated successfully")
    public ResponseEntity<Map<String, Object>> getComplianceReport(
            @Parameter(description = "Order ID filter")
            @RequestParam(value = "orderId", required = false) Long orderId,
            @Parameter(description = "Business unit ID filter")
            @RequestParam(value = "businessUnitId", required = false) Long businessUnitId) {

        log.info("Generating compliance report: orderId={}, businessUnitId={}", orderId, businessUnitId);

        Map<String, Object> report = new LinkedHashMap<>();
        if (orderId != null) {
            var result = documentManagementService.checkOrderDocumentCompleteness(orderId);
            report.put("orderId", orderId);
            report.put("isComplete", result.getIsComplete());
            report.put("missingDocumentTypes", result.getMissingDocumentTypes());
            report.put("incompleteDocumentTypes", result.getIncompleteDocumentTypes());
            report.put("totalRequiredTypes", result.getTotalRequiredTypes());
            report.put("validatedDocuments", result.getValidatedDocuments());
            report.put("pendingDocuments", result.getPendingDocuments());
            report.put("rejectedDocuments", result.getRejectedDocuments());
        } else {
            report.put("message", "Provide orderId to get per-order compliance details.");
            report.put("mandatoryTypes", documentTypeRepository.findByIsMandatoryTrue().size());
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report/validation")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER', 'VALIDATOR')")
    @Operation(summary = "Get validation report",
               description = "Generates a report of document validation activities and statistics")
    @ApiResponse(responseCode = "200", description = "Validation report generated successfully")
    public ResponseEntity<Map<String, Object>> getValidationReport(
            @Parameter(description = "Start date for report (ISO format)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date for report (ISO format)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Validator ID filter")
            @RequestParam(value = "validatorId", required = false) String validatorId) {

        log.info("Generating validation report: fromDate={}, toDate={}, validatorId={}",
                fromDate, toDate, validatorId);

        List<Document> all = documentRepository.findAll();
        if (fromDate != null) {
            all = all.stream().filter(d -> d.getValidatedAt() != null && !d.getValidatedAt().isBefore(fromDate)).toList();
        }
        if (toDate != null) {
            all = all.stream().filter(d -> d.getValidatedAt() != null && !d.getValidatedAt().isAfter(toDate)).toList();
        }
        if (validatorId != null && !validatorId.isBlank()) {
            all = all.stream().filter(d -> validatorId.equals(d.getValidatedBy())).toList();
        }

        long totalValidations = all.stream().filter(d -> d.getValidatedAt() != null).count();
        long approved = all.stream().filter(Document::isValidated).count();
        long rejected = all.stream().filter(Document::isRejected).count();
        double avgValidationTimeHours = all.stream()
                .filter(d -> d.getValidatedAt() != null && d.getUploadedAt() != null)
                .mapToLong(d -> java.time.Duration.between(d.getUploadedAt(), d.getValidatedAt()).toHours())
                .average().orElse(0.0);
        Map<String, Long> byValidator = all.stream()
                .filter(d -> d.getValidatedBy() != null)
                .collect(Collectors.groupingBy(Document::getValidatedBy, Collectors.counting()));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalValidations", totalValidations);
        report.put("approved", approved);
        report.put("rejected", rejected);
        report.put("avgValidationTimeHours", avgValidationTimeHours);
        report.put("validationsByValidator", byValidator);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/report/access")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "Get access report",
               description = "Generates a report of document access patterns and activities")
    @ApiResponse(responseCode = "200", description = "Access report generated successfully")
    public ResponseEntity<Map<String, Object>> getAccessReport(
            @Parameter(description = "Start date for report (ISO format)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date for report (ISO format)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Document ID filter")
            @RequestParam(value = "documentId", required = false) Long documentId) {

        log.info("Generating access report: fromDate={}, toDate={}, documentId={}",
                fromDate, toDate, documentId);

        LocalDateTime start = (fromDate != null) ? fromDate : LocalDateTime.now().minusDays(30);
        List<DocumentAccessLog> logs = accessLogRepository.findRecentAccessLogs(start);
        if (documentId != null) {
            logs = logs.stream().filter(l -> l.getDocument() != null && documentId.equals(l.getDocument().getId())).toList();
        }

        Map<String, Object> report = new LinkedHashMap<>();
        long total = logs.size();
        long downloads = logs.stream().filter(l -> l.getAction() == AccessAction.DOWNLOAD).count();
        long views = logs.stream().filter(l -> l.getAction() == AccessAction.VIEW).count();
        report.put("totalAccessEvents", total);
        report.put("downloads", downloads);
        report.put("views", views);

        Map<Long, Long> topDocs = logs.stream()
                .filter(l -> l.getDocument() != null)
                .collect(Collectors.groupingBy(l -> l.getDocument().getId(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        report.put("topDocuments", topDocs);

        Map<String, Long> accessByUser = logs.stream()
                .filter(l -> l.getUserId() != null)
                .collect(Collectors.groupingBy(DocumentAccessLog::getUserId, Collectors.counting()));
        report.put("accessByUser", accessByUser);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get dashboard data",
               description = "Retrieves aggregated statistics and metrics for dashboard display")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    public ResponseEntity<Map<String, Object>> getDashboardData() {

        log.info("Retrieving dashboard data");
        List<Document> all = documentRepository.findAll();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalDocuments", all.size());
        long recentUploads = all.stream().filter(d -> d.getUploadedAt() != null && d.getUploadedAt().isAfter(LocalDateTime.now().minusDays(7))).count();
        data.put("recentUploads7d", recentUploads);
        data.put("pendingValidations", all.stream().filter(Document::isPending).count());
        data.put("expiringSoon30d", all.stream().filter(d -> d.isExpiringSoon(d.getDocumentType() != null ? d.getDocumentType().getExpirationWarningDays() : 30)).count());
        data.put("documentsByStatus", all.stream().collect(Collectors.groupingBy(d -> d.getStatus().name(), Collectors.counting())));
        data.put("documentsByType", all.stream().collect(Collectors.groupingBy(Document::getTypeCode, Collectors.counting())));
        return ResponseEntity.ok(data);
    }

    @GetMapping("/export/audit")
    @PreAuthorize("hasAnyRole('ADMIN', 'SECURITY_ADMIN', 'AUDIT_VIEWER')")
    @Operation(summary = "Export audit log",
               description = "Exports document audit log in specified format (CSV or Excel)")
    @ApiResponse(responseCode = "200", description = "Audit log exported successfully")
    public ResponseEntity<InputStreamResource> exportAuditLog(
            @Parameter(description = "Start date for export (ISO format)", required = true)
            @RequestParam("fromDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @Parameter(description = "End date for export (ISO format)", required = true)
            @RequestParam("toDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @Parameter(description = "Export format: csv or excel")
            @RequestParam(value = "format", defaultValue = "csv") String format) {

        log.info("Exporting audit log: fromDate={}, toDate={}, format={}", fromDate, toDate, format);
        List<DocumentAccessLog> logs = accessLogRepository.findRecentAccessLogs(fromDate);
        StringBuilder csv = new StringBuilder();
        csv.append("timestamp,documentId,userId,action,result,ip\n");
        for (DocumentAccessLog l : logs) {
            Long docId = l.getDocument() != null ? l.getDocument().getId() : null;
            csv.append(l.getAccessedAt()).append(',')
               .append(docId != null ? docId : "").append(',')
               .append(l.getUserId() != null ? l.getUserId() : "").append(',')
               .append(l.getAction() != null ? l.getAction().name() : "").append(',')
               .append(l.getActionResult() != null ? l.getActionResult() : "").append(',')
               .append(l.getIpAddress() != null ? l.getIpAddress() : "")
               .append('\n');
        }
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        InputStreamResource resource = new InputStreamResource(new java.io.ByteArrayInputStream(bytes));

        String filename = String.format("audit-log-%s-to-%s.csv", fromDate.toLocalDate(), toDate.toLocalDate());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}

