package com.example.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.auth.AuthResponse;
import com.example.user_service.dto.auth.LoginRequest;
import com.example.user_service.dto.auth.ResendOtpRequest;
import com.example.user_service.dto.auth.VerifyOtpRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.entity.User;
import com.example.user_service.service.auth.AuthService;
import com.example.user_service.utils.Response;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import com.example.user_service.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;
    private final Response reponseUtil;

    public AuthController(AuthService authService, Response reponseUtil) {
        this.authService = authService;
        this.reponseUtil = reponseUtil;
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody @Valid ResendOtpRequest request, HttpServletRequest req) {
        Application app =(Application) req.getAttribute("APPLICATION");
        String res = authService.resendOtp(request, app);
        return ResponseEntity.status(200).body(
                reponseUtil.response(true, 200, res, "OTP sent to email", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletResponse response, HttpServletRequest req) {

        Application app = (Application) req.getAttribute("APPLICATION");

        AuthResponse res = authService.verifyOtp(request, app);

        boolean rememberDevice = request.getRememberDevice();

        Cookie accessTokenCookie = new Cookie("access_token", res.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60);

        Cookie refreshTokenCookie = new Cookie("refresh_token", res.getRefreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        if (rememberDevice) {
            refreshTokenCookie.setMaxAge(30 * 24 * 60 * 60);
        } else {
            refreshTokenCookie.setMaxAge(-1);
        }

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        Map<String, Object> userRes = new HashMap<>();
        userRes.put("firstname", res.getUser().getFirstname());
        userRes.put("lastname", res.getUser().getLastname());
        userRes.put("email", res.getUser().getEmail());
        userRes.put("status", res.getUser().getStatus());
        userRes.put("id", res.getUser().getId());

        return ResponseEntity.status(200).body(
                reponseUtil.response(true, 200, userRes, "OTP verified successfully", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {

        Cookie access = new Cookie("access_token", null);
        access.setMaxAge(0);
        access.setPath("/");
        access.setHttpOnly(true);

        Cookie refresh = new Cookie("refresh_token", null);
        refresh.setMaxAge(0);
        refresh.setPath("/");
        refresh.setHttpOnly(true);

        response.addCookie(access);
        response.addCookie(refresh);

        return ResponseEntity.status(200).body(
                reponseUtil.response(true, 200, null, "Logged out", null));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid LoginRequest request, HttpServletRequest req) {
        Application app = (Application) req.getAttribute("APPLICATION");
        User user = authService.login(request,app);

        Map<String, Object> userRes = new HashMap<>();
        userRes.put("firstname", user.getFirstname());
        userRes.put("lastname", user.getLastname());
        userRes.put("email", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                reponseUtil.response(true, 204, userRes, "OTP sent to email", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(HttpServletRequest request,
            HttpServletResponse response) {
        if (request.getCookies() == null) {
            throw new BadRequestException("Refresh token cookie missing");
        }
        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if ("refresh_token".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
            }
        }

        if (refreshToken != null) {
            AuthResponse res = authService.refreshToken(refreshToken);

            Cookie accessTokenCookie = new Cookie("access_token", res.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60);

            Cookie refreshTokenCookie = new Cookie("refresh_token", res.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);
        } else {
            throw new BadRequestException("Refresh token cookie missing");
        }

        return ResponseEntity.status(200).body(
                reponseUtil.response(true, 200, null, "tokens refreshed", null));
    }

}
