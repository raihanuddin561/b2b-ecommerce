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
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.dealkartbd.backend_app.exception.ErrorMessages.BAD_CREDENTIALS_MSG;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailConfirmationService emailConfirmationService;
    private final ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    private void configureModelMapper() {
        // Configure custom mapping for User to UserDto
        modelMapper.addMappings(new PropertyMap<User, UserDto>() {
            @Override
            protected void configure() {
                // Map fullName to name
                map().setName(source.getFullName());

                // Skip complex fields that need custom handling
                skip().setRoles(null);
                skip().setAddress(null);
                skip().setCompanyName(null);
                skip().setCompanyAddress(null);
            }
        });

        // Configure strict matching to avoid unexpected mappings
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));
        return mapToDto(user);
    }

    @Override
    @Transactional
    public void updateLastLogin(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));

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
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));
        user.setLocked(false);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void suspendAccount(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(BAD_CREDENTIALS_MSG.getMessage()));
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
        // Use ModelMapper for basic field mapping
        UserDto dto = modelMapper.map(user, UserDto.class);

        // Handle complex fields manually
        mapRoles(user, dto);
        mapAddresses(user, dto);
        mapCompanyInfo(user, dto);

        return dto;
    }

    private void mapRoles(User user, UserDto dto) {
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        }
    }

    private void mapAddresses(User user, UserDto dto) {
        // Build buyer personal address from individual fields
        dto.setAddress(buildAddress(
            user.getAddressStreet(),
            user.getAddressCity(),
            user.getAddressState(),
            user.getAddressPostalCode(),
            user.getAddressCountry()
        ));
    }

    private void mapCompanyInfo(User user, UserDto dto) {
        if (user.getCompany() != null) {
            Company company = user.getCompany();
            dto.setCompanyName(company.getName());

            // Build company address from individual fields
            dto.setCompanyAddress(buildAddress(
                company.getAddressStreet(),
                company.getAddressCity(),
                company.getAddressState(),
                company.getAddressPostalCode(),
                company.getAddressCountry()
            ));
        }
    }

    private String buildAddress(String street, String city, String state, String postalCode, String country) {
        StringBuilder address = new StringBuilder();

        if (street != null && !street.isEmpty())
            address.append(street).append(", ");
        if (city != null && !city.isEmpty())
            address.append(city).append(", ");
        if (state != null && !state.isEmpty())
            address.append(state).append(", ");
        if (postalCode != null && !postalCode.isEmpty())
            address.append(postalCode).append(", ");
        if (country != null && !country.isEmpty())
            address.append(country);

        String fullAddress = address.toString().replaceAll(", $", "");
        return fullAddress.isEmpty() ? null : fullAddress;
    }
}
