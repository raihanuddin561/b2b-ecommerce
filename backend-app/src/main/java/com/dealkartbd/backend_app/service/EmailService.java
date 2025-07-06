package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.constans.ApiPaths;
import com.dealkartbd.backend_app.exception.EmailSendingException;
import com.dealkartbd.backend_app.exception.InvalidEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static com.dealkartbd.backend_app.constans.ApiPaths.CONFIRM_EMAIL_PATH;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public void sendEmailConfirmation(String toEmail, String token, String userName) {
        validateEmail(toEmail);
        try {
            SimpleMailMessage message = getSimpleMailMessage(toEmail, token, userName);
            mailSender.send(message);
            log.info("Email confirmation sent successfully to: {}", toEmail);

        } catch (MailException e) {
            log.error("Failed to send email to: {} - Error: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send confirmation email. Please try again later.", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {} - Error: {}", toEmail, e.getMessage());
            throw new EmailSendingException("An unexpected error occurred while sending email.", e);
        }
    }

    private SimpleMailMessage getSimpleMailMessage(String toEmail, String token, String userName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Confirm Your Email Address");

        String confirmationUrl = baseUrl + CONFIRM_EMAIL_PATH +"?token=" + token;

        String text = String.format(
                "Dear %s,\n\n" +
                        "Thank you for registering with Kuttush Family. " +
                        "Please click the link below to confirm your email address:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't create an account, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "The Team",
                userName, confirmationUrl
        );

        message.setText(text);
        return message;
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email address cannot be null or empty");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new InvalidEmailException("Invalid email format: " + email);
        }
    }
}
