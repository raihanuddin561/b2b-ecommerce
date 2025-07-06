package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.entity.User;

public interface UserService {
    UserDto getUserByEmail(String email);
    void updateLastLogin(String email);
    void incrementFailedLoginAttempts(String email);
    void unlockAccount(String email);
    void activateAccount(String email);
    void suspendAccount(String email);
    void createUserWithEmailConfirmation(User user);
}
