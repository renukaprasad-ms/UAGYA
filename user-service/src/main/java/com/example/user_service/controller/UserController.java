package com.example.user_service.controller;

import com.example.user_service.dto.user.CheckEmailRequest;
import com.example.user_service.dto.user.UserCreateRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.entity.User;
import com.example.user_service.service.user.UserService;
import com.example.user_service.utils.Response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;



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
            @Valid @RequestBody UserCreateRequest request,HttpServletRequest req
    ) {
        Application app = (Application) req.getAttribute("APPLICATION");
        User user = userService.createUser(request, app);
        Map<String, Object> userRes = new HashMap<>();
        userRes.put("firstname", user.getFirstname());
        userRes.put("lastname", user.getLastname());
        userRes.put("email", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(
            reponseUtil.response(true, 204, userRes, "OTP sent to email", null)
        );
    }

    @PostMapping("/check-email")
    public ResponseEntity<Map<String,Object>> checkEmail(@RequestBody CheckEmailRequest request, HttpServletRequest req) {
        Application app = (Application) req.getAttribute("APPLICATION");
        boolean exists = userService.checkEmail(request.getEmail(), app);
         Map<String, Object> data = Map.of("exists", exists);
        return ResponseEntity.status(200).body(
            reponseUtil.response(true, 200, data,  exists ? "Email already exists" : "Email is available", null)
        );
    }

    @GetMapping
    public ResponseEntity<Map<String,Object>> getAllUser() {
        
        List<User> usres = userService.getAllUser();

        return ResponseEntity.status(200).body(
            reponseUtil.response(true, 0, usres, "user fteched successfully", null)
        );

    }
    
    
}
