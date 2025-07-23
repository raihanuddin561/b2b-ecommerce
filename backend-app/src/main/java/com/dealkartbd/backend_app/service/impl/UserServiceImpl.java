package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.repository.UserRepository;
import com.dealkartbd.backend_app.service.EmailConfirmationService;
import com.dealkartbd.backend_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailConfirmationService emailConfirmationService;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return mapToDto(user);
    }

    @Override
    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        // Lock account after 5 failed attempts for 30 minutes
        if (attempts >= 5) {
            user.setLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(30));
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setLocked(false);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void suspendAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        user.setAccountStatus(AccountStatus.SUSPENDED);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createUserWithEmailConfirmation(User user) {
        // Set initial status
        user.setAccountStatus(AccountStatus.PENDING_ACTIVATION);
        user.setEmailVerified(false);
        User savedUser = userRepository.save(user);

        // Send confirmation email
        emailConfirmationService.createAndSendConfirmationToken(savedUser);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = modelMapper.map(user, UserDto.class);
        dto.setRoles(user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toSet()));
        dto.setCompanyName(user.getCompany() != null ? user.getCompany().getName() : null);
        // Build company address from individual fields
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            StringBuilder address = new StringBuilder();
            if (company.getAddressStreet() != null && !company.getAddressStreet().isEmpty()) address.append(company.getAddressStreet()).append(", ");
            if (company.getAddressCity() != null && !company.getAddressCity().isEmpty()) address.append(company.getAddressCity()).append(", ");
            if (company.getAddressState() != null && !company.getAddressState().isEmpty()) address.append(company.getAddressState()).append(", ");
            if (company.getAddressPostalCode() != null && !company.getAddressPostalCode().isEmpty()) address.append(company.getAddressPostalCode()).append(", ");
            if (company.getAddressCountry() != null && !company.getAddressCountry().isEmpty()) address.append(company.getAddressCountry());
            String fullAddress = address.toString().replaceAll(", $", ""); // Remove trailing comma
            dto.setCompanyAddress(fullAddress.isEmpty() ? null : fullAddress);
        } else {
            dto.setCompanyAddress(null);
        }
        return dto;
    }
}
