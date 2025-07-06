package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.service.EmailConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.dealkartbd.backend_app.constans.ApiPaths.*;
import static com.dealkartbd.backend_app.constans.AppConstants.*;

@RestController
@RequestMapping(AUTH_PATH_PREFIX)
@RequiredArgsConstructor
public class AuthController {

    private final EmailConfirmationService emailConfirmationService;

    @GetMapping(CONFIRM_EMAIL_PATH_SUFFIX)
    public ResponseEntity<String> confirmEmail(@RequestParam String token) {
        emailConfirmationService.confirmEmail(token);
        return ResponseEntity.ok(EMAIL_CONFIRMATION_MSG);
    }

    @PostMapping(RESEND_CONFIRMATION_PATH_SUFFIX)
    public ResponseEntity<String> resendConfirmation(@RequestParam String email) {
        emailConfirmationService.resendConfirmationEmail(email);
        return ResponseEntity.ok(CONFIRMATION_EMAIL_MSG);
    }
}
