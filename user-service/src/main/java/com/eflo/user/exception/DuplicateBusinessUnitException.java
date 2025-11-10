package com.eflo.user.exception;

/**
 * Exception thrown when attempting to create a business unit that already exists.
 */
public class DuplicateBusinessUnitException extends RuntimeException {

    private final String identifier;

    public DuplicateBusinessUnitException(String identifier) {
        super(String.format("Business unit already exists with identifier: %s", identifier));
        this.identifier = identifier;
    }

    public DuplicateBusinessUnitException(String identifier, String message) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
