package com.dealkartbd.backend_app.exception;

public record ApiErrorResponse(
    String message,
    int code,
    String timestamp
) {}
