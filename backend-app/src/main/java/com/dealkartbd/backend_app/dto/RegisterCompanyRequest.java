package com.dealkartbd.backend_app.dto;

public record RegisterCompanyRequest(
    String companyName,
    String registrationNumber,
    String address,
    String adminName,
    String adminEmail,
    String adminPassword
) {}
