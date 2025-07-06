package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterCompanyRequest;
import com.dealkartbd.backend_app.dto.RegisterCompanyResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.entity.UserType;
import com.dealkartbd.backend_app.exception.RoleIsNotFoundException;
import com.dealkartbd.backend_app.exception.UserAlreadyExistsException;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.RoleRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

import static com.dealkartbd.backend_app.constans.AppConstants.REGISTRATION_SUCCESS_MSG;

@Service
@RequiredArgsConstructor
public class RegisterCompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public RegisterCompanyResponse registerCompany(RegisterCompanyRequest request) {
        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new UserAlreadyExistsException("Email already in use");
        }
        Company company = new Company();
        company.setName(request.companyName());
        company.setAddress(request.address());
        company.setRegistrationNumber(request.registrationNumber());

        Company savedCompany = companyRepository.save(company);

        // 3. Get ADMIN role
        Role adminRole = roleRepository.findByName(Role.RoleName.VENDOR)
                .orElseThrow(() -> new RoleIsNotFoundException("VENDOR role not found"));

        // 4. Create admin user
        User user = new User();
        user.setFullName(request.adminName());
        user.setEmail(request.adminEmail());
        user.setPassword(passwordEncoder.encode(request.adminPassword()));
        user.setCompany(savedCompany);
        user.setRoles(Set.of(adminRole));
        user.setUserType(UserType.COMPANY_OWNER);

        // Use new method that sends email confirmation
        userService.createUserWithEmailConfirmation(user);

        return new RegisterCompanyResponse(REGISTRATION_SUCCESS_MSG);
    }
}
