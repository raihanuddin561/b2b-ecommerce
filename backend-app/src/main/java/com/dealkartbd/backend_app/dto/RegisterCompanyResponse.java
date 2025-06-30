package com.dealkartbd.backend_app.dto;

import lombok.Data;

@Data
public class RegisterCompanyResponse {
    private String message;

    public RegisterCompanyResponse(String message) {
        this.message = message;
    }
}
