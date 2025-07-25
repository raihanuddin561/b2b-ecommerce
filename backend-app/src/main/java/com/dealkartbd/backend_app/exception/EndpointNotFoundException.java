package com.dealkartbd.backend_app.exception;

/**
 * Exception thrown when a requested endpoint/path does not exist
 * Used for handling wrong API paths professionally
 */
public class EndpointNotFoundException extends RuntimeException {

    public EndpointNotFoundException(String message) {
        super(message);
    }

    public EndpointNotFoundException(String method, String path) {
        super(String.format("Endpoint not found: %s %s", method, path));
    }

    public EndpointNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
