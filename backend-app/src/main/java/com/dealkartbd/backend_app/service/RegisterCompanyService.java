package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterCompanyRequest;
import com.dealkartbd.backend_app.dto.RegisterCompanyResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.entity.UserType;
import com.dealkartbd.backend_app.exception.InvalidEmailException;
import com.dealkartbd.backend_app.exception.InvalidFieldException;
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
import static com.dealkartbd.backend_app.exception.ErrorMessages.ROLE_IS_NOT_FOUND;
import static com.dealkartbd.backend_app.exception.ErrorMessages.USER_ALREADY_EXISTS;

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
        // 1. Check if email already exists first (user uniqueness)
        if (userRepository.existsByEmail(request.adminEmail().trim())) {
            throw new UserAlreadyExistsException(USER_ALREADY_EXISTS.getMessage());
        }

        // 2. Validate required fields (company and admin info)
        if (request.companyName() == null || request.companyName().trim().isEmpty()) {
            throw new InvalidFieldException("Company name is required.");
        }
        if (request.registrationNumber() == null || request.registrationNumber().trim().isEmpty()) {
            throw new InvalidFieldException("Company registration number is required.");
        }
        if (request.adminName() == null || request.adminName().trim().isEmpty()) {
            throw new InvalidFieldException("Admin name is required.");
        }
        if (request.adminPassword() == null || request.adminPassword().trim().isEmpty()) {
            throw new InvalidFieldException("Admin password is required.");
        }
        if (request.adminPhone() == null || request.adminPhone().trim().isEmpty()) {
            throw new InvalidFieldException("Admin phone is required.");
        }

        // 3. Validate company website if provided
        if (request.website() != null && !request.website().trim().isEmpty()) {
            if (!request.website().matches("^(https?://)?[\\w.-]+(\\.[\\w.-]+)+[/#?]?.*$")) {
                throw new InvalidFieldException("Invalid company website URL.");
            }
        }

        // 4. Check for duplicate company by name or registration number
        if (companyRepository.existsByName(request.companyName().trim())) {
            throw new UserAlreadyExistsException("A company with this name already exists.");
        }
        if (companyRepository.existsByRegistrationNumber(request.registrationNumber().trim())) {
            throw new UserAlreadyExistsException("A company with this registration number already exists.");
        }

        // 5. Validate email format and check if it's active (strict validation)
        String emailValidationError = emailValidationService.validateEmailWithDetails(request.adminEmail().trim());
        if (emailValidationError != null) {
            throw new InvalidEmailException(emailValidationError);
        }

        try {
            // 6. Prepare and sanitize company entity
            Company company = Company.builder().build();
            company.setName(request.companyName().trim());
            company.setRegistrationNumber(request.registrationNumber().trim());
            company.setCompanyType(request.companyType() != null ? request.companyType().trim() : null);
            company.setIndustry(request.industry() != null ? request.industry().trim() : null);
            company.setWebsite(request.website() != null ? request.website().trim() : null);
            company.setCompanyPhone(request.companyPhone() != null ? request.companyPhone().trim() : null);
            company.setTaxId(request.taxId() != null ? request.taxId().trim() : null);
            company.setAddressStreet(request.addressStreet() != null ? request.addressStreet().trim() : null);
            company.setAddressCity(request.addressCity() != null ? request.addressCity().trim() : null);
            company.setAddressState(request.addressState() != null ? request.addressState().trim() : null);
            company.setAddressPostalCode(request.addressPostalCode() != null ? request.addressPostalCode().trim() : null);
            company.setAddressCountry(request.addressCountry() != null ? request.addressCountry().trim() : null);

            Company savedCompany = companyRepository.save(company);

            // 7. Get COMPANY_OWNER role for admin user
            Role adminRole = roleRepository.findByName(Role.RoleName.COMPANY_OWNER)
                    .orElseThrow(() -> new RoleIsNotFoundException(ROLE_IS_NOT_FOUND.getMessage()));

            // 8. Prepare and sanitize admin user entity
            User user = User.builder().build();
            user.setFullName(request.adminName().trim());
            user.setEmail(request.adminEmail().trim());
            user.setPassword(passwordEncoder.encode(request.adminPassword().trim()));
            user.setPhone(request.adminPhone().trim());
            user.setPosition(request.adminRole() != null ? request.adminRole().trim() : null);
            user.setCompany(savedCompany);
            user.setRoles(Set.of(adminRole));
            user.setUserType(UserType.COMPANY_OWNER);

            // 9. Create user and send email confirmation
            userService.createUserWithEmailConfirmation(user);

            // 10. Log registration success
            log.info("Company registered successfully: {} with admin email: {}",
                    request.companyName(), request.adminEmail());

            return new RegisterCompanyResponse(
                    REGISTRATION_SUCCESS_MSG +
                            " If you don't receive the email, you can request a new confirmation email."
            );

        } catch (Exception e) {
            // All exceptions are handled globally, just log here
            log.error("Error during company registration for email: {} - {}",
                    request.adminEmail(), e.getMessage());
            throw e;
        }
    }
}
