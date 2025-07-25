package com.dealkartbd.backend_app.exception;

/**
 * Exception thrown when a service is temporarily unavailable
 * Used for maintenance, overload, or wrong path scenarios that should return 503
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Factory method for wrong path scenarios
     */
    public static ServiceUnavailableException forWrongPath(String path) {
        return new ServiceUnavailableException(
            String.format("The requested path '%s' is not available. Please check the API documentation for valid endpoints.", path)
        );
    }
}
