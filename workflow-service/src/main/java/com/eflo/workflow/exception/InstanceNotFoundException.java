package com.eflo.workflow.exception;

/**
 * Exception thrown when a workflow instance is not found
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class InstanceNotFoundException extends WorkflowException {

    public InstanceNotFoundException(Long instanceId) {
        super("Workflow instance not found with ID: " + instanceId, "INSTANCE_NOT_FOUND");
    }

    public InstanceNotFoundException(String message) {
        super(message, "INSTANCE_NOT_FOUND");
    }
}
