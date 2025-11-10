package com.eflo.document.integration;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.repository.DocumentTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DocumentTypeController.
 * Tests CRUD operations for document types with real database.
 *
 * @author Document Service
 * @version 1.0
 */
class DocumentTypeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Test
    @DisplayName("Should create document type successfully")
    @WithMockUser(roles = "ADMIN")
    void testCreateDocumentType() throws Exception {
        // Given
        String request = """
            {
                "typeCode": "INVOICE",
                "typeName": "Invoice",
                "description": "Invoice documents",
                "category": "FINANCIAL",
                "isActive": true,
                "isMandatory": true,
                "requiresValidation": true,
                "minDocuments": 1,
                "maxDocuments": 10,
                "allowedExtensions": ["pdf", "jpg", "png"],
                "maxFileSizeMB": 10
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/document-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
                .with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.typeCode").value("INVOICE"))
            .andExpect(jsonPath("$.typeName").value("Invoice"))
            .andExpect(jsonPath("$.category").value("FINANCIAL"))
            .andExpect(jsonPath("$.isActive").value(true))
            .andExpect(jsonPath("$.isMandatory").value(true))
            .andExpect(jsonPath("$.requiresValidation").value(true));

        // Verify database
        DocumentType saved = documentTypeRepository.findByTypeCode("INVOICE").orElseThrow();
        assertThat(saved.getTypeCode()).isEqualTo("INVOICE");
        assertThat(saved.getTypeName()).isEqualTo("Invoice");
        assertThat(saved.getCategory()).isEqualTo(DocumentCategory.FINANCIAL);
        assertThat(saved.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should retrieve document type by ID")
    @WithMockUser(roles = "USER")
    void testGetDocumentTypeById() throws Exception {
        // Given
        DocumentType documentType = createTestDocumentType("CONTRACT", "Contract");

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/{id}", documentType.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(documentType.getId()))
            .andExpect(jsonPath("$.typeCode").value("CONTRACT"))
            .andExpect(jsonPath("$.typeName").value("Contract"));
    }

    @Test
    @DisplayName("Should retrieve document type by code")
    @WithMockUser(roles = "USER")
    void testGetDocumentTypeByCode() throws Exception {
        // Given
        createTestDocumentType("INVOICE", "Invoice");

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/code/{typeCode}", "INVOICE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.typeCode").value("INVOICE"))
            .andExpect(jsonPath("$.typeName").value("Invoice"));
    }

    @Test
    @DisplayName("Should list all active document types")
    @WithMockUser(roles = "USER")
    void testGetAllActiveDocumentTypes() throws Exception {
        // Given
        createTestDocumentType("INVOICE", "Invoice");
        createTestDocumentType("CONTRACT", "Contract");

        DocumentType inactive = createTestDocumentType("OLD_TYPE", "Old Type");
        inactive.setIsActive(false);
        documentTypeRepository.save(inactive);

        // When & Then
        mockMvc.perform(get("/api/v1/document-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].isActive", everyItem(is(true))));
    }

    @Test
    @DisplayName("Should get mandatory document types only")
    @WithMockUser(roles = "USER")
    void testGetMandatoryDocumentTypes() throws Exception {
        // Given
        DocumentType mandatory1 = createTestDocumentType("INVOICE", "Invoice");
        mandatory1.setIsMandatory(true);
        documentTypeRepository.save(mandatory1);

        DocumentType mandatory2 = createTestDocumentType("CONTRACT", "Contract");
        mandatory2.setIsMandatory(true);
        documentTypeRepository.save(mandatory2);

        DocumentType optional = createTestDocumentType("MEMO", "Memo");
        optional.setIsMandatory(false);
        documentTypeRepository.save(optional);

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/mandatory"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].isMandatory", everyItem(is(true))));
    }

    @Test
    @DisplayName("Should filter document types by category")
    @WithMockUser(roles = "USER")
    void testGetDocumentTypesByCategory() throws Exception {
        // Given
        DocumentType financial1 = createTestDocumentType("INVOICE", "Invoice");
        financial1.setCategory(DocumentCategory.FINANCIAL);
        documentTypeRepository.save(financial1);

        DocumentType financial2 = createTestDocumentType("RECEIPT", "Receipt");
        financial2.setCategory(DocumentCategory.FINANCIAL);
        documentTypeRepository.save(financial2);

        DocumentType legal = createTestDocumentType("CONTRACT", "Contract");
        legal.setCategory(DocumentCategory.LEGAL);
        documentTypeRepository.save(legal);

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/category/{category}", "FINANCIAL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].category", everyItem(is("FINANCIAL"))));
    }

    @Test
    @DisplayName("Should update document type successfully")
    @WithMockUser(roles = "ADMIN")
    void testUpdateDocumentType() throws Exception {
        // Given
        DocumentType existing = createTestDocumentType("INVOICE", "Invoice");

        String updateRequest = """
            {
                "typeName": "Updated Invoice",
                "description": "Updated description",
                "maxDocuments": 20,
                "maxFileSizeMB": 15
            }
            """;

        // When & Then
        mockMvc.perform(put("/api/v1/document-types/{id}", existing.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.typeName").value("Updated Invoice"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.maxDocuments").value(20))
            .andExpect(jsonPath("$.maxFileSizeMB").value(15));

        // Verify database
        DocumentType updated = documentTypeRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getTypeName()).isEqualTo("Updated Invoice");
        assertThat(updated.getDescription()).isEqualTo("Updated description");
        assertThat(updated.getMaxDocuments()).isEqualTo(20);
    }

    @Test
    @DisplayName("Should deactivate document type")
    @WithMockUser(roles = "ADMIN")
    void testDeactivateDocumentType() throws Exception {
        // Given
        DocumentType active = createTestDocumentType("INVOICE", "Invoice");
        active.setIsActive(true);
        documentTypeRepository.save(active);

        // When & Then
        mockMvc.perform(post("/api/v1/document-types/{id}/deactivate", active.getId())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isActive").value(false));

        // Verify database
        DocumentType deactivated = documentTypeRepository.findById(active.getId()).orElseThrow();
        assertThat(deactivated.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should activate document type")
    @WithMockUser(roles = "ADMIN")
    void testActivateDocumentType() throws Exception {
        // Given
        DocumentType inactive = createTestDocumentType("INVOICE", "Invoice");
        inactive.setIsActive(false);
        documentTypeRepository.save(inactive);

        // When & Then
        mockMvc.perform(post("/api/v1/document-types/{id}/activate", inactive.getId())
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isActive").value(true));

        // Verify database
        DocumentType activated = documentTypeRepository.findById(inactive.getId()).orElseThrow();
        assertThat(activated.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should prevent duplicate document type codes")
    @WithMockUser(roles = "ADMIN")
    void testPreventDuplicateTypeCodes() throws Exception {
        // Given - Create first document type
        createTestDocumentType("INVOICE", "Invoice");

        // Try to create duplicate
        String duplicateRequest = """
            {
                "typeCode": "INVOICE",
                "typeName": "Another Invoice",
                "category": "FINANCIAL",
                "isActive": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/document-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateRequest)
                .with(csrf()))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should delete document type (soft delete)")
    @WithMockUser(roles = "ADMIN")
    void testDeleteDocumentType() throws Exception {
        // Given
        DocumentType documentType = createTestDocumentType("TEMP", "Temporary");

        // When & Then
        mockMvc.perform(delete("/api/v1/document-types/{id}", documentType.getId())
                .with(csrf()))
            .andExpect(status().isNoContent());

        // Verify soft delete
        DocumentType deleted = documentTypeRepository.findById(documentType.getId()).orElseThrow();
        assertThat(deleted.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should validate document type constraints")
    @WithMockUser(roles = "ADMIN")
    void testValidateDocumentTypeConstraints() throws Exception {
        // Given - Invalid request (missing required fields)
        String invalidRequest = """
            {
                "typeCode": "",
                "typeName": "",
                "category": "INVALID_CATEGORY"
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/v1/document-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest)
                .with(csrf()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get document type validation rules")
    @WithMockUser(roles = "USER")
    void testGetDocumentTypeValidationRules() throws Exception {
        // Given
        DocumentType documentType = createTestDocumentType("INVOICE", "Invoice");

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/{id}/validation-rules", documentType.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should get document type statistics")
    @WithMockUser(roles = "ADMIN")
    void testGetDocumentTypeStatistics() throws Exception {
        // Given
        createTestDocumentType("INVOICE", "Invoice");
        createTestDocumentType("CONTRACT", "Contract");

        // When & Then
        mockMvc.perform(get("/api/v1/document-types/statistics"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalTypes").value(greaterThanOrEqualTo(2)));
    }

    // Helper methods

    private DocumentType createTestDocumentType(String typeCode, String typeName) {
        DocumentType documentType = DocumentType.builder()
            .typeCode(typeCode)
            .typeName(typeName)
            .description(typeName + " documents")
            .category(DocumentCategory.GENERAL)
            .isActive(true)
            .isMandatory(false)
            .requiresValidation(false)
            .minDocuments(0)
            .maxDocuments(10)
            .allowedExtensions(new String[]{"pdf", "jpg", "png", "doc", "docx"})
            .maxFileSizeMB(10)
            .build();

        return documentTypeRepository.save(documentType);
    }
}
