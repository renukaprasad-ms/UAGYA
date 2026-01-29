package com.example.user_service.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class AppKey {

    private static final SecureRandom random = new SecureRandom();

    public static String generate(String appName) {
        String normalizedName = appName
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        byte[] bytes = new byte[16]; 
        random.nextBytes(bytes);

        String randomPart = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        return "app_" + normalizedName + "_" + randomPart;
    }
}
