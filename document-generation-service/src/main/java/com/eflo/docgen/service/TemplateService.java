package com.eflo.docgen.service;

import com.eflo.docgen.domain.entity.DocumentTemplate;
import com.eflo.docgen.domain.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Template Service
 *
 * Manages document template lifecycle
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final DocumentTemplateRepository templateRepository;
    private final PlaceholderResolverService placeholderResolver;

    /**
     * Create new template
     */
    @Transactional
    public DocumentTemplate createTemplate(DocumentTemplate template, MultipartFile file) throws IOException {
        log.info("Creating template: code={}, type={}", template.getTemplateCode(), template.getTemplateType());

        if (templateRepository.existsByTemplateCode(template.getTemplateCode())) {
            throw new RuntimeException("Template with code '" + template.getTemplateCode() + "' already exists");
        }

        // Store file content
        if (file != null && !file.isEmpty()) {
            template.setTemplateContent(file.getBytes());
            template.setTemplateSizeBytes(file.getSize());
            template.setOriginalFilename(file.getOriginalFilename());
        }

        DocumentTemplate saved = templateRepository.save(template);
        log.info("Template created: id={}, code={}", saved.getId(), saved.getTemplateCode());
        return saved;
    }

    /**
     * Update template content
     */
    @Transactional
    public DocumentTemplate updateTemplateContent(String templateCode, MultipartFile file) throws IOException {
        DocumentTemplate template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateCode));

        template.setTemplateContent(file.getBytes());
        template.setTemplateSizeBytes(file.getSize());
        template.setOriginalFilename(file.getOriginalFilename());
        template.setVersion(template.getVersion() + 1);

        return templateRepository.save(template);
    }

    /**
     * Get all active templates
     */
    public List<DocumentTemplate> getActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    /**
     * Get template by code
     */
    public DocumentTemplate getTemplate(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateCode));
    }

    /**
     * Delete template
     */
    @Transactional
    public void deleteTemplate(String templateCode) {
        DocumentTemplate template = getTemplate(templateCode);
        templateRepository.delete(template);
        log.info("Template deleted: code={}", templateCode);
    }

    /**
     * Activate/deactivate template
     */
    @Transactional
    public void setTemplateStatus(String templateCode, boolean isActive) {
        DocumentTemplate template = getTemplate(templateCode);
        template.setIsActive(isActive);
        templateRepository.save(template);
        log.info("Template status updated: code={}, active={}", templateCode, isActive);
    }
}
