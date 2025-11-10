package com.eflo.user.exception;

/**
 * Exception thrown when a user attempts an unauthorized action.
 */
public class UnauthorizedException extends RuntimeException {

    private final String action;
    private final String resource;

    public UnauthorizedException(String message) {
        super(message);
        this.action = null;
        this.resource = null;
    }

    public UnauthorizedException(String action, String resource) {
        super(String.format("Unauthorized to perform action '%s' on resource '%s'", action, resource));
        this.action = action;
        this.resource = resource;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }
}
