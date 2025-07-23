package com.dealkartbd.backend_app.dto;

public record RegisterBuyerRequest(
    String fullName,
    String email,
    String password,
    String phone,
    String addressStreet,
    String addressCity,
    String addressState,
    String addressPostalCode,
    String addressCountry
) {}

