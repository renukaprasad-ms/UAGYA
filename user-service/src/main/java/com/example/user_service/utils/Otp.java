package com.example.user_service.utils;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class Otp {
    private final SecureRandom random = new SecureRandom();

    public String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

}
