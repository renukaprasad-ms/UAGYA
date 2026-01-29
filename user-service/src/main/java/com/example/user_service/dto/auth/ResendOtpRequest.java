package com.example.user_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class ResendOtpRequest {
    @NotNull(message = "email is required")
    @Email(message = "enter valid email")
    private String email;

    public String getEmail() {
        return this.email;
    }
}
