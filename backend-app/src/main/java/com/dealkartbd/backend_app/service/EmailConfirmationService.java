package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.entity.EmailConfirmationToken;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.exception.EmailAlreadyConfirmedException;
import com.dealkartbd.backend_app.exception.EmailConfirmationException;
import com.dealkartbd.backend_app.exception.EmailSendingException;
import com.dealkartbd.backend_app.exception.InvalidTokenException;
import com.dealkartbd.backend_app.repository.EmailConfirmationTokenRepository;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationService {

    private final EmailConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createAndSendConfirmationToken(User user) {
        try {
            // Delete existing token if any
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            // Create new token
            String token = UUID.randomUUID().toString();
            EmailConfirmationToken confirmationToken = new EmailConfirmationToken(token, user);
            tokenRepository.save(confirmationToken);

            // Send email - if this fails, we still keep the user but mark email as failed
            emailService.sendEmailConfirmation(user.getEmail(), token, user.getFullName());

        } catch (EmailSendingException e) {
            log.error("Failed to send confirmation email for user: {} - {}", user.getEmail(), e.getMessage());
            // Don't throw exception here - allow user registration to complete
            // User can request resend later
        } catch (Exception e) {
            log.error("Unexpected error during email confirmation setup for user: {} - {}",
                user.getEmail(), e.getMessage());
            // Log but don't fail the registration process
        }
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

        log.info("Email successfully confirmed for user: {}", user.getEmail());
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

        // Create and send new confirmation token
        // This time we do want to throw exception if email fails since user explicitly requested it
        try {
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

            String token = UUID.randomUUID().toString();
            EmailConfirmationToken confirmationToken = new EmailConfirmationToken(token, user);
            tokenRepository.save(confirmationToken);

            emailService.sendEmailConfirmation(user.getEmail(), token, user.getFullName());
            log.info("Confirmation email resent successfully to: {}", email);

        } catch (EmailSendingException e) {
            log.error("Failed to resend confirmation email to: {} - {}", email, e.getMessage());
            throw new EmailSendingException("Failed to resend confirmation email. Please try again later.", e);
        }
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            tokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
            log.debug("Cleaned up expired email confirmation tokens");
        } catch (Exception e) {
            log.error("Error during cleanup of expired tokens: {}", e.getMessage());
        }
    }
}
