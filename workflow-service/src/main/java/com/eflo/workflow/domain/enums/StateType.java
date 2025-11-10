package com.eflo.workflow.domain.enums;

/**
 * Workflow State Type
 *
 * Defines the different types of states that can exist in a workflow process.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public enum StateType {

    /**
     * Initial state of the workflow - entry point
     */
    START("Start state - workflow entry point"),

    /**
     * Standard processing state
     */
    NORMAL("Normal processing state"),

    /**
     * Decision point state with conditional transitions
     */
    DECISION("Decision state with conditional routing"),

    /**
     * Terminal state indicating successful completion
     */
    FINAL("Final state - successful completion"),

    /**
     * Error state for exception handling
     */
    ERROR("Error state for exception handling");

    private final String description;

    StateType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if state is a terminal state
     */
    public boolean isTerminal() {
        return this == FINAL || this == ERROR;
    }
}
