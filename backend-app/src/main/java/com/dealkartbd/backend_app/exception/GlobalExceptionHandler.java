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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final static String ERROR_KEY = "error";

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
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleUserExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return buildResponse(errorMessage, HttpStatus.BAD_REQUEST, request.getRequestURI());
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request.getRequestURI());
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

    // ===== PATH AND RESOURCE RELATED EXCEPTIONS =====

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

    // Handle Spring's NoHandlerFoundException (when endpoint doesn't exist)
    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleNoHandlerFound(
            org.springframework.web.servlet.NoHandlerFoundException ex, HttpServletRequest request) {
        String message = String.format("Endpoint not found: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(message, HttpStatus.NOT_FOUND, request.getRequestURI());
    }

    // Handle HTTP method not allowed (wrong HTTP method for existing endpoint)
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMethodNotAllowed(
            org.springframework.web.HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Method %s not allowed for this endpoint. Supported methods: %s",
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        return buildResponse(message, HttpStatus.METHOD_NOT_ALLOWED, request.getRequestURI());
    }

    // Handle missing path variables
    @ExceptionHandler(org.springframework.web.bind.MissingPathVariableException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMissingPathVariable(
            org.springframework.web.bind.MissingPathVariableException ex, HttpServletRequest request) {
        String message = String.format("Required path variable '%s' is missing", ex.getVariableName());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle type mismatch in path variables (e.g., string where number expected)
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    // Handle unsupported media type
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleUnsupportedMediaType(
            org.springframework.web.HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("Media type '%s' not supported. Supported types: %s",
                ex.getContentType(), ex.getSupportedMediaTypes());
        return buildResponse(message, HttpStatus.UNSUPPORTED_MEDIA_TYPE, request.getRequestURI());
    }

    // Handle missing request body
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Required request body is missing or malformed";
        return buildResponse(message, HttpStatus.BAD_REQUEST, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, ApiErrorResponse>> handleGeneric(
            Exception ex, HttpServletRequest request) {
        return buildResponse("Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI());
    }

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
