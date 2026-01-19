package com.example.user_service.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.User;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.utils.Otp;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final Otp otpUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       EmailOtpRepository emailOtpRepository,
                       Otp otpUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.otpUtil = otpUtil;
        this.emailService = emailService;
    }

    public String resendOtp(String email) {

        if (email == null || email.isBlank()
                || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Please provide a valid email address.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String otp = otpUtil.generateOtp();

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElse(new EmailOtp());

        emailOtp.setUser(user);
        emailOtp.setOtp(otp);
        emailOtp.setUsed(false);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        emailOtpRepository.save(emailOtp);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP sent successfully";
    }

    public User verifyOtp(String otp, String email) {

        if (email == null || email.isBlank()
                || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Please provide a valid email address.");
        }

        if (otp == null || !otp.matches("\\d{6}")) {
            throw new BadRequestException("Please provide a valid 6-digit OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        EmailOtp emailOtp = emailOtpRepository
                .findByUserAndOtpAndUsedFalse(user, otp)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);

        return user;
    }
}

