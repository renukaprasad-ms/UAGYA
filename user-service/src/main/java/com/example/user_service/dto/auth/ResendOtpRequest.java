package com.example.user_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class ResendOtpRequest {
    @NotNull
    @Email
    private String email;

    public String getEmail() {
        return this.email;
    }
}
