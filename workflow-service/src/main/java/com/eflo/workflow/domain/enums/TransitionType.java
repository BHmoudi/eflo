package com.eflo.workflow.domain.enums;

/**
 * Workflow Transition Type
 *
 * Defines the different types of transitions between workflow states.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum TransitionType {

    /**
     * Standard forward transition
     */
    NORMAL("Normal forward transition"),

    /**
     * Escalation transition when deadlines are breached
     */
    ESCALATION("Escalation due to deadline breach"),

    /**
     * Rollback transition to previous state
     */
    ROLLBACK("Rollback to previous state"),

    /**
     * Cancellation transition
     */
    CANCEL("Cancellation transition");

    private final String description;

    TransitionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
