package com.dealkartbd.backend_app.repository;

import com.dealkartbd.backend_app.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByRegistrationNumber(String registrationNumber);
}
