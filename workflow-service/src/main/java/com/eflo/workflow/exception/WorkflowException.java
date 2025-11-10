package com.eflo.workflow.exception;

/**
 * Base exception for all workflow-related errors
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class WorkflowException extends RuntimeException {

    private final String errorCode;

    public WorkflowException(String message) {
        super(message);
        this.errorCode = "WORKFLOW_ERROR";
    }

    public WorkflowException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "WORKFLOW_ERROR";
    }

    public WorkflowException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
