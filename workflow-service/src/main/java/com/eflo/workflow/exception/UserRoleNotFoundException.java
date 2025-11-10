package com.eflo.workflow.exception;

/**
 * Exception thrown when a user role is not found
 *
 * @author Workflow Service
 * @version 1.0.0
 */
public class UserRoleNotFoundException extends WorkflowException {

    public UserRoleNotFoundException(Long userRoleId) {
        super("User role not found with ID: " + userRoleId, "USER_ROLE_NOT_FOUND");
    }

    public UserRoleNotFoundException(Long userId, Long roleId) {
        super("User role not found for user ID: " + userId + " and role ID: " + roleId, "USER_ROLE_NOT_FOUND");
    }
}
