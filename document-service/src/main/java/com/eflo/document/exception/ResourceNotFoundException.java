package com.eflo.document.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Used throughout the application to indicate missing entities.
 *
 * @author Document Service
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a ResourceNotFoundException for a specific resource type and ID.
     *
     * @param resourceType the type of resource
     * @param resourceId the ID of the resource
     */
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s with ID '%s' not found", resourceType, resourceId));
    }
}
