package com.example.user_service.dto.auth;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class VerifyOtpRequest {
    @NotNull(message = "email is required")
    @Email(message = "enter a valid email")
    private String email;
    @NotNull(message = "otp is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
    private String otp;
    private boolean rememberDevice;

    public String getEmail() {
        return this.email;
    }
    public String getOtp() {
        return this.otp;
    }
    public boolean getRememberDevice() {
    return this.rememberDevice;
}
}
