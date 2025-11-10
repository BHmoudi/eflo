package com.eflo.document.domain.enums;

public enum AccessAction {
    UPLOAD("Upload document"),
    DOWNLOAD("Download document"),
    VIEW("View document"),
    UPDATE("Update document"),
    DELETE("Delete document"),
    VALIDATE("Validate document"),
    REJECT("Reject document"),
    ARCHIVE("Archive document"),
    RESTORE("Restore document"),
    SHARE("Share document"),
    EXPORT("Export document");

    private final String description;

    AccessAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isModifyingAction() {
        return this == UPLOAD || this == UPDATE || this == DELETE ||
               this == VALIDATE || this == REJECT || this == ARCHIVE || this == RESTORE;
    }

    public boolean isReadOnlyAction() {
        return this == DOWNLOAD || this == VIEW || this == EXPORT;
    }
}
