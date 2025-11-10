package com.eflo.document.scanner;

import com.eflo.document.domain.enums.VirusScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Transfer Object representing the result of a virus scan operation.
 * Contains the scan status, timestamp, threat information, and additional metadata.
 *
 * @author Eflo Document Service
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResult {

    /**
     * The status of the virus scan
     */
    private VirusScanStatus status;

    /**
     * Timestamp when the scan was performed
     */
    @Builder.Default
    private LocalDateTime scanDate = LocalDateTime.now();

    /**
     * Name or signature of the detected threat (if any)
     */
    private String threatName;

    /**
     * Detailed scan message or description
     */
    private String message;

    /**
     * Name of the scanned file
     */
    private String fileName;

    /**
     * Size of the scanned file in bytes
     */
    private Long fileSize;

    /**
     * Duration of the scan in milliseconds
     */
    private Long scanDurationMs;

    /**
     * Scanner engine used (e.g., "ClamAV")
     */
    private String scannerEngine;

    /**
     * Version of the scanner engine
     */
    private String scannerVersion;

    /**
     * Additional scan metadata and details
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Creates a successful clean scan result
     *
     * @param fileName the name of the scanned file
     * @return ScanResult with CLEAN status
     */
    public static ScanResult clean(String fileName) {
        return ScanResult.builder()
                .status(VirusScanStatus.CLEAN)
                .fileName(fileName)
                .message("No threats detected")
                .scanDate(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an infected scan result
     *
     * @param fileName    the name of the scanned file
     * @param threatName  the name of the detected threat
     * @return ScanResult with INFECTED status
     */
    public static ScanResult infected(String fileName, String threatName) {
        return ScanResult.builder()
                .status(VirusScanStatus.INFECTED)
                .fileName(fileName)
                .threatName(threatName)
                .message("Threat detected: " + threatName)
                .scanDate(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a failed scan result
     *
     * @param fileName the name of the scanned file
     * @param reason   the reason for failure
     * @return ScanResult with FAILED status
     */
    public static ScanResult failed(String fileName, String reason) {
        return ScanResult.builder()
                .status(VirusScanStatus.FAILED)
                .fileName(fileName)
                .message("Scan failed: " + reason)
                .scanDate(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a skipped scan result
     *
     * @param fileName the name of the scanned file
     * @param reason   the reason for skipping
     * @return ScanResult with SKIPPED status
     */
    public static ScanResult skipped(String fileName, String reason) {
        return ScanResult.builder()
                .status(VirusScanStatus.SKIPPED)
                .fileName(fileName)
                .message("Scan skipped: " + reason)
                .scanDate(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a pending scan result
     *
     * @param fileName the name of the file to be scanned
     * @return ScanResult with PENDING status
     */
    public static ScanResult pending(String fileName) {
        return ScanResult.builder()
                .status(VirusScanStatus.PENDING)
                .fileName(fileName)
                .message("Scan pending")
                .scanDate(LocalDateTime.now())
                .build();
    }

    /**
     * Adds metadata to the scan result
     *
     * @param key   the metadata key
     * @param value the metadata value
     */
    public void addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
    }

    /**
     * Converts scan result to a map suitable for database storage
     *
     * @return Map representation of the scan result
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", status != null ? status.name() : null);
        result.put("scanDate", scanDate != null ? scanDate.toString() : null);
        result.put("threatName", threatName);
        result.put("message", message);
        result.put("fileName", fileName);
        result.put("fileSize", fileSize);
        result.put("scanDurationMs", scanDurationMs);
        result.put("scannerEngine", scannerEngine);
        result.put("scannerVersion", scannerVersion);
        if (metadata != null && !metadata.isEmpty()) {
            result.put("metadata", metadata);
        }
        return result;
    }

    /**
     * Checks if the scan was successful (clean or skipped)
     *
     * @return true if the file is safe to use
     */
    public boolean isSafe() {
        return status != null && status.isSafe();
    }

    /**
     * Checks if the scan requires action (infected or failed)
     *
     * @return true if action is required
     */
    public boolean requiresAction() {
        return status != null && status.requiresAction();
    }
}
