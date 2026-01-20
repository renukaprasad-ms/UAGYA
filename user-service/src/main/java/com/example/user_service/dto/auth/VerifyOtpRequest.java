package com.example.user_service.dto.auth;

public class VerifyOtpRequest {
    private String email;
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
