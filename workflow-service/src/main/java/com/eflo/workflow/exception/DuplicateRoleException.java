package com.eflo.workflow.exception;

/**
 * Exception thrown when attempting to create a role with a duplicate code
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class DuplicateRoleException extends WorkflowException {

    public DuplicateRoleException(String roleCode) {
        super("Role with code '" + roleCode + "' already exists", "DUPLICATE_ROLE");
    }
}
