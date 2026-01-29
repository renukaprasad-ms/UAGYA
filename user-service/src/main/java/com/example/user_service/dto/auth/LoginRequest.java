package com.example.user_service.dto.auth;

import com.example.user_service.entity.enums.DeviceType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {
    @NotNull(message = "email is required")
    @Email(message = "enter valid email")
    private String email;
    @NotNull(message = "password is required")
    private String password;
    private boolean rememberDevice;
    @NotBlank(message = "deviceId is required")
    private String device_id;
    @NotNull(message = "deviceType is required")
    private DeviceType device_type;

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean getRememberDevice() {
        return this.rememberDevice;
    }

    public String getDeviceId() {
        return this.device_id;
    }

    public DeviceType getDeviceType() {
        return this.device_type;
    }
}
