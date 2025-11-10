package com.eflo.workflow.domain.enums;

/**
 * Approval status for task approvals
 */
public enum ApprovalStatus {
    PENDING("En attente", "Pending"),
    APPROVED("Approuvé", "Approved"),
    REJECTED("Rejeté", "Rejected"),
    DELEGATED("Délégué", "Delegated"),
    TIMEOUT("Expiré", "Timeout");

    private final String displayNameFr;
    private final String displayNameEn;

    ApprovalStatus(String displayNameFr, String displayNameEn) {
        this.displayNameFr = displayNameFr;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayNameFr() {
        return displayNameFr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public boolean isCompleted() {
        return this == APPROVED || this == REJECTED;
    }

    public boolean isPending() {
        return this == PENDING;
    }
}
