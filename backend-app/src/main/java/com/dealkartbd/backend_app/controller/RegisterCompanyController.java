package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.RegisterCompanyRequest;
import com.dealkartbd.backend_app.dto.RegisterCompanyResponse;
import com.dealkartbd.backend_app.service.RegisterCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterCompanyController {

    private final RegisterCompanyService registerCompanyService;

    @PostMapping("/register-company")
    public ResponseEntity<RegisterCompanyResponse> registerCompany(
            @RequestBody RegisterCompanyRequest request) {
        RegisterCompanyResponse response = registerCompanyService.registerCompany(request);
        return ResponseEntity.ok(response);
    }
}
