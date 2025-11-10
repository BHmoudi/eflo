package com.eflo.user.exception;

/**
 * Exception thrown when an invalid user-business unit or role assignment is attempted.
 */
public class InvalidAssignmentException extends RuntimeException {

    private final String userId;
    private final String targetId;

    public InvalidAssignmentException(String message) {
        super(message);
        this.userId = null;
        this.targetId = null;
    }

    public InvalidAssignmentException(String userId, String targetId, String message) {
        super(message);
        this.userId = userId;
        this.targetId = targetId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTargetId() {
        return targetId;
    }
}
