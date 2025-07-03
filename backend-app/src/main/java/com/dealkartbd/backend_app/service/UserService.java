package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.UserDto;

public interface UserService {
    public UserDto getUserByEmail(String email);
}
