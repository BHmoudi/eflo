package com.eflo.workflow.domain.enums;

/**
 * Workflow Instance Status
 *
 * Defines all possible states of a workflow instance throughout its lifecycle.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum InstanceStatus {

    /**
     * Instance has been created but not yet started
     */
    CREATED("Instance created and ready to start"),

    /**
     * Instance is actively running and processing
     */
    RUNNING("Instance is actively running"),

    /**
     * Instance has been temporarily paused
     */
    PAUSED("Instance temporarily paused"),

    /**
     * Instance has completed successfully
     */
    COMPLETED("Instance completed successfully"),

    /**
     * Instance has been cancelled by user or system
     */
    CANCELLED("Instance cancelled"),

    /**
     * Instance encountered an error and cannot proceed
     */
    ERROR("Instance in error state");

    private final String description;

    InstanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if instance is in a terminal state
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED || this == ERROR;
    }

    /**
     * Check if instance can be transitioned
     */
    public boolean canTransition() {
        return this == RUNNING;
    }
}
