package com.eflo.document.mapper;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.model.*;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

/**
 * MapStruct mapper for converting between Document entity and DTOs.
 * Handles mapping of Document entities to various response formats and request DTOs to entities.
 *
 * <p>This mapper includes custom mappings for nested objects (StorageInfo, ExpirationInfo, DocumentVersion)
 * and proper handling of JSONB fields (customMetadata, virusScanResult).</p>
 *
 * @author Document Service
 * @version 1.0
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DocumentMapper {

    /**
     * Maps a Document entity to a DocumentResponse DTO.
     * Includes all detailed information including nested objects.
     *
     * @param document the document entity
     * @return the document response DTO
     */
    @Mapping(target = "documentTypeId", source = "documentType.id")
    @Mapping(target = "typeName", source = "documentType.typeName")
    @Mapping(target = "fileSizeMB", expression = "java(calculateFileSizeMB(document.getFileSizeBytes()))")
    @Mapping(target = "storageInfo", expression = "java(buildStorageInfo(document))")
    @Mapping(target = "versionInfo", expression = "java(buildVersionInfo(document))")
    @Mapping(target = "expirationInfo", expression = "java(buildExpirationInfo(document))")
    @Mapping(target = "scanResult", expression = "java(buildScanResult(document))")
    @Mapping(target = "isActive", expression = "java(document.isActive())")
    @Mapping(target = "isExpired", expression = "java(document.isExpired())")
    @Mapping(target = "hasRestrictedAccess", expression = "java(document.hasRestrictedAccess())")
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "previewUrl", ignore = true)
    DocumentResponse toResponse(Document document);

    /**
     * Maps a Document entity to a DocumentSummaryResponse DTO.
     * Includes only essential information for list views.
     *
     * @param document the document entity
     * @return the document summary response DTO
     */
    @Mapping(target = "documentTypeId", source = "documentType.id")
    @Mapping(target = "typeName", source = "documentType.typeName")
    @Mapping(target = "fileSizeMB", expression = "java(calculateFileSizeMB(document.getFileSizeBytes()))")
    @Mapping(target = "isExpired", expression = "java(document.isExpired())")
    @Mapping(target = "isExpiringSoon", expression = "java(isExpiringSoon(document))")
    @Mapping(target = "downloadUrl", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    DocumentSummaryResponse toSummaryResponse(Document document);

    /**
     * Maps a list of Document entities to a list of DocumentResponse DTOs.
     *
     * @param documents the list of document entities
     * @return the list of document response DTOs
     */
    List<DocumentResponse> toDocumentList(List<Document> documents);

    /**
     * Maps a list of Document entities to a list of DocumentSummaryResponse DTOs.
     *
     * @param documents the list of document entities
     * @return the list of document summary response DTOs
     */
    List<DocumentSummaryResponse> toSummaryList(List<Document> documents);

    /**
     * Maps a DocumentUploadRequest to a Document entity.
     * Note: The file itself is handled separately.
     *
     * @param request the upload request
     * @return the document entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentUuid", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "storedFilename", ignore = true)
    @Mapping(target = "fileExtension", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "fileSizeBytes", ignore = true)
    @Mapping(target = "fileHash", ignore = true)
    @Mapping(target = "storageBucket", ignore = true)
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "storageRegion", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isLatestVersion", ignore = true)
    @Mapping(target = "parentDocument", ignore = true)
    @Mapping(target = "replacedByDocument", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusReason", ignore = true)
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "validationComments", ignore = true)
    @Mapping(target = "expirationNotified", ignore = true)
    @Mapping(target = "expirationNotificationSentAt", ignore = true)
    @Mapping(target = "virusScanStatus", ignore = true)
    @Mapping(target = "virusScanDate", ignore = true)
    @Mapping(target = "virusScanResult", ignore = true)
    @Mapping(target = "customMetadata", source = "metadata")
    @Mapping(target = "isConfidential", constant = "false")
    @Mapping(target = "accessLevel", ignore = true)
    @Mapping(target = "encryptionEnabled", constant = "false")
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    Document toEntity(DocumentUploadRequest request);

    /**
     * Updates an existing Document entity from a DocumentUpdateRequest.
     * Only updates fields that are non-null in the request.
     *
     * @param request the update request
     * @param document the document entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "documentUuid", ignore = true)
    @Mapping(target = "documentType", ignore = true)
    @Mapping(target = "typeCode", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "storedFilename", ignore = true)
    @Mapping(target = "fileExtension", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "fileSizeBytes", ignore = true)
    @Mapping(target = "fileHash", ignore = true)
    @Mapping(target = "storageBucket", ignore = true)
    @Mapping(target = "storagePath", ignore = true)
    @Mapping(target = "storageRegion", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "isLatestVersion", ignore = true)
    @Mapping(target = "parentDocument", ignore = true)
    @Mapping(target = "replacedByDocument", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusReason", ignore = true)
    @Mapping(target = "validatedAt", ignore = true)
    @Mapping(target = "validatedBy", ignore = true)
    @Mapping(target = "validationComments", ignore = true)
    @Mapping(target = "expirationNotified", ignore = true)
    @Mapping(target = "expirationNotificationSentAt", ignore = true)
    @Mapping(target = "virusScanStatus", ignore = true)
    @Mapping(target = "virusScanDate", ignore = true)
    @Mapping(target = "virusScanResult", ignore = true)
    @Mapping(target = "customMetadata", source = "metadata")
    @Mapping(target = "isConfidential", ignore = true)
    @Mapping(target = "accessLevel", ignore = true)
    @Mapping(target = "encryptionEnabled", ignore = true)
    @Mapping(target = "businessUnitId", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    void updateEntityFromRequest(DocumentUpdateRequest request, @MappingTarget Document document);

    /**
     * Calculates file size in megabytes from bytes.
     *
     * @param fileSizeBytes the file size in bytes
     * @return the file size in megabytes
     */
    default Double calculateFileSizeMB(Long fileSizeBytes) {
        if (fileSizeBytes == null) {
            return null;
        }
        return fileSizeBytes / (1024.0 * 1024.0);
    }

    /**
     * Builds StorageInfo from Document entity.
     *
     * @param document the document entity
     * @return the storage info
     */
    default StorageInfo buildStorageInfo(Document document) {
        if (document == null) {
            return null;
        }

        return StorageInfo.builder()
            .bucket(document.getStorageBucket())
            .path(document.getStoragePath())
            .region(document.getStorageRegion())
            .sizeBytes(document.getFileSizeBytes())
            .sizeMB(calculateFileSizeMB(document.getFileSizeBytes()))
            .contentType(document.getMimeType())
            .storageKey(String.format("%s/%s", document.getStorageBucket(), document.getStoragePath()))
            .build();
    }

    /**
     * Builds DocumentVersion from Document entity.
     *
     * @param document the document entity
     * @return the version info
     */
    default DocumentVersion buildVersionInfo(Document document) {
        if (document == null) {
            return null;
        }

        return DocumentVersion.builder()
            .versionNumber(document.getVersion())
            .isLatest(document.getIsLatestVersion())
            .createdDate(document.getCreatedAt())
            .createdBy(document.getCreatedBy())
            .parentDocumentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
            .nextVersionId(document.getReplacedByDocument() != null ? document.getReplacedByDocument().getId() : null)
            .status(document.getIsLatestVersion() ? DocumentVersion.VersionStatus.ACTIVE : DocumentVersion.VersionStatus.SUPERSEDED)
            .build();
    }

    /**
     * Builds ExpirationInfo from Document entity.
     *
     * @param document the document entity
     * @return the expiration info
     */
    default ExpirationInfo buildExpirationInfo(Document document) {
        if (document == null) {
            return null;
        }

        if (document.getExpirationDate() == null) {
            return ExpirationInfo.noExpiration();
        }

        // Get warning days from document type if available, otherwise use default
        Integer warningDays = document.getDocumentType() != null ?
            document.getDocumentType().getExpirationWarningDays() : 30;

        ExpirationInfo info = ExpirationInfo.from(document.getExpirationDate(), warningDays);
        info.setNotified(document.getExpirationNotified());
        info.setNotificationSentAt(document.getExpirationNotificationSentAt());

        return info;
    }

    /**
     * Builds ScanResult from Document entity.
     *
     * @param document the document entity
     * @return the scan result
     */
    default ScanResult buildScanResult(Document document) {
        if (document == null || document.getVirusScanStatus() == null) {
            return null;
        }

        return ScanResult.builder()
            .scanStatus(document.getVirusScanStatus())
            .scanDate(document.getVirusScanDate())
            .details(document.getVirusScanResult())
            .build();
    }

    /**
     * Checks if document is expiring soon.
     *
     * @param document the document entity
     * @return true if expiring soon
     */
    default Boolean isExpiringSoon(Document document) {
        if (document == null || document.getExpirationDate() == null) {
            return false;
        }

        Integer warningDays = document.getDocumentType() != null ?
            document.getDocumentType().getExpirationWarningDays() : 30;

        return document.isExpiringSoon(warningDays);
    }
}
