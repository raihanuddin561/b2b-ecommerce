package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterBuyerRequest;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.entity.UserType;
import com.dealkartbd.backend_app.exception.InvalidFieldException;
import com.dealkartbd.backend_app.exception.RoleIsNotFoundException;
import com.dealkartbd.backend_app.exception.UserAlreadyExistsException;
import com.dealkartbd.backend_app.repository.RoleRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterBuyerService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    @Transactional
    public void registerBuyer(RegisterBuyerRequest request) {
        // Validate required fields
        if (request.fullName() == null || request.fullName().trim().isEmpty()) {
            throw new InvalidFieldException("Full name is required.");
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            throw new InvalidFieldException("Email is required.");
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            throw new InvalidFieldException("Password is required.");
        }
        // Check for duplicate email
        if (userRepository.existsByEmail(request.email().trim())) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }
        // Prepare user entity
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPassword(passwordEncoder.encode(request.password().trim()));
        user.setPhone(request.phone() != null ? request.phone().trim() : null);
        user.setUserType(UserType.BUYER);
        user.setAddressStreet(request.addressStreet() != null ? request.addressStreet().trim() : null);
        user.setAddressCity(request.addressCity() != null ? request.addressCity().trim() : null);
        user.setAddressState(request.addressState() != null ? request.addressState().trim() : null);
        user.setAddressPostalCode(request.addressPostalCode() != null ? request.addressPostalCode().trim() : null);
        user.setAddressCountry(request.addressCountry() != null ? request.addressCountry().trim() : null);
        // Assign CUSTOMER role (used for buyers)
        Role buyerRole = roleRepository.findByName(Role.RoleName.CUSTOMER)
                .orElseThrow(() -> new RoleIsNotFoundException("CUSTOMER role is not found."));
        user.setRoles(Set.of(buyerRole));
        // Create user (with email confirmation, if needed)
        userService.createUserWithEmailConfirmation(user);
        log.info("Buyer registered successfully: {}", request.email());
    }
}
