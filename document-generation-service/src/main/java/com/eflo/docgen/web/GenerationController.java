package com.eflo.docgen.web;

import com.eflo.docgen.domain.entity.GeneratedDocument;
import com.eflo.docgen.service.DocumentGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Document Generation Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/generate")
@RequiredArgsConstructor
public class GenerationController {

    private final DocumentGenerationService generationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SALESPERSON')")
    public ResponseEntity<GeneratedDocument> generateDocument(
            @RequestParam String templateCode,
            @RequestParam Long orderId,
            @RequestParam(required = false) Long workflowInstanceId,
            Authentication authentication) {

        String generatedBy = authentication != null ? authentication.getName() : "SYSTEM";

        GeneratedDocument doc = generationService.generateDocument(
                templateCode, orderId, workflowInstanceId, generatedBy);

        return ResponseEntity.status(HttpStatus.CREATED).body(doc);
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'VIEWER')")
    public ResponseEntity<List<GeneratedDocument>> getDocumentsForOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(generationService.getDocumentsForOrder(orderId));
    }

    @GetMapping("/{documentId}/download")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'VIEWER')")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long documentId) {
        GeneratedDocument doc = generationService.getDocumentsForOrder(0L).stream()
                .filter(d -> d.getId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Document not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(doc.getMimeType()));
        headers.setContentDispositionFormData("attachment", doc.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .body(doc.getGeneratedContent());
    }

    @PostMapping("/{documentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'CDV', 'SALES_MANAGER')")
    public ResponseEntity<Void> approveDocument(
            @PathVariable Long documentId,
            @RequestParam(required = false) String comments,
            Authentication authentication) {

        String approvedBy = authentication.getName();

        // Extract user's roles
        String approvedByRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.joining(","));

        generationService.approveDocument(documentId, approvedBy, approvedByRole, comments);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/order/{orderId}/approval-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'VIEWER')")
    public ResponseEntity<Boolean> checkApprovalStatus(@PathVariable Long orderId) {
        boolean allApproved = generationService.areAllDocumentsApproved(orderId);
        return ResponseEntity.ok(allApproved);
    }
}
