package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.service.EmailConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dealkartbd.backend_app.constans.ApiPaths.*;
import static com.dealkartbd.backend_app.constans.AppConstants.CONFIRMATION_EMAIL_MSG;
import static com.dealkartbd.backend_app.constans.AppConstants.REGISTRATION_SUCCESS_MSG;

@RestController
@RequestMapping(AUTH_PATH_PREFIX)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final EmailConfirmationService emailConfirmationService;

    @GetMapping(CONFIRM_EMAIL_PATH_SUFFIX)
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        emailConfirmationService.confirmEmail(token);
        return ResponseEntity.ok(CONFIRMATION_EMAIL_MSG);
    }

    @PostMapping(RESEND_CONFIRMATION_PATH_SUFFIX)
    public ResponseEntity<String> resendConfirmation(@RequestParam String email) {
        log.info("Resend confirmation request for email: {}", email);
        emailConfirmationService.resendConfirmationEmail(email);
        return ResponseEntity.ok(REGISTRATION_SUCCESS_MSG);
    }
}
