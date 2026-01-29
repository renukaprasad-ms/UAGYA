package com.example.user_service.service.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.auth.AuthResponse;
import com.example.user_service.dto.auth.LoginRequest;
import com.example.user_service.dto.auth.ResendOtpRequest;
import com.example.user_service.dto.auth.VerifyOtpRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.RefreshToken;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserApp;
import com.example.user_service.entity.UserDeviceSession;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.RefreshTokenRepository;
import com.example.user_service.repository.UserAppRepository;
import com.example.user_service.repository.UserDeviceSessionRepository;
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
    private final UserDeviceSessionRepository userDeviceSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
            EmailOtpRepository emailOtpRepository,
            Otp otpUtil,
            EmailService emailService, JwtService jwtService, UserAppRepository userAppRepository,
            UserDeviceSessionRepository userDeviceSessionRepository, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.emailOtpRepository = emailOtpRepository;
        this.otpUtil = otpUtil;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.userAppRepository = userAppRepository;
        this.userDeviceSessionRepository = userDeviceSessionRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String resendOtp(ResendOtpRequest request, Application app) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!userAppRepository.existsByuserAndApp(user, app)) {
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
    public AuthResponse verifyOtp(VerifyOtpRequest request, Application app) {

        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        UserApp userApp = getUserAppOrThrow(user, app);

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
        emailOtpRepository.deleteByUser(user);

        Optional<UserDeviceSession> existing = userDeviceSessionRepository.findByUserAndAppAndDeviceId(user, app,
                request.getDeviceId());

        if (existing.isEmpty()) {
            long activeCount = userDeviceSessionRepository.countByUserAndAppAndActiveTrue(user, app);

            if (activeCount >= 2) {
                throw new BadRequestException("Device limit exceeded");
            }
        }

        UserDeviceSession deviceSession = userDeviceSessionRepository
                .findByUserAndAppAndDeviceId(user, app, request.getDeviceId())
                .orElseGet(UserDeviceSession::new);
        deviceSession.setApp(app);
        deviceSession.setUser(user);
        deviceSession.setDeviceId(request.getDeviceId());
        deviceSession.setDeviceType(request.getDeviceType());
        deviceSession.setActive(true);
        deviceSession.setLastLoginAt(LocalDateTime.now());

        userDeviceSessionRepository.save(deviceSession);
        refreshTokenRepository.revokeByDeviceSession(deviceSession);

        String accessToken = jwtService.generateAccessToken(
                user,
                app.getId(),
                deviceSession.getDeviceId(),
                userApp.getRole().getRole());
        String refreshToken = generateRefreshToken();
        LocalDateTime refreshExpiresAt = LocalDateTime.now()
                .plusDays(request.getRememberDevice() ? 30 : 7);

        RefreshToken refresh = new RefreshToken();
        refresh.setToken(hashRefreshToken(refreshToken));
        refresh.setUser(user);
        refresh.setApp(app);
        refresh.setDeviceSession(deviceSession);
        refresh.setExpiresAt(refreshExpiresAt);

        refreshTokenRepository.save(refresh);

        return new AuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, Application app) {
        String email = normalizeEmail(request.getEmail());
        String password = request.getPassword();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
        UserApp userApp = getUserAppOrThrow(user, app);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        Optional<UserDeviceSession> existing = userDeviceSessionRepository.findByUserAndAppAndDeviceId(user, app,
                request.getDeviceId());

        if (existing.isEmpty()) {
            long activeCount = userDeviceSessionRepository.countByUserAndAppAndActiveTrue(user, app);

            if (activeCount >= 2) {
                throw new BadRequestException("Device limit exceeded");
            }
        }

        UserDeviceSession deviceSession = userDeviceSessionRepository
                .findByUserAndAppAndDeviceId(user, app, request.getDeviceId())
                .orElseGet(UserDeviceSession::new);
        deviceSession.setApp(app);
        deviceSession.setUser(user);
        deviceSession.setDeviceId(request.getDeviceId());
        deviceSession.setDeviceType(request.getDeviceType());
        deviceSession.setActive(true);
        deviceSession.setLastLoginAt(LocalDateTime.now());

        userDeviceSessionRepository.save(deviceSession);
        refreshTokenRepository.revokeByDeviceSession(deviceSession);

        String accessToken = jwtService.generateAccessToken(
                user,
                app.getId(),
                deviceSession.getDeviceId(),
                userApp.getRole().getRole());
        String refreshToken = generateRefreshToken();

        LocalDateTime refreshExpiresAt = LocalDateTime.now()
                .plusDays(request.getRememberDevice() ? 30 : 7);

        RefreshToken refresh = new RefreshToken();
        refresh.setToken(hashRefreshToken(refreshToken));
        refresh.setUser(user);
        refresh.setApp(app);
        refresh.setDeviceSession(deviceSession);
        refresh.setExpiresAt(refreshExpiresAt);

        refreshTokenRepository.save(refresh);

        return new AuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new BadRequestException("refresh token not found");
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenHash)
                .orElseThrow(() -> new BadRequestException("invalid token"));

        if (token.isRevoked()) {
            throw new BadRequestException("invalid token");
        }

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(now)) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new BadRequestException("refresh token expired");
        }


        User user = token.getUser();
        if (user == null) {
            throw new BadRequestException("user not found");
        }
        Application app = token.getApp();
        if (app == null) {
            throw new BadRequestException("app not found");
        }
        UserDeviceSession session = token.getDeviceSession();
        if (session == null || !session.isActive()) {
            throw new BadRequestException("Device logged out");
        }
        UserApp userApp = getUserAppOrThrow(user, app);

        refreshTokenRepository.revokeByDeviceSession(session);

        String newRefreshToken = generateRefreshToken();
        RefreshToken newToken = new RefreshToken();
        newToken.setToken(hashRefreshToken(newRefreshToken));
        newToken.setUser(user);
        newToken.setApp(app);
        newToken.setDeviceSession(session);
        newToken.setExpiresAt(refreshExpiryFromPrevious(now, token));

        refreshTokenRepository.save(newToken);

        String newAccessToken = jwtService.generateAccessToken(
                user,
                app.getId(),
                session.getDeviceId(),
                userApp.getRole().getRole());
        return new AuthResponse(user, newAccessToken, newRefreshToken);
    }

    @Transactional
    public boolean logout(String refreshToken) {
        if (refreshToken == null) {
            throw new BadRequestException("refresh token not found");
        }

        String refreshTokenHash = hashRefreshToken(refreshToken);
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenHash)
                .orElseThrow(() -> new BadRequestException("user already logged out"));

        if (token.isRevoked()) {
            throw new BadRequestException("user already logged out");
        }

        UserDeviceSession session = token.getDeviceSession();
        if (session != null) {
            refreshTokenRepository.revokeByDeviceSession(session);
            session.setActive(false);
            userDeviceSessionRepository.save(session);
        } else {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }

        return true;
    }

    private UserApp getUserAppOrThrow(User user, Application app) {
        return userAppRepository.findByUserAndApp(user, app)
                .orElseThrow(() -> new ResourceNotFoundException("useer not found"));
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }

    private String hashRefreshToken(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private LocalDateTime refreshExpiryFromPrevious(LocalDateTime now, RefreshToken token) {
        if (token.getCreatedAt() == null || token.getExpiresAt() == null) {
            return now.plusDays(7);
        }
        Duration ttl = Duration.between(token.getCreatedAt(), token.getExpiresAt());
        if (ttl.isZero() || ttl.isNegative()) {
            return now.plusDays(7);
        }
        return now.plusSeconds(ttl.getSeconds());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
