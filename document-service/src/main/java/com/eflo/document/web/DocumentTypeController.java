package com.eflo.document.web;

import com.eflo.document.domain.enums.DocumentCategory;
import com.eflo.document.domain.model.DocumentTypeCreateRequest;
import com.eflo.document.domain.model.DocumentTypeResponse;
import com.eflo.document.domain.model.DocumentTypeUpdateRequest;
import com.eflo.document.mapper.DocumentTypeMapper;
import com.eflo.document.service.DocumentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for document type management.
 * Provides endpoints for creating, updating, and retrieving document type configurations.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
@Tag(name = "Document Type Management", description = "APIs for managing document types and configurations")
@SecurityRequirement(name = "bearer-auth")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;
    private final DocumentTypeMapper documentTypeMapper;

    /**
     * List all document types.
     *
     * @param includeInactive whether to include inactive types
     * @return list of document types
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "List all document types", description = "Retrieves all document types with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document types retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentTypeResponse>> listAllTypes(
            @Parameter(description = "Include inactive types")
            @RequestParam(value = "includeInactive", defaultValue = "false") Boolean includeInactive) {

        log.info("Listing all document types: includeInactive={}", includeInactive);

        var types = documentTypeService.getAllDocumentTypes(null);
        if (!Boolean.TRUE.equals(includeInactive)) {
            types = types.stream().filter(dt -> Boolean.TRUE.equals(dt.getIsActive())).toList();
        }
        return ResponseEntity.ok(documentTypeMapper.toEntityList(types));
    }

    /**
     * Create a new document type.
     *
     * @param request document type creation request
     * @return created document type
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_TYPE_MANAGER')")
    @Operation(summary = "Create document type", description = "Creates a new document type configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document type created successfully",
            content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "409", description = "Document type already exists"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentTypeResponse> createType(
            @Valid @RequestBody DocumentTypeCreateRequest request) {

        log.info("Creating new document type: typeCode={}, typeName={}",
            request.getTypeCode(), request.getTypeName());

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        var saved = documentTypeService.createDocumentType(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeMapper.toResponse(saved));
    }

    /**
     * Get document type by ID.
     *
     * @param id document type ID
     * @return document type details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get document type by ID", description = "Retrieves detailed document type information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type found",
            content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document type not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentTypeResponse> getType(
            @Parameter(description = "Document type ID", required = true)
            @PathVariable Long id) {

        log.info("Retrieving document type: id={}", id);

        var type = documentTypeService.getDocumentTypeById(id);
        return ResponseEntity.ok(documentTypeMapper.toResponse(type));
    }

    /**
     * Update an existing document type.
     *
     * @param id document type ID
     * @param request update request
     * @return updated document type
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_TYPE_MANAGER')")
    @Operation(summary = "Update document type", description = "Updates an existing document type configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document type updated successfully",
            content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Document type not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentTypeResponse> updateType(
            @Parameter(description = "Document type ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DocumentTypeUpdateRequest request) {

        log.info("Updating document type: id={}", id);

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        var updated = documentTypeService.updateDocumentType(id, request, user);
        return ResponseEntity.ok(documentTypeMapper.toResponse(updated));
    }

    /**
     * Delete a document type.
     *
     * @param id document type ID
     * @return success response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete document type", description = "Deletes a document type (only if not in use)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document type deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Document type not found"),
        @ApiResponse(responseCode = "409", description = "Document type is in use and cannot be deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Void> deleteType(
            @Parameter(description = "Document type ID", required = true)
            @PathVariable Long id) {

        log.info("Deleting document type: id={}", id);

        documentTypeService.deleteDocumentType(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all mandatory document types.
     *
     * @return list of mandatory document types
     */
    @GetMapping("/mandatory")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get mandatory document types",
               description = "Retrieves all document types marked as mandatory")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mandatory types retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentTypeResponse>> getMandatoryTypes() {

        log.info("Retrieving mandatory document types");

        var types = documentTypeService.getMandatoryDocumentTypes(null);
        return ResponseEntity.ok(documentTypeMapper.toEntityList(types));
    }

    /**
     * Get document types by category.
     *
     * @param category document category
     * @return list of document types in the category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get document types by category",
               description = "Retrieves document types filtered by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document types retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentTypeResponse>> getByCategory(
            @Parameter(description = "Document category", required = true)
            @PathVariable DocumentCategory category) {

        log.info("Retrieving document types by category: category={}", category);

        var types = documentTypeService.getDocumentTypesByCategory(category);
        return ResponseEntity.ok(documentTypeMapper.toEntityList(types));
    }

    /**
     * Duplicate a document type configuration.
     *
     * @param id document type ID to duplicate
     * @param newTypeCode new type code for the duplicate
     * @param newTypeName new type name for the duplicate
     * @return duplicated document type
     */
    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_TYPE_MANAGER')")
    @Operation(summary = "Duplicate document type",
               description = "Creates a copy of an existing document type with a new code and name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Document type duplicated successfully",
            content = @Content(schema = @Schema(implementation = DocumentTypeResponse.class))),
        @ApiResponse(responseCode = "404", description = "Source document type not found"),
        @ApiResponse(responseCode = "409", description = "New type code already exists"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<DocumentTypeResponse> duplicateType(
            @Parameter(description = "Document type ID to duplicate", required = true)
            @PathVariable Long id,
            @Parameter(description = "New type code", required = true)
            @RequestParam("newTypeCode") String newTypeCode,
            @Parameter(description = "New type name", required = true)
            @RequestParam("newTypeName") String newTypeName) {

        log.info("Duplicating document type: id={}, newTypeCode={}, newTypeName={}",
            id, newTypeCode, newTypeName);

        String user = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SYSTEM";
        var dup = documentTypeService.duplicateDocumentType(id, newTypeCode, user);
        if (newTypeName != null && !newTypeName.isBlank()) {
            var update = new DocumentTypeUpdateRequest();
            update.setTypeName(newTypeName);
            dup = documentTypeService.updateDocumentType(dup.getId(), update, user);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeMapper.toResponse(dup));
    }

    /**
     * Get document type templates.
     * Templates are pre-configured document types that can be used as starting points.
     *
     * @return list of template document types
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCUMENT_TYPE_MANAGER')")
    @Operation(summary = "Get document type templates",
               description = "Retrieves pre-configured document type templates")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<DocumentTypeResponse>> getTemplates() {

        log.info("Retrieving document type templates");

        var types = documentTypeService.getAllDocumentTypes(null).stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsActive()))
                .limit(10)
                .toList();
        return ResponseEntity.ok(documentTypeMapper.toEntityList(types));
    }
}
