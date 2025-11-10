package com.eflo.workflow.exception;

/**
 * Exception thrown when a workflow process is not found
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class ProcessNotFoundException extends WorkflowException {

    public ProcessNotFoundException(Long processId) {
        super("Workflow process not found with ID: " + processId, "PROCESS_NOT_FOUND");
    }

    public ProcessNotFoundException(String processCode) {
        super("Workflow process not found with code: " + processCode, "PROCESS_NOT_FOUND");
    }
}
