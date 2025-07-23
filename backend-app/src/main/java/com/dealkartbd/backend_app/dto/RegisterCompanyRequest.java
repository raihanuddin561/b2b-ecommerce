package com.dealkartbd.backend_app.dto;

public record RegisterCompanyRequest(
    String companyName,
    String registrationNumber,
    String companyType,
    String industry,
    String website,
    String companyPhone,
    String taxId,
    String addressStreet,
    String addressCity,
    String addressState,
    String addressPostalCode,
    String addressCountry,
    String adminName,
    String adminEmail,
    String adminPassword,
    String adminPhone,
    String adminRole
) {}
