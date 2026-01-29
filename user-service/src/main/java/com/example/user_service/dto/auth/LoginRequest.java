package com.example.user_service.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    @NotNull(message = "email is required")
    @Email(message = "enter valid email")
    private String email;
    @NotNull(message = "password is required")
    private String password;

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }
}
