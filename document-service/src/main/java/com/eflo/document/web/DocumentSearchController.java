package com.eflo.document.web;

import com.eflo.document.domain.model.DocumentResponse;
import com.eflo.document.domain.model.DocumentSearchRequest;
import com.eflo.document.domain.model.DocumentSummaryResponse;
import com.eflo.document.mapper.DocumentMapper;
import com.eflo.document.service.DocumentSearchService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for document search operations.
 * Provides advanced search capabilities with various filters and criteria.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documents/search")
@RequiredArgsConstructor
@Tag(name = "Document Search", description = "APIs for searching and discovering documents")
@SecurityRequirement(name = "bearer-auth")
public class DocumentSearchController {

    private final DocumentSearchService searchService;
    private final DocumentMapper documentMapper;

    /**
     * Advanced document search with comprehensive filtering.
     *
     * @param searchRequest search criteria and filters
     * @return page of matching documents
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Advanced document search",
               description = "Searches documents with comprehensive filtering, sorting, and pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> advancedSearch(
            @Valid @RequestBody DocumentSearchRequest searchRequest) {

        log.info("Performing advanced document search: searchTerm={}, filters={}",
            searchRequest.getSearchTerm(), searchRequest);

        Pageable pageable = PageRequest.of(
                searchRequest.getPage() != null ? searchRequest.getPage() : 0,
                searchRequest.getSize() != null ? searchRequest.getSize() : 20
        );

        Page<com.eflo.document.domain.entity.Document> results = searchService.searchDocuments(searchRequest, pageable);
        List<DocumentSummaryResponse> summaries = results.getContent().stream()
                .map(documentMapper::toSummaryResponse)
                .toList();
        Page<DocumentSummaryResponse> response = new PageImpl<>(summaries, pageable, results.getTotalElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Get recently uploaded documents.
     *
     * @param days number of days to look back
     * @param pageable pagination parameters
     * @return page of recent documents
     */
    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Get recent documents",
               description = "Retrieves recently uploaded documents within specified days")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recent documents retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid days parameter"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> getRecentDocuments(
            @Parameter(description = "Number of days to look back (default: 7)")
            @RequestParam(value = "days", defaultValue = "7") Integer days,
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {

        log.info("Retrieving recent documents: days={}, page={}", days, pageable.getPageNumber());

        DocumentSearchRequest req = DocumentSearchRequest.builder()
                .uploadedFrom(LocalDateTime.now().minusDays(days))
                .uploadedTo(LocalDateTime.now())
                .latestVersionOnly(true)
                .build();
        Page<com.eflo.document.domain.entity.Document> results = searchService.searchDocuments(req, pageable);
        List<DocumentSummaryResponse> summaries = results.getContent().stream()
                .map(documentMapper::toSummaryResponse)
                .toList();
        return ResponseEntity.ok(new PageImpl<>(summaries, pageable, results.getTotalElements()));
    }

    /**
     * Get documents uploaded by the current user.
     *
     * @param pageable pagination parameters
     * @return page of user's documents
     */
    @GetMapping("/my-uploads")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my uploaded documents",
               description = "Retrieves documents uploaded by the current authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User documents retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentSummaryResponse>> getMyUploads(
            @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable) {

        log.info("Retrieving current user's uploaded documents: page={}", pageable.getPageNumber());
        String currentUser = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : null;

        DocumentSearchRequest req = DocumentSearchRequest.builder()
                .uploadedBy(currentUser)
                .latestVersionOnly(true)
                .build();
        Page<com.eflo.document.domain.entity.Document> results = searchService.searchDocuments(req, pageable);
        List<DocumentSummaryResponse> summaries = results.getContent().stream()
                .map(documentMapper::toSummaryResponse)
                .toList();
        return ResponseEntity.ok(new PageImpl<>(summaries, pageable, results.getTotalElements()));
    }

    /**
     * Search documents by custom metadata.
     *
     * @param metadata metadata search criteria as key-value pairs
     * @param pageable pagination parameters
     * @return page of matching documents
     */
    @PostMapping("/metadata")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'VIEWER')")
    @Operation(summary = "Search by metadata",
               description = "Searches documents based on custom metadata fields")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metadata search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid metadata search criteria"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Page<DocumentResponse>> searchByMetadata(
            @Parameter(description = "Metadata search criteria as JSON object", required = true)
            @RequestBody Map<String, Object> metadata,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Searching documents by metadata: criteria={}, page={}", metadata, pageable.getPageNumber());
        List<com.eflo.document.domain.entity.Document> all = searchService.searchByMetadata(metadata);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<com.eflo.document.domain.entity.Document> pageSlice = start > end ? List.of() : all.subList(start, end);
        List<DocumentResponse> mapped = pageSlice.stream().map(documentMapper::toResponse).toList();
        return ResponseEntity.ok(new PageImpl<>(mapped, pageable, all.size()));
    }
}
