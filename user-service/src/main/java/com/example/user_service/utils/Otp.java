package com.example.user_service.utils;

import java.util.Random;

import org.springframework.stereotype.Component;

@Component
public class Otp {
    public String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

}
