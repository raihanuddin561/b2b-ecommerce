package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterBuyerRequest;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.exception.InvalidFieldException;
import com.dealkartbd.backend_app.exception.RoleIsNotFoundException;
import com.dealkartbd.backend_app.exception.UserAlreadyExistsException;
import com.dealkartbd.backend_app.repository.RoleRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterBuyerServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @InjectMocks
    private RegisterBuyerService registerBuyerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerBuyer_success() {
        RegisterBuyerRequest request = new RegisterBuyerRequest(
                "Test Buyer", "buyer@example.com", "password123", null, null, null, null, null, null
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.CUSTOMER)).thenReturn(Optional.of(new Role()));
        when(passwordEncoder.encode(any())).thenReturn("encoded");

        assertDoesNotThrow(() -> registerBuyerService.registerBuyer(request));
        verify(userService).createUserWithEmailConfirmation(any(User.class));
    }

    @Test
    void registerBuyer_duplicateEmail() {
        RegisterBuyerRequest request = new RegisterBuyerRequest(
                "Test Buyer", "buyer@example.com", "password123", null, null, null, null, null, null
        );
        when(userRepository.existsByEmail(any())).thenReturn(true);
        assertThrows(UserAlreadyExistsException.class, () -> registerBuyerService.registerBuyer(request));
    }

    @Test
    void registerBuyer_missingFullName() {
        RegisterBuyerRequest request = new RegisterBuyerRequest(
                null, "buyer@example.com", "password123", null, null, null, null, null, null
        );
        assertThrows(InvalidFieldException.class, () -> registerBuyerService.registerBuyer(request));
    }

    @Test
    void registerBuyer_missingRole() {
        RegisterBuyerRequest request = new RegisterBuyerRequest(
                "Test Buyer", "buyer@example.com", "password123", null, null, null, null, null, null
        );
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName(Role.RoleName.CUSTOMER)).thenReturn(Optional.empty());
        assertThrows(RoleIsNotFoundException.class, () -> registerBuyerService.registerBuyer(request));
    }
}
