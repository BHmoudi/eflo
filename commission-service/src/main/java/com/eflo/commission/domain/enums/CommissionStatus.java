package com.eflo.commission.domain.enums;

public enum CommissionStatus {
    PENDING_CALCULATION("Pending Calculation"),
    CALCULATED("Calculated"),
    VALIDATED("Validated"),
    PENDING_PAYMENT("Pending Payment"),
    PAID("Paid"),
    CANCELLED("Cancelled"),
    ADJUSTED("Adjusted"),
    ERROR("Error");

    private final String description;

    CommissionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
