package com.example.user_service.dto.auth;

import com.example.user_service.entity.User;

public class AuthResponse {

    private User user;
    private String accessToken;
    private String refreshToken;

    public AuthResponse(User user, String accessToken, String refreshToken) {
        this.user = user;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
