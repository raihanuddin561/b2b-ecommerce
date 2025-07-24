package com.dealkartbd.backend_app.exception;

/**
 * Exception thrown when client makes request to invalid or malformed paths
 * This provides better user experience than generic 400 errors
 */
public class InvalidPathException extends RuntimeException {

    public InvalidPathException(String message) {
        super(message);
    }

    public InvalidPathException(String path, String reason) {
        super(String.format("Invalid path '%s': %s", path, reason));
    }
}
