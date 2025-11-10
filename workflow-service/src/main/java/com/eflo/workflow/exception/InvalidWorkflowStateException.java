package com.eflo.workflow.exception;

/**
 * Exception thrown when a workflow operation is attempted in an invalid state
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class InvalidWorkflowStateException extends WorkflowException {

    public InvalidWorkflowStateException(String currentState, String operation) {
        super(String.format("Cannot perform operation '%s' in current state '%s'", operation, currentState),
              "INVALID_WORKFLOW_STATE");
    }

    public InvalidWorkflowStateException(String message) {
        super(message, "INVALID_WORKFLOW_STATE");
    }
}
