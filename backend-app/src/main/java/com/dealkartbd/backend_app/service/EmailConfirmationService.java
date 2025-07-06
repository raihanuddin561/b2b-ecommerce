package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.entity.EmailConfirmationToken;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.exception.EmailAlreadyConfirmedException;
import com.dealkartbd.backend_app.exception.EmailConfirmationException;
import com.dealkartbd.backend_app.exception.InvalidTokenException;
import com.dealkartbd.backend_app.repository.EmailConfirmationTokenRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {

    private final EmailConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createAndSendConfirmationToken(User user) {
        // Delete existing token if any
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        // Create new token
        String token = UUID.randomUUID().toString();
        EmailConfirmationToken confirmationToken = new EmailConfirmationToken(token, user);
        tokenRepository.save(confirmationToken);

        // Send email
        emailService.sendEmailConfirmation(user.getEmail(), token, user.getFullName());
    }

    @Transactional
    public void confirmEmail(String token) {
        EmailConfirmationToken confirmationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid confirmation token"));

        if (confirmationToken.isUsed()) {
            throw new InvalidTokenException("Confirmation token has already been used");
        }

        if (confirmationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Confirmation token has expired");
        }

        User user = confirmationToken.getUser();
        user.setEmailVerified(true);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        confirmationToken.setUsed(true);
        tokenRepository.save(confirmationToken);
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    @Transactional
    public void resendConfirmationEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if user is already verified
        if (user.isEmailVerified() && user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new EmailAlreadyConfirmedException("Email is already confirmed and account is active");
        }

        // Check if user account is in pending activation status
        if (user.getAccountStatus() != AccountStatus.PENDING_ACTIVATION) {
            throw new EmailConfirmationException("Account is not in pending activation status");
        }

        // Delete existing token and create new one
        createAndSendConfirmationToken(user);
    }
}
