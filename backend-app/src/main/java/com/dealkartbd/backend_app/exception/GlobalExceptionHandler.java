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
            EmailAlreadyConfirmedException ex, HttpServletRequest request) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT, request.getRequestURI());
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
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                path
        );
        return new ResponseEntity<>(Map.of(ERROR_KEY, response), status);
    }
}
