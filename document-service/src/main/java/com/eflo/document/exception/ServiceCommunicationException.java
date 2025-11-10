package com.eflo.document.exception;

import lombok.Getter;

/**
 * Exception thrown when communication with external services fails.
 * Used by Feign clients to indicate service unavailability or communication errors.
 *
 * @author Document Service
 * @version 1.0
 */
@Getter
public class ServiceCommunicationException extends RuntimeException {

    private final int statusCode;
    private final String serviceName;

    /**
     * Constructs a new ServiceCommunicationException with the specified message and status code.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     */
    public ServiceCommunicationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.serviceName = extractServiceName(message);
    }

    /**
     * Constructs a new ServiceCommunicationException with the specified message, status code, and cause.
     *
     * @param message the detail message
     * @param statusCode the HTTP status code
     * @param cause the cause
     */
    public ServiceCommunicationException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.serviceName = extractServiceName(message);
    }

    /**
     * Constructs a new ServiceCommunicationException with the specified service name, message, and status code.
     *
     * @param serviceName the name of the service
     * @param message the detail message
     * @param statusCode the HTTP status code
     */
    public ServiceCommunicationException(String serviceName, String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.serviceName = serviceName;
    }

    private static String extractServiceName(String message) {
        if (message != null && message.contains(" ")) {
            String[] parts = message.split(" ");
            for (String part : parts) {
                if (part.endsWith("-service") || part.endsWith("Service")) {
                    return part;
                }
            }
        }
        return "unknown";
    }
}
