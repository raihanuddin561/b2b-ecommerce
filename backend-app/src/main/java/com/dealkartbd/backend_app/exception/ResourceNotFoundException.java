package com.dealkartbd.backend_app.exception;

/**
 * Exception thrown when a requested resource path is not found
 * This is used for more specific error handling than generic 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(String.format("%s not found with identifier: %s", resourceType, identifier));
    }

    public ResourceNotFoundException(String resourceType, String field, String value) {
        super(String.format("%s not found with %s: %s", resourceType, field, value));
    }
}
