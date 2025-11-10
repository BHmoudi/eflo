package com.eflo.user.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 * This is an alias for DuplicateUserException for semantic clarity in different contexts.
 */
public class UserAlreadyExistsException extends RuntimeException {

    private final String email;
    private final String username;

    public UserAlreadyExistsException(String email) {
        super(String.format("User already exists with email: %s", email));
        this.email = email;
        this.username = null;
    }

    public UserAlreadyExistsException(String email, String username) {
        super(String.format("User already exists with email: %s or username: %s", email, username));
        this.email = email;
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }
}
