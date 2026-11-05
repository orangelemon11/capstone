 package org.example.dto;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName,
        String companyName,
        String phone
) {}
