package com.eflo.workflow.exception;

/**
 * Exception thrown when a workflow task is not found
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class TaskNotFoundException extends WorkflowException {

    public TaskNotFoundException(Long taskId) {
        super("Workflow task not found with ID: " + taskId, "TASK_NOT_FOUND");
    }

    public TaskNotFoundException(String message) {
        super(message, "TASK_NOT_FOUND");
    }
}
