package com.dealkartbd.backend_app.repository;

import com.dealkartbd.backend_app.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT c FROM Company c JOIN c.users u WHERE u.email = :email")
    Optional<Company> findByEmail(@Param("email") String email);

    /**
     * Checks if a company exists by its name.
     * @param name the company name
     * @return true if a company with the given name exists
     */
    boolean existsByName(String name);

    /**
     * Checks if a company exists by its registration number.
     * @param registrationNumber the registration number
     * @return true if a company with the given registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);
}
