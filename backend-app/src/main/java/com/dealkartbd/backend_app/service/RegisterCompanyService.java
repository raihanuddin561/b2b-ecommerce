package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.dto.RegisterCompanyRequest;
import com.dealkartbd.backend_app.dto.RegisterCompanyResponse;
import com.dealkartbd.backend_app.entity.Company;
import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.repository.CompanyRepository;
import com.dealkartbd.backend_app.repository.RoleRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterCompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterCompanyResponse registerCompany(RegisterCompanyRequest request) {
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.adminEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // 2. Create company
        Company company = new Company();
        company.setName(request.companyName());
        company.setAddress(request.address());
        company.setRegistrationNumber(request.registrationNumber());

        Company savedCompany = companyRepository.save(company);

        // 3. Get ADMIN role
        Role adminRole = roleRepository.findByName(Role.RoleName.VENDOR)
                .orElseThrow(() -> new RuntimeException("VENDOR role not found"));

        // 4. Create admin user
        User user = new User();
        user.setFullName(request.adminName());
        user.setEmail(request.adminEmail());
        user.setPassword(passwordEncoder.encode(request.adminPassword()));
        user.setCompany(savedCompany);
        user.setRoles(Set.of(adminRole));

        userRepository.save(user);

        return new RegisterCompanyResponse("Company registered successfully!");
    }
}
