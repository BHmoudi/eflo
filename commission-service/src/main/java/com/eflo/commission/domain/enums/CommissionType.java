package com.eflo.commission.domain.enums;

public enum CommissionType {
    VEHICLE("Vehicle Commission"),
    ACCESSORY("Accessory Commission"),
    SERVICE("Service Commission"),
    COMBINED("Combined Commission");

    private final String description;

    CommissionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
