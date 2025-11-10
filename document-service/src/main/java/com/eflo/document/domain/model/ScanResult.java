package com.eflo.document.domain.model;

import com.eflo.document.domain.enums.VirusScanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model class representing virus scan result information.
 * Contains scan status, detected threats, scan date, and detailed scan information.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {

    /**
     * Status of the virus scan.
     */
    private VirusScanStatus scanStatus;

    /**
     * List of detected threats (viruses, malware, etc.).
     */
    private List<ThreatInfo> threats;

    /**
     * Date and time when the scan was performed.
     */
    private LocalDateTime scanDate;

    /**
     * Detailed scan information and metadata.
     */
    private Map<String, Object> details;

    /**
     * Name of the scanning engine or service used.
     */
    private String scanEngine;

    /**
     * Version of the scanning engine.
     */
    private String scanEngineVersion;

    /**
     * Virus definition version used for scanning.
     */
    private String definitionVersion;

    /**
     * Duration of the scan in milliseconds.
     */
    private Long scanDurationMs;

    /**
     * Scan result message.
     */
    private String message;

    /**
     * Recommended action based on scan result.
     */
    private String recommendedAction;

    /**
     * Information about a detected threat.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThreatInfo {

        /**
         * Name of the detected threat.
         */
        private String threatName;

        /**
         * Type of threat (virus, malware, trojan, etc.).
         */
        private String threatType;

        /**
         * Severity level of the threat.
         */
        private ThreatSeverity severity;

        /**
         * Description of the threat.
         */
        private String description;

        /**
         * Location where the threat was found (e.g., file path, offset).
         */
        private String location;

        /**
         * Action taken on the threat (quarantine, delete, etc.).
         */
        private String actionTaken;
    }

    /**
     * Threat severity levels.
     */
    public enum ThreatSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    /**
     * Check if the scan found no threats (clean).
     *
     * @return true if scan is clean
     */
    public boolean isClean() {
        return scanStatus == VirusScanStatus.CLEAN ||
               scanStatus == VirusScanStatus.SKIPPED ||
               (threats == null || threats.isEmpty());
    }

    /**
     * Check if threats were detected.
     *
     * @return true if threats were detected
     */
    public boolean hasThreats() {
        return threats != null && !threats.isEmpty();
    }

    /**
     * Check if scan is pending.
     *
     * @return true if scan is pending
     */
    public boolean isPending() {
        return scanStatus == VirusScanStatus.PENDING;
    }

    /**
     * Check if scan failed.
     *
     * @return true if scan failed
     */
    public boolean isFailed() {
        return scanStatus == VirusScanStatus.FAILED;
    }

    /**
     * Check if the file is infected.
     *
     * @return true if file is infected
     */
    public boolean isInfected() {
        return scanStatus == VirusScanStatus.INFECTED;
    }

    /**
     * Get the count of detected threats.
     *
     * @return number of threats
     */
    public int getThreatCount() {
        return threats != null ? threats.size() : 0;
    }

    /**
     * Check if any critical threats were detected.
     *
     * @return true if critical threats exist
     */
    public boolean hasCriticalThreats() {
        if (threats == null) {
            return false;
        }
        return threats.stream()
                .anyMatch(threat -> threat.getSeverity() == ThreatSeverity.CRITICAL);
    }

    /**
     * Get list of threat names.
     *
     * @return list of threat names
     */
    public List<String> getThreatNames() {
        if (threats == null) {
            return List.of();
        }
        return threats.stream()
                .map(ThreatInfo::getThreatName)
                .toList();
    }

    /**
     * Create a clean scan result.
     *
     * @param scanEngine the scanning engine name
     * @return ScanResult indicating clean scan
     */
    public static ScanResult clean(String scanEngine) {
        return ScanResult.builder()
                .scanStatus(VirusScanStatus.CLEAN)
                .scanDate(LocalDateTime.now())
                .scanEngine(scanEngine)
                .message("No threats detected")
                .build();
    }

    /**
     * Create a pending scan result.
     *
     * @return ScanResult indicating pending scan
     */
    public static ScanResult pending() {
        return ScanResult.builder()
                .scanStatus(VirusScanStatus.PENDING)
                .message("Virus scan pending")
                .build();
    }

    /**
     * Create an infected scan result.
     *
     * @param threats list of detected threats
     * @param scanEngine the scanning engine name
     * @return ScanResult indicating infected file
     */
    public static ScanResult infected(List<ThreatInfo> threats, String scanEngine) {
        return ScanResult.builder()
                .scanStatus(VirusScanStatus.INFECTED)
                .threats(threats)
                .scanDate(LocalDateTime.now())
                .scanEngine(scanEngine)
                .message(String.format("%d threat(s) detected", threats.size()))
                .recommendedAction("Quarantine or delete the file")
                .build();
    }

    /**
     * Create a failed scan result.
     *
     * @param errorMessage the error message
     * @return ScanResult indicating scan failure
     */
    public static ScanResult failed(String errorMessage) {
        return ScanResult.builder()
                .scanStatus(VirusScanStatus.FAILED)
                .scanDate(LocalDateTime.now())
                .message(errorMessage)
                .recommendedAction("Retry scan or manual inspection required")
                .build();
    }
}
