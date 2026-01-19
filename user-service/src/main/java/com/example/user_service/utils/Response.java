package com.example.user_service.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Response {
    public Map<String, Object> response(
            boolean success, int statusCode, Object data, String message, String errorMessage) {
        Map<String, Object> response = new HashMap<>();

        response.put("success", success);
        response.put("status_code", statusCode);
        if (data != null) {
            response.put("data", data);
        }

        if (message != null) {
            response.put("message", message);
        }

        if (errorMessage != null) {
            response.put("error", errorMessage);
        }

        return response;
    }
}
