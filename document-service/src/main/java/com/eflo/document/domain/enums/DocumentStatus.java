package com.eflo.document.domain.enums;

public enum DocumentStatus {
    PENDING_UPLOAD("Document placeholder created, awaiting upload"),
    UPLOADED("Document uploaded, pending validation"),
    PENDING("Document pending validation"),
    VALIDATED("Document validated and approved"),
    REJECTED("Document rejected"),
    EXPIRED("Document has expired"),
    ARCHIVED("Document archived"),
    DELETED("Document soft deleted");

    private final String description;

    DocumentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == PENDING_UPLOAD || this == UPLOADED || this == PENDING || this == VALIDATED;
    }

    public boolean isTerminal() {
        return this == DELETED || this == ARCHIVED;
    }
}
