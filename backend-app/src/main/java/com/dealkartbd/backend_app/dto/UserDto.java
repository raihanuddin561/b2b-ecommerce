package com.dealkartbd.backend_app.dto;

import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Set<String> roles;
    private String address;
    private String companyName;
    private String companyAddress;
    private UserType userType;
    private AccountStatus accountStatus;
    private boolean emailVerified;
    private boolean locked;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private int failedLoginAttempts;
    private LocalDateTime lockedUntil;
}
