package com.eflo.workflow.domain.enums;

/**
 * Type of approval required for an approval level
 */
public enum ApprovalType {
    /**
     * Only one person with the role needs to approve
     */
    SINGLE("Un seul", "Single"),

    /**
     * All people with the role must approve
     */
    ALL("Tous", "All"),

    /**
     * Majority (>50%) of people with the role must approve
     */
    MAJORITY("Majorité", "Majority");

    private final String displayNameFr;
    private final String displayNameEn;

    ApprovalType(String displayNameFr, String displayNameEn) {
        this.displayNameFr = displayNameFr;
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayNameFr() {
        return displayNameFr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }
}
