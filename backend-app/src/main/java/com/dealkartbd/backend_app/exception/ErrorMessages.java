package com.dealkartbd.backend_app.exception;

public enum ErrorMessages {
    INVALID_REQUEST("Invalid request"),
    INVALID_CREDENTIALS("Invalid credentials"),
    USER_NOT_FOUND("User not found"),
    COMPANY_NOT_FOUND("Company not found"),
    PRODUCT_NOT_FOUND("Product not found"),
    ORDER_NOT_FOUND("Order not found"),
    INTERNAL_SERVER_ERROR("Internal server error"),
    UNAUTHORIZED_ACCESS("Unauthorized access"),
    FORBIDDEN_ACCESS("Forbidden access"),
    RESOURCE_ALREADY_EXISTS("Resource already exists"),
    BAD_REQUEST("Bad request"),
    TOKEN_IS_NOT_VALID("Token is not valid"),
    TOKEN_IS_EXPIRED("Token is expired");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
