package com.eflo.user.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {

    private final String userId;

    public UserNotFoundException(String userId) {
        super(String.format("User not found with ID: %s", userId));
        this.userId = userId;
    }

    public UserNotFoundException(String userId, String message) {
        super(message);
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
