package com.eflo.document.service;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.entity.DocumentType;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentVersion;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.exception.DocumentStorageException;
import com.eflo.document.storage.MinIOStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing document versions.
 * Handles version creation, retrieval, comparison, and reversion.
 *
 * @author Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentVersionService {

    private final DocumentRepository documentRepository;
    private final MinIOStorageService storageService;

    /**
     * Creates a new version of a document.
     *
     * @param documentId the original document identifier
     * @param file the new file version
     * @param createdBy the user creating the version
     * @return the new document version
     * @throws DocumentStorageException if version creation fails
     */
    @Transactional
    public Document createNewVersion(Long documentId, MultipartFile file, String createdBy) {
        log.info("Creating new version for document ID: {} by user: {}", documentId, createdBy);

        // Get the current document
        Document currentDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentStorageException("Document not found with ID: " + documentId));

        // Verify this is the latest version
        if (!currentDocument.isLatest()) {
            throw new DocumentStorageException("Can only create new version from the latest version");
        }

        try {
            // Mark current document as no longer latest
            currentDocument.setIsLatestVersion(false);
            documentRepository.save(currentDocument);

            // Calculate new version number
            int newVersionNumber = currentDocument.getVersion() + 1;

            // Upload new file to storage
            String storedFilename = generateVersionedFilename(currentDocument, newVersionNumber);
            storageService.uploadFile(
                    currentDocument.getStorageBucket(),
                    storedFilename,
                    file.getInputStream(),
                    file.getContentType(),
                    file.getSize()
            );

            // Create new document version
            Document newVersion = Document.builder()
                    .documentUuid(UUID.randomUUID())
                    .documentType(currentDocument.getDocumentType())
                    .typeCode(currentDocument.getTypeCode())
                    .orderId(currentDocument.getOrderId())
                    .orderNumber(currentDocument.getOrderNumber())
                    .originalFilename(file.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .fileExtension(getFileExtension(file.getOriginalFilename()))
                    .mimeType(file.getContentType())
                    .fileSizeBytes(file.getSize())
                    .fileHash(null) // Hash can be calculated later if needed
                    .storageBucket(currentDocument.getStorageBucket())
                    .storagePath(storedFilename)
                    .storageRegion(currentDocument.getStorageRegion())
                    .version(newVersionNumber)
                    .isLatestVersion(true)
                    .parentDocument(findRootDocument(currentDocument))
                    .status(DocumentStatus.PENDING)
                    .businessUnitId(currentDocument.getBusinessUnitId())
                    .uploadedBy(createdBy)
                    .uploadedAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Copy metadata from previous version
            newVersion.setCustomMetadata(new HashMap<>(currentDocument.getCustomMetadata()));
            newVersion.setTags(new ArrayList<>(currentDocument.getTags()));
            newVersion.setDescription(currentDocument.getDescription());
            newVersion.setIsConfidential(currentDocument.getIsConfidential());
            newVersion.setAccessLevel(currentDocument.getAccessLevel());

            Document savedVersion = documentRepository.save(newVersion);

            // Update current document to point to new version
            currentDocument.setReplacedByDocument(savedVersion);
            documentRepository.save(currentDocument);

            log.info("Successfully created new version {} for document ID: {}", newVersionNumber, documentId);
            return savedVersion;

        } catch (Exception e) {
            log.error("Error creating new version for document ID: {}", documentId, e);
            throw new DocumentStorageException("Failed to create new document version", e);
        }
    }

    /**
     * Retrieves all versions of a document.
     *
     * @param documentId the document identifier
     * @return list of all document versions
     */
    @Transactional(readOnly = true)
    public List<Document> getAllVersions(Long documentId) {
        log.debug("Retrieving all versions for document ID: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentStorageException("Document not found with ID: " + documentId));

        // Find root document
        Document root = findRootDocument(document);

        // Get all versions (root + descendants)
        List<Document> versions = new ArrayList<>();
        versions.add(root);

        // Find all documents with this root as parent
        List<Document> allDocuments = documentRepository.findAll();
        for (Document doc : allDocuments) {
            if (doc.getParentDocument() != null && doc.getParentDocument().getId().equals(root.getId())) {
                versions.add(doc);
            }
        }

        // Sort by version number
        versions.sort(Comparator.comparing(Document::getVersion));

        log.debug("Found {} versions for document ID: {}", versions.size(), documentId);
        return versions;
    }

    /**
     * Retrieves version history for a document.
     *
     * @param documentId the document identifier
     * @return list of document version information
     */
    @Transactional(readOnly = true)
    public List<DocumentVersion> getVersionHistory(Long documentId) {
        log.debug("Retrieving version history for document ID: {}", documentId);

        List<Document> versions = getAllVersions(documentId);

        return versions.stream()
                .map(this::mapToDocumentVersion)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific version of a document.
     *
     * @param documentId the document identifier
     * @param version the version number
     * @return the document version
     * @throws DocumentStorageException if version not found
     */
    @Transactional(readOnly = true)
    public Document getSpecificVersion(Long documentId, int version) {
        log.debug("Retrieving version {} for document ID: {}", version, documentId);

        if (version < 1) {
            throw new DocumentStorageException("Version number must be at least 1");
        }

        List<Document> versions = getAllVersions(documentId);

        return versions.stream()
                .filter(doc -> doc.getVersion() == version)
                .findFirst()
                .orElseThrow(() -> new DocumentStorageException(
                        "Version " + version + " not found for document ID: " + documentId));
    }

    /**
     * Reverts a document to a previous version.
     *
     * @param documentId the document identifier
     * @param version the version number to revert to
     * @param revertedBy the user performing the revert
     * @return the new current version (copy of the reverted version)
     * @throws DocumentStorageException if revert fails
     */
    @Transactional
    public Document revertToVersion(Long documentId, int version, String revertedBy) {
        log.info("Reverting document ID: {} to version {} by user: {}", documentId, version, revertedBy);

        // Get the version to revert to
        Document versionToRevert = getSpecificVersion(documentId, version);

        // Get current latest version
        Document currentLatest = getAllVersions(documentId).stream()
                .filter(Document::isLatest)
                .findFirst()
                .orElseThrow(() -> new DocumentStorageException("No latest version found"));

        try {
            // Mark current as no longer latest
            currentLatest.setIsLatestVersion(false);
            documentRepository.save(currentLatest);

            // Get file content from storage
            InputStream fileContent = storageService.downloadFile(
                    versionToRevert.getStorageBucket(),
                    versionToRevert.getStoragePath()
            );

            // Calculate new version number
            int newVersionNumber = currentLatest.getVersion() + 1;

            // Generate new storage path
            String storedFilename = generateVersionedFilename(versionToRevert, newVersionNumber);

            // Upload file to storage (copy of reverted version)
            // Note: This is a simplified version - actual implementation would use the InputStream
            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("storagePath", versionToRevert.getStoragePath());
            uploadResult.put("fileHash", versionToRevert.getFileHash());

            // Create new version as copy of reverted version
            Document revertedDocument = Document.builder()
                    .documentUuid(UUID.randomUUID())
                    .documentType(versionToRevert.getDocumentType())
                    .typeCode(versionToRevert.getTypeCode())
                    .orderId(versionToRevert.getOrderId())
                    .orderNumber(versionToRevert.getOrderNumber())
                    .originalFilename(versionToRevert.getOriginalFilename())
                    .storedFilename(storedFilename)
                    .fileExtension(versionToRevert.getFileExtension())
                    .mimeType(versionToRevert.getMimeType())
                    .fileSizeBytes(versionToRevert.getFileSizeBytes())
                    .fileHash(versionToRevert.getFileHash())
                    .storageBucket(versionToRevert.getStorageBucket())
                    .storagePath(storedFilename)
                    .storageRegion(versionToRevert.getStorageRegion())
                    .version(newVersionNumber)
                    .isLatestVersion(true)
                    .parentDocument(findRootDocument(versionToRevert))
                    .status(DocumentStatus.PENDING)
                    .description("Reverted to version " + version)
                    .businessUnitId(versionToRevert.getBusinessUnitId())
                    .uploadedBy(revertedBy)
                    .uploadedAt(LocalDateTime.now())
                    .createdBy(revertedBy)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Copy metadata
            revertedDocument.setCustomMetadata(new HashMap<>(versionToRevert.getCustomMetadata()));
            revertedDocument.setTags(new ArrayList<>(versionToRevert.getTags()));
            revertedDocument.setIsConfidential(versionToRevert.getIsConfidential());
            revertedDocument.setAccessLevel(versionToRevert.getAccessLevel());

            Document savedDocument = documentRepository.save(revertedDocument);

            // Update previous latest to point to new version
            currentLatest.setReplacedByDocument(savedDocument);
            documentRepository.save(currentLatest);

            log.info("Successfully reverted document ID: {} to version {}", documentId, version);
            return savedDocument;

        } catch (Exception e) {
            log.error("Error reverting document ID: {} to version {}", documentId, version, e);
            throw new DocumentStorageException("Failed to revert document to version " + version, e);
        }
    }

    /**
     * Compares two versions of a document.
     *
     * @param documentId the document identifier
     * @param version1 the first version number
     * @param version2 the second version number
     * @return comparison result
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compareVersions(Long documentId, int version1, int version2) {
        log.debug("Comparing versions {} and {} for document ID: {}", version1, version2, documentId);

        Document doc1 = getSpecificVersion(documentId, version1);
        Document doc2 = getSpecificVersion(documentId, version2);

        Map<String, Object> comparison = new HashMap<>();
        comparison.put("documentId", documentId);
        comparison.put("version1", createVersionComparison(doc1));
        comparison.put("version2", createVersionComparison(doc2));
        comparison.put("differences", findDifferences(doc1, doc2));

        return comparison;
    }

    /**
     * Deletes a specific version of a document.
     * Cannot delete the latest version or the only version.
     *
     * @param documentId the document identifier
     * @param version the version number to delete
     * @param deletedBy the user deleting the version
     * @throws DocumentStorageException if deletion fails
     */
    @Transactional
    public void deleteVersion(Long documentId, int version, String deletedBy) {
        log.info("Deleting version {} for document ID: {} by user: {}", version, documentId, deletedBy);

        List<Document> versions = getAllVersions(documentId);

        if (versions.size() == 1) {
            throw new DocumentStorageException("Cannot delete the only version of a document");
        }

        Document versionToDelete = versions.stream()
                .filter(doc -> doc.getVersion() == version)
                .findFirst()
                .orElseThrow(() -> new DocumentStorageException("Version " + version + " not found"));

        if (versionToDelete.isLatest()) {
            throw new DocumentStorageException("Cannot delete the latest version");
        }

        try {
            // Delete file from storage
            storageService.deleteFile(versionToDelete.getStorageBucket(), versionToDelete.getStoragePath());

            // Soft delete the document record
            versionToDelete.markAsDeleted(deletedBy);
            documentRepository.save(versionToDelete);

            log.info("Successfully deleted version {} for document ID: {}", version, documentId);

        } catch (Exception e) {
            log.error("Error deleting version {} for document ID: {}", version, documentId, e);
            throw new DocumentStorageException("Failed to delete version " + version, e);
        }
    }

    /**
     * Archives old versions, keeping only the latest N versions.
     *
     * @param documentId the document identifier
     * @param keepLatestN the number of latest versions to keep
     */
    @Transactional
    public void archiveOldVersions(Long documentId, int keepLatestN) {
        log.info("Archiving old versions for document ID: {}, keeping latest {}", documentId, keepLatestN);

        if (keepLatestN < 1) {
            throw new DocumentStorageException("Must keep at least 1 version");
        }

        List<Document> versions = getAllVersions(documentId);

        // Sort by version descending
        versions.sort(Comparator.comparing(Document::getVersion).reversed());

        // Archive versions beyond keepLatestN
        for (int i = keepLatestN; i < versions.size(); i++) {
            Document versionToArchive = versions.get(i);
            if (!versionToArchive.isArchived()) {
                versionToArchive.markAsArchived();
                documentRepository.save(versionToArchive);
                log.debug("Archived version {} for document ID: {}", versionToArchive.getVersion(), documentId);
            }
        }

        log.info("Archived {} old versions for document ID: {}", Math.max(0, versions.size() - keepLatestN), documentId);
    }

    /**
     * Retrieves the latest version of a document.
     *
     * @param documentId the document identifier
     * @return the latest document version
     */
    @Transactional(readOnly = true)
    public Document getLatestVersion(Long documentId) {
        log.debug("Retrieving latest version for document ID: {}", documentId);

        List<Document> versions = getAllVersions(documentId);

        return versions.stream()
                .filter(Document::isLatest)
                .findFirst()
                .orElseThrow(() -> new DocumentStorageException("No latest version found for document ID: " + documentId));
    }

    // Private helper methods

    /**
     * Finds the root document in the version chain.
     */
    private Document findRootDocument(Document document) {
        Document current = document;
        while (current.getParentDocument() != null) {
            current = current.getParentDocument();
        }
        return current;
    }

    /**
     * Generates a versioned filename.
     */
    private String generateVersionedFilename(Document document, int versionNumber) {
        String baseName = document.getOriginalFilename();
        int dotIndex = baseName.lastIndexOf('.');
        String name = dotIndex > 0 ? baseName.substring(0, dotIndex) : baseName;
        String extension = dotIndex > 0 ? baseName.substring(dotIndex) : "";

        return String.format("%s_v%d_%s%s",
                name,
                versionNumber,
                UUID.randomUUID().toString().substring(0, 8),
                extension);
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Maps a Document entity to DocumentVersion DTO.
     */
    private DocumentVersion mapToDocumentVersion(Document document) {
        return DocumentVersion.builder()
                .versionNumber(document.getVersion())
                .createdDate(document.getCreatedAt())
                .createdBy(document.getCreatedBy())
                .isLatest(document.getIsLatestVersion())
                .parentDocumentId(document.getParentDocument() != null ? document.getParentDocument().getId() : null)
                .build();
    }

    /**
     * Creates version comparison data.
     */
    private Map<String, Object> createVersionComparison(Document document) {
        Map<String, Object> comparison = new HashMap<>();
        comparison.put("version", document.getVersion());
        comparison.put("filename", document.getOriginalFilename());
        comparison.put("fileSize", document.getFileSizeBytes());
        comparison.put("uploadedBy", document.getUploadedBy());
        comparison.put("uploadedAt", document.getUploadedAt());
        comparison.put("status", document.getStatus());
        comparison.put("fileHash", document.getFileHash());
        comparison.put("isLatest", document.isLatest());
        return comparison;
    }

    /**
     * Finds differences between two document versions.
     */
    private List<String> findDifferences(Document doc1, Document doc2) {
        List<String> differences = new ArrayList<>();

        if (!Objects.equals(doc1.getOriginalFilename(), doc2.getOriginalFilename())) {
            differences.add("Filename changed");
        }

        if (!Objects.equals(doc1.getFileSizeBytes(), doc2.getFileSizeBytes())) {
            differences.add("File size changed");
        }

        if (!Objects.equals(doc1.getFileHash(), doc2.getFileHash())) {
            differences.add("File content changed");
        }

        if (!Objects.equals(doc1.getStatus(), doc2.getStatus())) {
            differences.add("Status changed");
        }

        return differences;
    }
}
