package com.dealkartbd.backend_app.dto.login;

public record LoginResponse(
    String token,
    String role
) {}
