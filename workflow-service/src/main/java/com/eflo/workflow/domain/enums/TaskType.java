package com.eflo.workflow.domain.enums;

/**
 * Workflow Task Type
 *
 * Defines the different types of tasks that can be performed in a workflow.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum TaskType {

    /**
     * Document upload task
     */
    DOCUMENT("Document upload task"),

    /**
     * Action task requiring user data input
     */
    ACTION("User action/data input task"),

    /**
     * Control task for approval or validation
     */
    CONTROL("Control/approval task"),

    // Legacy types
    /**
     * Data input task requiring user to enter information
     */
    DATA_INPUT("User data input task"),

    /**
     * Document upload task (legacy)
     */
    DOCUMENT_UPLOAD("Document upload task"),

    /**
     * Approval task requiring decision
     */
    APPROVAL("Approval/decision task"),

    /**
     * Validation task to verify data/documents
     */
    VALIDATION("Validation task"),

    /**
     * Notification task to inform users
     */
    NOTIFICATION("Notification task"),

    /**
     * Integration task calling external services
     */
    INTEGRATION("External service integration task"),

    /**
     * Manual task requiring human intervention
     */
    MANUAL("Manual processing task"),

    /**
     * Automated task executed by system
     */
    AUTOMATED("Automated system task");

    private final String description;

    TaskType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if task requires human interaction
     */
    public boolean requiresHuman() {
        return this != AUTOMATED && this != INTEGRATION;
    }
}
