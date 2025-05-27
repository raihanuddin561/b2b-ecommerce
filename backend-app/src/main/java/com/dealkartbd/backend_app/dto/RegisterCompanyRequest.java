package com.dealkartbd.backend_app.dto;

import lombok.Data;

@Data
public class RegisterCompanyRequest {
    private String companyName;
    private String registrationNumber;
    private String address;

    private String adminName;
    private String adminEmail;
    private String adminPassword;
}
