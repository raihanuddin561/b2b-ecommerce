package com.dealkartbd.backend_app.dto.login;

import java.util.List;

public record LoginResponse(
    String token,
    List<String> roles,
    String userType
) {}
