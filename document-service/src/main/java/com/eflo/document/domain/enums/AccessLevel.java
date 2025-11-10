package com.eflo.document.domain.enums;

public enum AccessLevel {
    STANDARD("Standard access level"),
    RESTRICTED("Restricted access - requires specific permissions"),
    CONFIDENTIAL("Confidential - highly restricted access");

    private final String description;

    AccessLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getSecurityLevel() {
        switch (this) {
            case STANDARD: return 1;
            case RESTRICTED: return 2;
            case CONFIDENTIAL: return 3;
            default: return 0;
        }
    }
}
