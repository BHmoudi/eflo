package com.eflo.workflow.exception;

/**
 * Exception thrown when a workflow role is not found
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class RoleNotFoundException extends WorkflowException {

    public RoleNotFoundException(Long roleId) {
        super("Workflow role not found with ID: " + roleId, "ROLE_NOT_FOUND");
    }

    public RoleNotFoundException(String roleCode) {
        super("Workflow role not found with code: " + roleCode, "ROLE_NOT_FOUND");
    }
}
