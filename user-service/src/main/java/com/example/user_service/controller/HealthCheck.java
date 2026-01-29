package com.example.user_service.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.utils.Response;

@RestController
@RequestMapping("/health-check")
public class HealthCheck {
    private final Response resUtil;

    public HealthCheck(Response resUtil) {
        this.resUtil = resUtil;
    }

    @GetMapping
    public ResponseEntity<Map<String,Object>> check() {
        return ResponseEntity.status(200).body(
            resUtil.response(true, 200, null, "server running", null)
        );
    }
}
