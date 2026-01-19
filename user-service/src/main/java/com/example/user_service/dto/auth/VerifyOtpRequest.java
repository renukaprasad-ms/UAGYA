package com.example.user_service.dto.auth;

public class VerifyOtpRequest {
    private String email;
    private String otp;

    public String getEmail() {
        return this.email;
    }
    public String getOtp() {
        return this.otp;
    }
}
