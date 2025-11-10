package com.eflo.commission.domain.enums;

public enum CalculationMethod {
    PERCENTAGE_MARGIN("Percentage of Net Margin"),
    PERCENTAGE_REVENUE("Percentage of Revenue"),
    TIERED("Tiered Commission"),
    FIXED_AMOUNT("Fixed Amount"),
    HYBRID("Hybrid (Multiple Methods)");

    private final String description;

    CalculationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
