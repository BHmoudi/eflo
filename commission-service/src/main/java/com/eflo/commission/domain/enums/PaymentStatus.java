package com.eflo.commission.domain.enums;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    APPROVED("Approved"),
    COMPLETED("Completed"),
    REJECTED("Rejected"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
