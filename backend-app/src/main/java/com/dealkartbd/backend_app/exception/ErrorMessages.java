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
    USER_ALREADY_EXISTS("User already exists"),
    ROLE_IS_NOT_FOUND("Role is not found"),
    EMAIL_ALREADY_EXISTS("Email already exists"),
    BAD_CREDENTIALS_MSG("Bad credentials"),
    EMAIL_CONFIRMATION_FAILED("Email confirmation failed"),
    EMAIL_SENDING_FAILED("Failed to send email"),
    PASSWORD_TOO_WEAK("Password is too weak"),
    PASSWORDS_DO_NOT_MATCH("Passwords do not match"),
    ACCOUNT_CREATION_FAILED("Account creation failed"),
    ACCOUNT_UPDATE_FAILED("Account update failed"),
    ACCOUNT_DELETION_FAILED("Account deletion failed"),
    ACCOUNT_ACTIVATION_FAILED("Account activation failed"),
    BAD_REQUEST("Bad request"),
    TOKEN_IS_NOT_VALID("Token is not valid"),
    TOKEN_IS_EXPIRED("Token is expired"),
    INVALID_OR_EXPIRATION_TOKEN("Invalid or expired confirmation token."),
    CONFIRMATION_TOKEN_EXPIRED("Confirmation token has expired"),
    CONFIRMATION_TOKEN_HAS_ALREADY_USED("Confirmation token has already been used"),
    TOKEN_VALIDATION_FAILED("Token validation failed"),
    ACCOUNT_IS_DISABLED("Account is disabled"),
    ACCOUNT_IS_LOCKED("Account is locked"),
    ACCOUNT_IS_NOT_ACTIVE("Account is not active"),
    ACCOUNT_IS_DEACTIVATED("Account is deactivated"),
    ACCOUNT_IS_SUSPENDED("Account is suspended"),

    ACCOUNT_IS_BANNED("Account is banned");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
