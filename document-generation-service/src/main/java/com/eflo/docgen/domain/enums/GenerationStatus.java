package com.eflo.docgen.domain.enums;

/**
 * Document Generation Status
 */
public enum GenerationStatus {
    PENDING("Generation queued"),
    PROCESSING("Generation in progress"),
    COMPLETED("Generation completed successfully"),
    FAILED("Generation failed"),
    CANCELLED("Generation cancelled");

    private final String description;

    GenerationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}
