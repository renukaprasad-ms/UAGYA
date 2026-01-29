package com.example.user_service.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.auth.AuthResponse;
import com.example.user_service.dto.auth.LoginRequest;
import com.example.user_service.dto.auth.ResendOtpRequest;
import com.example.user_service.dto.auth.VerifyOtpRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.User;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.UserAppRepository;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserAppRepository userAppRepository;

    public AuthService(UserRepository userRepository,
            EmailOtpRepository emailOtpRepository,
            Otp otpUtil,
            EmailService emailService, JwtService jwtService,  UserAppRepository userAppRepository) {
        this.userRepository = userRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.otpUtil = otpUtil;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.userAppRepository = userAppRepository;
    }

    public String resendOtp(ResendOtpRequest request, Application app) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if(!userAppRepository.existsByuserAndApp(user, app)) {
            throw new ResourceNotFoundException("useer not found");
        }

        String otp = otpUtil.generateOtp();

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElse(new EmailOtp());

        emailOtp.setUser(user);
        emailOtp.setOtp(passwordEncoder.encode(otp));
        emailOtp.setUsed(false);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        emailOtpRepository.save(emailOtp);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return "OTP sent successfully";
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request , Application app) {


        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if(!userAppRepository.existsByuserAndApp(user, app)) {
            throw new ResourceNotFoundException("useer not found");
        }

        EmailOtp emailOtp = emailOtpRepository
                .findByUserAndUsedFalse(user)
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        if (!passwordEncoder.matches(request.getOtp(), emailOtp.getOtp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        user.setIsEmailVerified(true);
        emailOtp.setUsed(true);

        userRepository.save(user);
        emailOtpRepository.save(emailOtp);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, request.getRememberDevice());

        return new AuthResponse(user, accessToken, refreshToken);
    }

    public User login(LoginRequest request, Application app) {
        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        if(!userAppRepository.existsByuserAndApp(user, app)) {
            throw new ResourceNotFoundException("useer not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String otp = otpUtil.generateOtp();

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElse(new EmailOtp());

        emailOtp.setUser(user);
        emailOtp.setOtp(passwordEncoder.encode(otp));
        emailOtp.setUsed(false);
        emailOtp.setExpiresAt(LocalDateTime.now().plusMinutes(2));

        emailOtpRepository.save(emailOtp);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return user;
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new BadRequestException("refresh token not found");
        }

        Jwt jwt = jwtService.verifyRefreshToken(refreshToken);

        String email = normalizeEmail(jwt.getSubject());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("user not found"));
        if(userAppRepository.findByUser(user).isEmpty()) {
            throw new ResourceNotFoundException("useer not found");
        }
        String newAccessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(user, newAccessToken, refreshToken);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
