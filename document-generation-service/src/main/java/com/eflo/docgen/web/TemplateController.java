package com.eflo.docgen.web;

import com.eflo.docgen.domain.entity.DocumentTemplate;
import com.eflo.docgen.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Template Management Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    public ResponseEntity<List<DocumentTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getActiveTemplates());
    }

    @GetMapping("/{templateCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER', 'USER')")
    public ResponseEntity<DocumentTemplate> getTemplate(@PathVariable String templateCode) {
        return ResponseEntity.ok(templateService.getTemplate(templateCode));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    public ResponseEntity<DocumentTemplate> createTemplate(
            @RequestPart("template") DocumentTemplate template,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        DocumentTemplate created = templateService.createTemplate(template, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{templateCode}/content")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    public ResponseEntity<DocumentTemplate> updateTemplateContent(
            @PathVariable String templateCode,
            @RequestPart("file") MultipartFile file) throws Exception {
        return ResponseEntity.ok(templateService.updateTemplateContent(templateCode, file));
    }

    @DeleteMapping("/{templateCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateCode) {
        templateService.deleteTemplate(templateCode);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{templateCode}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_MANAGER')")
    public ResponseEntity<Void> setTemplateStatus(
            @PathVariable String templateCode,
            @RequestParam boolean isActive) {
        templateService.setTemplateStatus(templateCode, isActive);
        return ResponseEntity.ok().build();
    }
}
