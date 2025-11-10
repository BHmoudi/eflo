package com.eflo.workflow.domain.enums;

/**
 * Priority Level
 *
 * Defines priority levels for workflow instances and tasks.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum Priority {

    /**
     * Low priority - can be processed later
     */
    LOW(1, "Low priority"),

    /**
     * Normal priority - standard processing
     */
    NORMAL(2, "Normal priority"),

    /**
     * High priority - should be processed soon
     */
    HIGH(3, "High priority"),

    /**
     * Urgent priority - immediate attention required
     */
    URGENT(4, "Urgent - immediate attention");

    private final int level;
    private final String description;

    Priority(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}
