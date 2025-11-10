package com.eflo.document.mapper;

import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.model.DocumentTypeCreateRequest;
import com.eflo.document.domain.model.DocumentTypeResponse;
import com.eflo.document.domain.model.DocumentTypeUpdateRequest;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for converting between DocumentType entity and DTOs.
 * Handles mapping of DocumentType entities to response DTOs and request DTOs to entities.
 *
 * <p>This mapper includes proper handling of JSONB fields (autoValidateConditions,
 * customValidationRules, metadataSchema) and list fields.</p>
 *
 * @author Document Service
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DocumentTypeMapper {

    /**
     * Maps a DocumentType entity to a DocumentTypeResponse DTO.
     *
     * @param documentType the document type entity
     * @return the document type response DTO
     */
    @Mapping(target = "documentCount", ignore = true)
    @Mapping(target = "activeDocumentCount", ignore = true)
    @Mapping(target = "pendingDocumentCount", ignore = true)
    DocumentTypeResponse toResponse(DocumentType documentType);

    /**
     * Maps a DocumentTypeCreateRequest to a DocumentType entity.
     *
     * @param request the create request
     * @return the document type entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DocumentType toEntity(DocumentTypeCreateRequest request);

    /**
     * Maps a list of DocumentType entities to a list of DocumentTypeResponse DTOs.
     *
     * @param documentTypes the list of document type entities
     * @return the list of document type response DTOs
     */
    List<DocumentTypeResponse> toEntityList(List<DocumentType> documentTypes);

    /**
     * Updates an existing DocumentType entity from a DocumentTypeUpdateRequest.
     * Only updates fields that are non-null in the request.
     *
     * @param request the update request
     * @param documentType the document type entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "businessUnitId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(DocumentTypeUpdateRequest request, @MappingTarget DocumentType documentType);

    /**
     * After mapping update, ensure the updated timestamp is set.
     *
     * @param documentType the updated document type
     */
    @AfterMapping
    default void afterUpdate(@MappingTarget DocumentType documentType) {
        documentType.setUpdatedAt(java.time.LocalDateTime.now());
    }
}
