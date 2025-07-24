package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.RegisterBuyerRequest;
import com.dealkartbd.backend_app.service.RegisterBuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/buyers")
@RequiredArgsConstructor
public class RegisterBuyerController {
    private final RegisterBuyerService registerBuyerService;

    /**
     * Registers a new buyer. Returns 201 Created on success.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        registerBuyerService.registerBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful. Please check your email to " +
                "confirm your account.");
    }
}

