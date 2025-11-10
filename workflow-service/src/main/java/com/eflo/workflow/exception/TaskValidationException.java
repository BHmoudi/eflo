package com.eflo.workflow.exception;

/**
 * Exception thrown when task validation fails
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class TaskValidationException extends WorkflowException {

    public TaskValidationException(String message) {
        super(message, "TASK_VALIDATION_ERROR");
    }

    public TaskValidationException(String message, Throwable cause) {
        super(message, "TASK_VALIDATION_ERROR", cause);
    }
}
