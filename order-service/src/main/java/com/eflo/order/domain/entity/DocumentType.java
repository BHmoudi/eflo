package com.eflo.order.domain.entity;

public enum DocumentType {
    INVOICE("Invoice"),
    CONTRACT("Contract"),
    INSURANCE("Insurance"),
    REGISTRATION("Registration"),
    PHOTO("Photo"),
    ID_CARD("ID Card"),
    PROOF_OF_ADDRESS("Proof of Address"),
    OTHER("Other");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
