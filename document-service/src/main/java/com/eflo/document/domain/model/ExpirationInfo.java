package com.eflo.document.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Model class representing document expiration information.
 * Contains expiration date, remaining days, expiration status, and notification details.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpirationInfo {

    /**
     * Expiration date of the document.
     */
    private LocalDate expirationDate;

    /**
     * Number of days remaining until expiration.
     * Negative if already expired.
     */
    private Long daysRemaining;

    /**
     * Flag indicating if the document is expired.
     */
    @Builder.Default
    private Boolean isExpired = false;

    /**
     * Flag indicating if the document is expiring soon (within warning period).
     */
    @Builder.Default
    private Boolean isExpiringSoon = false;

    /**
     * Flag indicating if expiration notification has been sent.
     */
    @Builder.Default
    private Boolean notified = false;

    /**
     * Date and time when expiration notification was sent.
     */
    private LocalDateTime notificationSentAt;

    /**
     * Number of days before expiration to send warning notification.
     */
    private Integer warningDays;

    /**
     * Expiration status.
     */
    private ExpirationStatus status;

    /**
     * Days since expiration (only if expired).
     */
    private Long daysSinceExpiration;

    /**
     * Date when the document was marked as expired.
     */
    private LocalDateTime expiredAt;

    /**
     * Expiration status enumeration.
     */
    public enum ExpirationStatus {
        VALID("Document is valid"),
        EXPIRING_SOON("Document expiring soon"),
        EXPIRED("Document has expired"),
        GRACE_PERIOD("Document in grace period after expiration"),
        NO_EXPIRATION("Document does not expire");

        private final String description;

        ExpirationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if the document is expired.
     *
     * @return true if expired
     */
    public boolean isDocumentExpired() {
        return Boolean.TRUE.equals(isExpired) || status == ExpirationStatus.EXPIRED;
    }

    /**
     * Check if the document is expiring soon.
     *
     * @return true if expiring soon
     */
    public boolean isDocumentExpiringSoon() {
        return Boolean.TRUE.equals(isExpiringSoon) || status == ExpirationStatus.EXPIRING_SOON;
    }

    /**
     * Check if the document is valid (not expired or expiring).
     *
     * @return true if valid
     */
    public boolean isValid() {
        return status == ExpirationStatus.VALID || status == ExpirationStatus.NO_EXPIRATION;
    }

    /**
     * Check if notification is needed.
     *
     * @return true if notification should be sent
     */
    public boolean needsNotification() {
        return Boolean.TRUE.equals(isExpiringSoon) && !Boolean.TRUE.equals(notified);
    }

    /**
     * Check if the document has no expiration date.
     *
     * @return true if no expiration
     */
    public boolean hasNoExpiration() {
        return expirationDate == null || status == ExpirationStatus.NO_EXPIRATION;
    }

    /**
     * Get formatted expiration date.
     *
     * @return formatted date string
     */
    public String getFormattedExpirationDate() {
        if (expirationDate == null) {
            return "No expiration";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return expirationDate.format(formatter);
    }

    /**
     * Get human-readable expiration status message.
     *
     * @return status message
     */
    public String getStatusMessage() {
        if (hasNoExpiration()) {
            return "This document does not expire";
        }
        if (isDocumentExpired()) {
            if (daysSinceExpiration != null && daysSinceExpiration > 0) {
                return String.format("Expired %d day(s) ago", daysSinceExpiration);
            }
            return "Document has expired";
        }
        if (isDocumentExpiringSoon()) {
            return String.format("Expires in %d day(s)", daysRemaining != null ? daysRemaining : 0);
        }
        if (daysRemaining != null) {
            return String.format("Valid for %d more day(s)", daysRemaining);
        }
        return "Valid";
    }

    /**
     * Get urgency level based on days remaining.
     *
     * @return urgency level (HIGH, MEDIUM, LOW, NONE)
     */
    public UrgencyLevel getUrgencyLevel() {
        if (isDocumentExpired()) {
            return UrgencyLevel.CRITICAL;
        }
        if (daysRemaining == null) {
            return UrgencyLevel.NONE;
        }
        if (daysRemaining <= 7) {
            return UrgencyLevel.HIGH;
        }
        if (daysRemaining <= 30) {
            return UrgencyLevel.MEDIUM;
        }
        return UrgencyLevel.LOW;
    }

    /**
     * Urgency levels for expiration.
     */
    public enum UrgencyLevel {
        CRITICAL,  // Already expired
        HIGH,      // Expires within 7 days
        MEDIUM,    // Expires within 30 days
        LOW,       // Expires after 30 days
        NONE       // No expiration or very far in future
    }

    /**
     * Create ExpirationInfo from expiration date.
     *
     * @param expirationDate the expiration date
     * @param warningDays days before expiration to warn
     * @return ExpirationInfo instance
     */
    public static ExpirationInfo from(LocalDate expirationDate, Integer warningDays) {
        if (expirationDate == null) {
            return ExpirationInfo.builder()
                    .status(ExpirationStatus.NO_EXPIRATION)
                    .build();
        }

        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, expirationDate);
        boolean isExpired = daysRemaining < 0;
        boolean isExpiringSoon = !isExpired && warningDays != null && daysRemaining <= warningDays;

        ExpirationStatus status;
        if (isExpired) {
            status = ExpirationStatus.EXPIRED;
        } else if (isExpiringSoon) {
            status = ExpirationStatus.EXPIRING_SOON;
        } else {
            status = ExpirationStatus.VALID;
        }

        Long daysSinceExpiration = isExpired ? Math.abs(daysRemaining) : null;

        return ExpirationInfo.builder()
                .expirationDate(expirationDate)
                .daysRemaining(daysRemaining)
                .isExpired(isExpired)
                .isExpiringSoon(isExpiringSoon)
                .warningDays(warningDays)
                .status(status)
                .daysSinceExpiration(daysSinceExpiration)
                .build();
    }

    /**
     * Create ExpirationInfo for a document without expiration.
     *
     * @return ExpirationInfo with no expiration status
     */
    public static ExpirationInfo noExpiration() {
        return ExpirationInfo.builder()
                .status(ExpirationStatus.NO_EXPIRATION)
                .isExpired(false)
                .isExpiringSoon(false)
                .build();
    }

    /**
     * Mark notification as sent.
     */
    public void markNotificationSent() {
        this.notified = true;
        this.notificationSentAt = LocalDateTime.now();
    }

    /**
     * Calculate and update days remaining.
     */
    public void updateDaysRemaining() {
        if (expirationDate != null) {
            LocalDate today = LocalDate.now();
            this.daysRemaining = ChronoUnit.DAYS.between(today, expirationDate);
            this.isExpired = this.daysRemaining < 0;
            if (this.isExpired) {
                this.daysSinceExpiration = Math.abs(this.daysRemaining);
            }
            if (warningDays != null && !isExpired) {
                this.isExpiringSoon = this.daysRemaining <= warningDays;
            }
        }
    }
}
