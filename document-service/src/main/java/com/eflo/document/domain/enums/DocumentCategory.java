package com.eflo.document.domain.enums;

public enum DocumentCategory {
    IDENTITY("Identity Documents"),
    FINANCIAL("Financial Documents"),
    LEGAL("Legal Documents"),
    CONTRACT("Contracts"),
    TECHNICAL("Technical Documents"),
    ADMINISTRATIVE("Administrative Documents"),
    OTHER("Other Documents");

    private final String displayName;

    DocumentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
