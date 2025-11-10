package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing document version information.
 * Contains version number, creation details, and version metadata.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    /**
     * Version number of the document.
     * Starts at 1 and increments with each new version.
     */
    private Integer versionNumber;

    /**
     * Date and time when this version was created.
     */
    private LocalDateTime createdDate;

    /**
     * User who created this version.
     */
    private String createdBy;

    /**
     * Flag indicating if this is the latest version.
     */
    @Builder.Default
    private Boolean isLatest = true;

    /**
     * ID of the parent document (original document ID).
     */
    private Long parentDocumentId;

    /**
     * ID of the previous version.
     */
    private Long previousVersionId;

    /**
     * ID of the next version (if this is not the latest).
     */
    private Long nextVersionId;

    /**
     * Reason or description for creating this version.
     */
    private String versionReason;

    /**
     * Change summary describing what changed in this version.
     */
    private String changeSummary;

    /**
     * Size difference compared to previous version (in bytes).
     * Positive = larger, Negative = smaller
     */
    private Long sizeDelta;

    /**
     * Version label or tag (e.g., "Draft", "Final", "Approved").
     */
    private String versionLabel;

    /**
     * Version status.
     */
    private VersionStatus status;

    /**
     * Version status enumeration.
     */
    public enum VersionStatus {
        DRAFT("Draft version"),
        ACTIVE("Active version"),
        ARCHIVED("Archived version"),
        SUPERSEDED("Superseded by newer version"),
        DELETED("Deleted version");

        private final String description;

        VersionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if this is the first version.
     *
     * @return true if version number is 1
     */
    public boolean isFirstVersion() {
        return versionNumber != null && versionNumber == 1;
    }

    /**
     * Check if this is the latest version.
     *
     * @return true if this is the latest version
     */
    public boolean isLatestVersion() {
        return Boolean.TRUE.equals(isLatest);
    }

    /**
     * Check if this version has been superseded.
     *
     * @return true if there is a next version
     */
    public boolean isSuperseded() {
        return nextVersionId != null || status == VersionStatus.SUPERSEDED;
    }

    /**
     * Check if this version is active.
     *
     * @return true if status is ACTIVE
     */
    public boolean isActive() {
        return status == VersionStatus.ACTIVE;
    }

    /**
     * Check if this version is archived.
     *
     * @return true if status is ARCHIVED
     */
    public boolean isArchived() {
        return status == VersionStatus.ARCHIVED;
    }

    /**
     * Get formatted version string.
     *
     * @return formatted version string (e.g., "v1", "v2")
     */
    public String getFormattedVersion() {
        return versionNumber != null ? "v" + versionNumber : "v0";
    }

    /**
     * Get formatted creation date.
     *
     * @return formatted date string
     */
    public String getFormattedCreatedDate() {
        if (createdDate == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return createdDate.format(formatter);
    }

    /**
     * Get version display text.
     *
     * @return display text with version number and label
     */
    public String getDisplayText() {
        StringBuilder display = new StringBuilder(getFormattedVersion());
        if (versionLabel != null && !versionLabel.isEmpty()) {
            display.append(" - ").append(versionLabel);
        }
        if (isLatest) {
            display.append(" (Latest)");
        }
        return display.toString();
    }

    /**
     * Check if size increased compared to previous version.
     *
     * @return true if size increased
     */
    public boolean sizeIncreased() {
        return sizeDelta != null && sizeDelta > 0;
    }

    /**
     * Check if size decreased compared to previous version.
     *
     * @return true if size decreased
     */
    public boolean sizeDecreased() {
        return sizeDelta != null && sizeDelta < 0;
    }

    /**
     * Get formatted size delta.
     *
     * @return formatted size change string
     */
    public String getFormattedSizeDelta() {
        if (sizeDelta == null) {
            return "No change";
        }
        if (sizeDelta == 0) {
            return "Same size";
        }
        String sign = sizeDelta > 0 ? "+" : "";
        if (Math.abs(sizeDelta) < 1024) {
            return sign + sizeDelta + " B";
        } else if (Math.abs(sizeDelta) < 1024 * 1024) {
            return String.format("%s%.2f KB", sign, sizeDelta / 1024.0);
        } else {
            return String.format("%s%.2f MB", sign, sizeDelta / (1024.0 * 1024.0));
        }
    }

    /**
     * Create initial version.
     *
     * @param createdBy user who created the version
     * @return DocumentVersion for version 1
     */
    public static DocumentVersion initial(String createdBy) {
        return DocumentVersion.builder()
                .versionNumber(1)
                .createdDate(LocalDateTime.now())
                .createdBy(createdBy)
                .isLatest(true)
                .status(VersionStatus.ACTIVE)
                .versionLabel("Initial")
                .build();
    }

    /**
     * Create next version from current version.
     *
     * @param currentVersion the current version
     * @param createdBy user who created the new version
     * @param reason reason for new version
     * @return DocumentVersion for next version
     */
    public static DocumentVersion nextVersion(DocumentVersion currentVersion, String createdBy, String reason) {
        return DocumentVersion.builder()
                .versionNumber(currentVersion.getVersionNumber() + 1)
                .createdDate(LocalDateTime.now())
                .createdBy(createdBy)
                .isLatest(true)
                .previousVersionId(currentVersion.getParentDocumentId())
                .versionReason(reason)
                .status(VersionStatus.ACTIVE)
                .build();
    }
}
