package com.eflo.workflow.exception;

/**
 * Exception thrown when an invalid workflow transition is attempted
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class InvalidTransitionException extends WorkflowException {

    public InvalidTransitionException(String fromState, String toState) {
        super(String.format("Invalid transition from state '%s' to state '%s'", fromState, toState),
              "INVALID_TRANSITION");
    }

    public InvalidTransitionException(String message) {
        super(message, "INVALID_TRANSITION");
    }
}
