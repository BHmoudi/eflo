package com.eflo.document.domain.enums;

public enum VirusScanStatus {
    PENDING("Scan pending"),
    CLEAN("No threats detected"),
    INFECTED("Virus or malware detected"),
    FAILED("Scan failed"),
    SKIPPED("Scan skipped");

    private final String description;

    VirusScanStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSafe() {
        return this == CLEAN || this == SKIPPED;
    }

    public boolean requiresAction() {
        return this == INFECTED || this == FAILED;
    }
}
