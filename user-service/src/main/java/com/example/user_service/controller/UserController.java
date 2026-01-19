package com.example.user_service.controller;

import com.example.user_service.dto.user.UserCreateRequest;
import com.example.user_service.entity.User;
import com.example.user_service.service.UserService;
import com.example.user_service.utils.Response;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final Response reponseUtil;

    public UserController(UserService userService, Response reponseUtil) {
        this.userService = userService;
        this.reponseUtil = reponseUtil;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        User user = userService.createUser(request);
        Map<String, Object> userRes = new HashMap<>();
        userRes.put("firstname", user.getFirstname());
        userRes.put("lastname", user.getLastname());
        userRes.put("email", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
            reponseUtil.response(true, 204, userRes, "OTP sent to email", null)
        );
    }
}
