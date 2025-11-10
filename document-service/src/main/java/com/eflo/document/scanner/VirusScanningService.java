package com.eflo.document.scanner;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.VirusScanStatus;
import com.eflo.document.domain.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing virus scanning operations on documents.
 * Provides comprehensive virus scanning functionality including single file scans,
 * batch scans, scheduled scans, and rescans. Delegates actual scanning to ClamAVScanner.
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Scan documents from InputStream or file path</li>
 *   <li>Batch scanning for multiple files</li>
 *   <li>Asynchronous scheduled scanning</li>
 *   <li>Rescan functionality for existing documents</li>
 *   <li>Scan status tracking and reporting</li>
 *   <li>Automatic database updates with scan results</li>
 * </ul>
 *
 * @author Eflo Document Service
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VirusScanningService {

    private final ClamAVScanner clamAVScanner;
    private final DocumentRepository documentRepository;
    private final FileMetadataExtractor metadataExtractor;

    /**
     * Scans a document from an input stream.
     * This is the primary method for scanning uploaded files before storage.
     *
     * @param inputStream the input stream containing the file data
     * @param fileName    the name of the file being scanned
     * @return ScanResult containing scan status and details
     * @throws IOException if stream reading fails
     */
    public ScanResult scanDocument(InputStream inputStream, String fileName) throws IOException {
        if (inputStream == null) {
            throw new IOException("InputStream cannot be null");
        }

        if (fileName == null || fileName.isEmpty()) {
            fileName = "unknown";
        }

        log.info("Starting virus scan for file: {}", fileName);

        try {
            // For ClamAV, we need to either use the stream directly or save to temp file
            // Using mark/reset to allow stream reuse
            if (inputStream.markSupported()) {
                inputStream.mark(Integer.MAX_VALUE);
            }

            ScanResult result = clamAVScanner.scanWithClamAV(inputStream, fileName);

            // Reset stream if possible
            if (inputStream.markSupported()) {
                try {
                    inputStream.reset();
                } catch (IOException e) {
                    log.warn("Failed to reset input stream after scan: {}", e.getMessage());
                }
            }

            log.info("Virus scan completed for file: {} - Status: {}", fileName, result.getStatus());

            return result;

        } catch (Exception e) {
            log.error("Error during virus scan for file: {} - {}", fileName, e.getMessage(), e);
            return ScanResult.failed(fileName, "Scan error: " + e.getMessage());
        }
    }

    /**
     * Scans a document from a file path.
     * Useful for scanning files already stored on disk.
     *
     * @param filePath the path to the file to scan
     * @return ScanResult containing scan status and details
     * @throws IOException if file reading fails
     */
    public ScanResult scanDocument(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException("File path cannot be null or empty");
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File does not exist: " + filePath);
        }

        String fileName = path.getFileName().toString();
        long fileSize = Files.size(path);

        log.info("Starting virus scan for file at path: {} (size: {} bytes)", filePath, fileSize);

        try (InputStream inputStream = Files.newInputStream(path)) {
            ScanResult result = clamAVScanner.scanWithClamAV(inputStream, fileName);
            result.setFileSize(fileSize);

            // Optionally add file metadata
            try {
                String mimeType = metadataExtractor.detectMimeType(path.toFile());
                result.addMetadata("mime_type", mimeType);
                result.addMetadata("file_path", filePath);
            } catch (Exception e) {
                log.debug("Failed to extract metadata during scan: {}", e.getMessage());
            }

            log.info("Virus scan completed for file: {} - Status: {}", fileName, result.getStatus());

            return result;

        } catch (Exception e) {
            log.error("Error scanning file at path {}: {}", filePath, e.getMessage(), e);
            throw new IOException("Scan failed: " + e.getMessage(), e);
        }
    }

    /**
     * Scans multiple documents in batch.
     * Processes files sequentially and returns results for all files.
     *
     * @param filePaths list of file paths to scan
     * @return list of ScanResults for each file
     */
    public List<ScanResult> scanBatch(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            log.warn("Batch scan called with empty file list");
            return Collections.emptyList();
        }

        log.info("Starting batch virus scan for {} files", filePaths.size());
        long startTime = System.currentTimeMillis();

        List<ScanResult> results = filePaths.stream()
                .map(filePath -> {
                    try {
                        return scanDocument(filePath);
                    } catch (IOException e) {
                        log.error("Failed to scan file in batch: {} - {}", filePath, e.getMessage());
                        return ScanResult.failed(Paths.get(filePath).getFileName().toString(),
                                "Batch scan error: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        long duration = System.currentTimeMillis() - startTime;
        long cleanCount = results.stream().filter(r -> r.getStatus() == VirusScanStatus.CLEAN).count();
        long infectedCount = results.stream().filter(r -> r.getStatus() == VirusScanStatus.INFECTED).count();
        long failedCount = results.stream().filter(r -> r.getStatus() == VirusScanStatus.FAILED).count();

        log.info("Batch scan completed - Total: {}, Clean: {}, Infected: {}, Failed: {}, Duration: {}ms",
                results.size(), cleanCount, infectedCount, failedCount, duration);

        return results;
    }

    /**
     * Schedules an asynchronous virus scan for a document.
     * Updates the document entity with scan results upon completion.
     * This method runs asynchronously and doesn't block the caller.
     *
     * @param document the document to scan
     */
    @Async
    @Transactional
    public void scheduleScan(Document document) {
        if (document == null) {
            log.warn("Cannot schedule scan - document is null");
            return;
        }

        log.info("Scheduling virus scan for document ID: {}, UUID: {}, File: {}",
                document.getId(), document.getDocumentUuid(), document.getOriginalFilename());

        try {
            // Mark as pending if not already
            if (document.getVirusScanStatus() == null) {
                document.setVirusScanStatus(VirusScanStatus.PENDING);
                documentRepository.save(document);
            }

            // Construct file path from storage information
            String filePath = constructFilePath(document);

            // Perform the scan
            ScanResult scanResult = scanDocument(filePath);

            // Update document with scan results
            updateDocumentWithScanResult(document, scanResult);

            log.info("Scheduled scan completed for document ID: {} - Status: {}",
                    document.getId(), scanResult.getStatus());

        } catch (Exception e) {
            log.error("Error during scheduled scan for document ID: {} - {}",
                    document.getId(), e.getMessage(), e);

            // Update document with failed status
            ScanResult failedResult = ScanResult.failed(document.getOriginalFilename(),
                    "Scheduled scan error: " + e.getMessage());
            updateDocumentWithScanResult(document, failedResult);
        }
    }

    /**
     * Rescans an existing document by its ID.
     * Useful for rescanning documents with failed or infected status.
     *
     * @param documentId the ID of the document to rescan
     * @return ScanResult containing updated scan status
     * @throws IOException if document not found or scan fails
     */
    @Transactional
    public ScanResult rescanDocument(Long documentId) throws IOException {
        if (documentId == null) {
            throw new IOException("Document ID cannot be null");
        }

        log.info("Rescanning document ID: {}", documentId);

        Optional<Document> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            throw new IOException("Document not found with ID: " + documentId);
        }

        Document document = documentOpt.get();

        // Verify document is not deleted
        if (document.isDeleted()) {
            throw new IOException("Cannot rescan deleted document ID: " + documentId);
        }

        String filePath = constructFilePath(document);
        ScanResult scanResult = scanDocument(filePath);

        // Update document with new scan results
        updateDocumentWithScanResult(document, scanResult);

        log.info("Rescan completed for document ID: {} - Status: {}", documentId, scanResult.getStatus());

        return scanResult;
    }

    /**
     * Gets the current virus scan status for a document.
     * Returns detailed scan information if available.
     *
     * @param documentId the ID of the document
     * @return ScanResult with current scan status, or null if not scanned
     * @throws IOException if document not found
     */
    public ScanResult getScanStatus(Long documentId) throws IOException {
        if (documentId == null) {
            throw new IOException("Document ID cannot be null");
        }

        Optional<Document> documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            throw new IOException("Document not found with ID: " + documentId);
        }

        Document document = documentOpt.get();

        // Build ScanResult from document data
        ScanResult.ScanResultBuilder builder = ScanResult.builder()
                .status(document.getVirusScanStatus())
                .fileName(document.getOriginalFilename())
                .fileSize(document.getFileSizeBytes())
                .scanDate(document.getVirusScanDate());

        // Extract additional info from stored scan result
        if (document.getVirusScanResult() != null) {
            Map<String, Object> storedResult = document.getVirusScanResult();

            if (storedResult.containsKey("threatName")) {
                builder.threatName((String) storedResult.get("threatName"));
            }
            if (storedResult.containsKey("message")) {
                builder.message((String) storedResult.get("message"));
            }
            if (storedResult.containsKey("scanDurationMs")) {
                Object duration = storedResult.get("scanDurationMs");
                if (duration instanceof Number) {
                    builder.scanDurationMs(((Number) duration).longValue());
                }
            }
            if (storedResult.containsKey("scannerEngine")) {
                builder.scannerEngine((String) storedResult.get("scannerEngine"));
            }
            if (storedResult.containsKey("scannerVersion")) {
                builder.scannerVersion((String) storedResult.get("scannerVersion"));
            }
            if (storedResult.containsKey("metadata")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadata = (Map<String, Object>) storedResult.get("metadata");
                builder.metadata(metadata);
            }
        }

        ScanResult result = builder.build();

        log.debug("Retrieved scan status for document ID: {} - Status: {}", documentId, result.getStatus());

        return result;
    }

    /**
     * Scans all pending documents.
     * Useful for batch processing of documents awaiting virus scan.
     *
     * @return list of ScanResults for all pending documents
     */
    @Transactional
    public List<ScanResult> scanPendingDocuments() {
        log.info("Scanning all pending documents");

        List<Document> pendingDocuments = documentRepository.findPendingVirusScan();

        if (pendingDocuments.isEmpty()) {
            log.info("No pending documents to scan");
            return Collections.emptyList();
        }

        log.info("Found {} documents pending virus scan", pendingDocuments.size());

        List<ScanResult> results = new ArrayList<>();

        for (Document document : pendingDocuments) {
            try {
                String filePath = constructFilePath(document);
                ScanResult scanResult = scanDocument(filePath);
                updateDocumentWithScanResult(document, scanResult);
                results.add(scanResult);

                log.info("Scanned pending document ID: {} - Status: {}",
                        document.getId(), scanResult.getStatus());

            } catch (Exception e) {
                log.error("Failed to scan pending document ID: {} - {}",
                        document.getId(), e.getMessage(), e);

                ScanResult failedResult = ScanResult.failed(document.getOriginalFilename(),
                        "Error: " + e.getMessage());
                updateDocumentWithScanResult(document, failedResult);
                results.add(failedResult);
            }
        }

        return results;
    }

    /**
     * Checks if ClamAV scanner is available and operational.
     *
     * @return true if ClamAV is available, false otherwise
     */
    public boolean isScannerAvailable() {
        boolean available = clamAVScanner.isClamAVAvailable();
        log.debug("Scanner availability check: {}", available);
        return available;
    }

    /**
     * Gets the scanner engine configuration and status.
     *
     * @return configuration details as a map
     */
    public Map<String, Object> getScannerInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("enabled", clamAVScanner.isEnabled());
        info.put("available", clamAVScanner.isClamAVAvailable());
        info.put("configuration", clamAVScanner.getConfiguration());

        if (clamAVScanner.isClamAVAvailable()) {
            info.put("version", clamAVScanner.getVersion());
        }

        return info;
    }

    /**
     * Updates a document entity with scan results.
     * Saves the scan status, timestamp, and detailed results to the database.
     *
     * @param document   the document to update
     * @param scanResult the scan result to apply
     */
    private void updateDocumentWithScanResult(Document document, ScanResult scanResult) {
        if (document == null || scanResult == null) {
            return;
        }

        document.recordVirusScan(scanResult.getStatus(), scanResult.toMap());
        documentRepository.save(document);

        log.debug("Updated document ID: {} with scan result - Status: {}",
                document.getId(), scanResult.getStatus());
    }

    /**
     * Constructs the full file path for a document from its storage information.
     *
     * @param document the document
     * @return full file path
     * @throws IOException if path construction fails
     */
    private String constructFilePath(Document document) throws IOException {
        if (document.getStoragePath() == null || document.getStoragePath().isEmpty()) {
            throw new IOException("Document storage path is not set for document ID: " + document.getId());
        }

        // The storage path should be the full path or relative to a storage root
        // This depends on your storage implementation (local filesystem, MinIO, etc.)
        String filePath = document.getStoragePath();

        // If using local filesystem, verify file exists
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            // Try with stored filename if path doesn't exist
            filePath = document.getStoragePath() + "/" + document.getStoredFilename();
            path = Paths.get(filePath);

            if (!Files.exists(path)) {
                throw new IOException("File not found at path: " + filePath);
            }
        }

        return filePath;
    }
}
