package com.dealkartbd.backend_app.exception;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final static String ERROR_KEY = "error";

    // ===== AUTHENTICATION & AUTHORIZATION EXCEPTIONS =====

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleUsernameNotFound(
            UsernameNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildResponse("Invalid email or password", HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    @ExceptionHandler(value = {ExpiredJwtException.class, TokenInvalidException.class})
    public ResponseEntity<Map<String, ApiErrorResponse>> handleTokenInvalidException(RuntimeException ex,
                                                                                     HttpServletRequest request) {
        return buildResponse("Invalid authorization token", HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    // ===== BUSINESS LOGIC EXCEPTIONS =====

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleUserExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleInvalidEmail(
            InvalidEmailException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleEmailSending(
            EmailSendingException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request.getRequestURI());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleInvalidToken(
            InvalidTokenException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(EmailConfirmationException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleEmailConfirmation(
            EmailConfirmationException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(EmailAlreadyConfirmedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleEmailAlreadyConfirmed(
            EmailAlreadyConfirmedException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
    }

    @ExceptionHandler(RoleIsNotFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleRoleIsNotFound(
            RoleIsNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
    }

    @ExceptionHandler(InvalidFieldException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleInvalidField(
            InvalidFieldException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // ===== VALIDATION EXCEPTIONS =====

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponse(errorMessage, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // ===== PATH AND ENDPOINT RELATED EXCEPTIONS =====

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleInvalidPath(
            InvalidPathException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(EndpointNotFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleEndpointNotFound(
            EndpointNotFoundException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleServiceUnavailable(
            ServiceUnavailableException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request.getRequestURI());
    }

    // ===== SPRING FRAMEWORK PATH EXCEPTIONS =====

    // Handle Spring's NoHandlerFoundException (when endpoint doesn't exist)
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex, HttpServletRequest request) {
        String message = String.format("API endpoint not found: %s %s. Please check the URL and HTTP method.",
                ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(message, HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    // Handle Spring Boot 3.x NoResourceFoundException (replaces NoHandlerFoundException in some cases)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        String message = String.format("Resource not found: %s. Please verify the API endpoint URL.",
                ex.getResourcePath());
        return buildResponse(message, HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    // Handle HTTP method not allowed (wrong HTTP method for existing endpoint)
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMethodNotAllowed(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String supportedMethods = ex.getSupportedMethods() != null ?
                String.join(", ", ex.getSupportedMethods()) : "Not specified";
        String message = String.format("HTTP method '%s' is not supported for this endpoint. Supported methods: %s",
                ex.getMethod(), supportedMethods);
        return buildResponse(message, HttpStatus.METHOD_NOT_ALLOWED, request.getRequestURI());
    }

    // Handle missing path variables
    @ExceptionHandler(org.springframework.web.bind.MissingPathVariableException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMissingPathVariable(
            org.springframework.web.bind.MissingPathVariableException ex, HttpServletRequest request) {
        String message = String.format("Required path parameter '%s' is missing from the URL", ex.getVariableName());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle type mismatch in path variables (e.g., string where number expected)
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String expectedType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), expectedType);
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle missing request parameters
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMissingRequestParameter(
            org.springframework.web.bind.MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Required request parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle unsupported media type
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleUnsupportedMediaType(
            org.springframework.web.HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String supportedTypes = ex.getSupportedMediaTypes().toString();
        String message = String.format("Media type '%s' is not supported. Supported types: %s",
                ex.getContentType(), supportedTypes);
        return buildResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getRequestURI());
    }

    // Handle missing or malformed request body
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Required request body is missing or contains invalid JSON format";
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle cases where request body is required but not provided
    @ExceptionHandler(org.springframework.web.bind.ServletRequestBindingException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleServletRequestBinding(
            org.springframework.web.bind.ServletRequestBindingException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle conversion errors (e.g., invalid date format)
    @ExceptionHandler(org.springframework.core.convert.ConversionFailedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleConversionFailed(
            org.springframework.core.convert.ConversionFailedException ex, HttpServletRequest request) {
        String message = String.format("Invalid parameter format: %s", ex.getMessage());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // ===== SECURITY EXCEPTIONS =====

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse("Access denied. You don't have permission to access this resource.",
                HttpStatus.FORBIDDEN, request.getRequestURI());
    }

    @ExceptionHandler(org.springframework.security.authentication.InsufficientAuthenticationException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleInsufficientAuthentication(
            org.springframework.security.authentication.InsufficientAuthenticationException ex, HttpServletRequest request) {
        return buildResponse("Authentication required. Please provide valid credentials.",
                HttpStatus.UNAUTHORIZED, request.getRequestURI());
    }

    // ===== CATCH-ALL EXCEPTION HANDLER =====

    /**
     * This handles any unhandled exceptions, including wrong paths that don't match any other handler
     * Should be the last handler in the class
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleGeneric(
            Exception ex, HttpServletRequest request) {

        // Log the exception for debugging (you can add proper logging here)
        System.err.println("Unhandled exception: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());

        // Check if it's a path-related issue
        String requestURI = request.getRequestURI();
        if (requestURI != null && !requestURI.startsWith("/api/")) {
            return buildResponse("Invalid API path. All API endpoints must start with '/api/'",
                    HttpStatus.NOT_FOUND, requestURI);
        }

        return buildResponse("An unexpected error occurred. Please contact support if the problem persists.",
                HttpStatus.INTERNAL_SERVER_ERROR, request.getRequestURI());
    }

    // ===== UTILITY METHOD =====

    private ResponseEntity<Map<String, ApiErrorResponse>> buildResponse(String message, HttpStatus status,
                                                                        String path) {
        ApiErrorResponse response = new ApiErrorResponse(
                message,
                status.value(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        return new ResponseEntity<>(Map.of(ERROR_KEY, response), status);
    }
}
