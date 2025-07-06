package com.dealkartbd.backend_app.repository;

import com.dealkartbd.backend_app.entity.EmailConfirmationToken;
import com.dealkartbd.backend_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, Long> {
    Optional<EmailConfirmationToken> findByToken(String token);
    Optional<EmailConfirmationToken> findByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}

