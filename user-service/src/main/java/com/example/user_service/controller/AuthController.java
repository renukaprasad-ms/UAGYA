package com.example.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.auth.ResendOtpRequest;
import com.example.user_service.dto.auth.VerifyOtpRequest;
import com.example.user_service.entity.User;
import com.example.user_service.service.AuthService;
import com.example.user_service.utils.Response;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> resendOtp(@RequestBody ResendOtpRequest request) {
        String res =  authService.resendOtp(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
            reponseUtil.response(true, 204, res, "OTP sent to email", null)
        );
    }
    
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        User user = authService.verifyOtp(request.getOtp(), request.getEmail());
        Map<String, Object> userRes = new HashMap<>();
        userRes.put("firstname", user.getFirstname());
        userRes.put("lastname", user.getLastname());
        userRes.put("email", user.getEmail());
        userRes.put("role", user.getRoleName());
        userRes.put("status", user.getStatus());
        return ResponseEntity.status(200).body(
            reponseUtil.response(true, 200, userRes, "OTP verified successfully", null)
        );
        
    }
    
}
