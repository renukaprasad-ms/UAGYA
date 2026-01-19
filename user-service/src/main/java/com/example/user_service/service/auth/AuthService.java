package com.example.user_service.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.auth.AuthResponse;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.User;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.utils.Otp;

import jakarta.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final Otp otpUtil;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder() ;

    public AuthService(UserRepository userRepository,
            EmailOtpRepository emailOtpRepository,
            Otp otpUtil,
            EmailService emailService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.otpUtil = otpUtil;
        this.emailService = emailService;
        this.jwtService = jwtService;
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

    @Transactional
    public AuthResponse verifyOtp(String otp, String email) {

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

        user.setIsEmailVerified(true);
        emailOtp.setUsed(true);

        userRepository.save(user);
        emailOtpRepository.save(emailOtp);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(user, accessToken, refreshToken);
    }

    public User login(String email, String password) {
        if (email == null || email.isBlank()
                || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new BadRequestException("Please provide a valid email address.");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Please provide password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        return user;
    }

}
