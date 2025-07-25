package com.dealkartbd.backend_app.dto.login;

import java.time.LocalDateTime;
import java.util.List;

public record LoginResponse(
    String token,
    Long userId,
    String name,                    // Display name only - safe to expose
    List<String> roles,
    String userType,
    LocalDateTime expiresAt,
    boolean emailVerified,
    String profileInitials        // First letters of name for avatar display
) {}
