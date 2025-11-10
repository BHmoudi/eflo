package com.eflo.user.exception;

/**
 * Exception thrown when attempting to create a user that already exists.
 */
public class DuplicateUserException extends RuntimeException {

    private final String identifier;

    public DuplicateUserException(String identifier) {
        super(String.format("User already exists with identifier: %s", identifier));
        this.identifier = identifier;
    }

    public DuplicateUserException(String identifier, String message) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
