package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.service.EmailConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dealkartbd.backend_app.constans.ApiPaths.*;
import static com.dealkartbd.backend_app.constans.AppConstants.*;

@RestController
@RequestMapping(AUTH_PATH_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final EmailConfirmationService emailConfirmationService;

    @GetMapping(CONFIRM_EMAIL_PATH_SUFFIX)
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        emailConfirmationService.confirmEmail(token);
        return ResponseEntity.ok("Email confirmed successfully! Your account is now active.");
    }

    @PostMapping(RESEND_CONFIRMATION_PATH_SUFFIX)
    public ResponseEntity<String> resendConfirmation(@RequestParam String email) {
        log.info("Resend confirmation request for email: {}", email);
        emailConfirmationService.resendConfirmationEmail(email);
        return ResponseEntity.ok("Confirmation email sent successfully. Please check your inbox.");
    }
}
