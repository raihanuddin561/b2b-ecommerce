package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterCompanyRequest;
import com.dealkartbd.backend_app.dto.RegisterCompanyResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.entity.UserType;
import com.dealkartbd.backend_app.exception.InvalidEmailException;
import com.dealkartbd.backend_app.exception.RoleIsNotFoundException;
import com.dealkartbd.backend_app.exception.UserAlreadyExistsException;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.RoleRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.dealkartbd.backend_app.constans.AppConstants.REGISTRATION_SUCCESS_MSG;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterCompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final EmailValidationService emailValidationService;

    @Transactional
    public RegisterCompanyResponse registerCompany(RegisterCompanyRequest request) {
        // Check if email already exists first
        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        // Validate email format and check if it's active (strict validation)
        String emailValidationError = emailValidationService.validateEmailWithDetails(request.adminEmail());
        if (emailValidationError != null) {
            throw new InvalidEmailException(emailValidationError);
        }

        try {
            Company company = new Company();
            company.setName(request.companyName());
            company.setAddress(request.address());
            company.setRegistrationNumber(request.registrationNumber());

            Company savedCompany = companyRepository.save(company);

            // Get VENDOR role
            Role adminRole = roleRepository.findByName(Role.RoleName.VENDOR)
                    .orElseThrow(() -> new RoleIsNotFoundException("VENDOR role not found"));

            // Create admin user
            User user = new User();
            user.setFullName(request.adminName());
            user.setEmail(request.adminEmail());
            user.setPassword(passwordEncoder.encode(request.adminPassword()));
            user.setCompany(savedCompany);
            user.setRoles(Set.of(adminRole));
            user.setUserType(UserType.COMPANY_OWNER);

            // Use new method that sends email confirmation
            // This method handles email failures gracefully
            userService.createUserWithEmailConfirmation(user);

            // Registration successful regardless of email status
            log.info("Company registered successfully: {} with admin email: {}",
                    request.companyName(), request.adminEmail());

            return new RegisterCompanyResponse(
                    REGISTRATION_SUCCESS_MSG + " Please check your email to confirm your account. " +
                            "If you don't receive the email, you can request a new confirmation email."
            );

        } catch (Exception e) {
            log.error("Error during company registration for email: {} - {}",
                    request.adminEmail(), e.getMessage());
            throw e; // Re-throw to be handled by global exception handler
        }
    }
}
