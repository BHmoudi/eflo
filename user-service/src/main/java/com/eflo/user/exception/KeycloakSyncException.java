package com.eflo.user.exception;

/**
 * Exception thrown when synchronization with Keycloak fails.
 */
public class KeycloakSyncException extends RuntimeException {

    private final String operation;
    private final String userId;

    public KeycloakSyncException(String operation, String userId, String message) {
        super(String.format("Keycloak sync failed for operation '%s' on user %s: %s", operation, userId, message));
        this.operation = operation;
        this.userId = userId;
    }

    public KeycloakSyncException(String operation, String userId, String message, Throwable cause) {
        super(String.format("Keycloak sync failed for operation '%s' on user %s: %s", operation, userId, message), cause);
        this.operation = operation;
        this.userId = userId;
    }

    public String getOperation() {
        return operation;
    }

    public String getUserId() {
        return userId;
    }
}
