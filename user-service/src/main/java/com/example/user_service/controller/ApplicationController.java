package com.example.user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.user_service.dto.app.AppCreateRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.service.applications.ApplicationService;
import com.example.user_service.utils.Response;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("api/app")
public class ApplicationController {
    private final ApplicationService applicationService;
    private final Response responseUtil;

    public ApplicationController(ApplicationService applicationService, Response responseUtil) {
        this.applicationService = applicationService;
        this.responseUtil = responseUtil;
    }

    @PostMapping()
    public ResponseEntity<Map<String, Object>> createApp(@RequestBody AppCreateRequest request) {
        Application app = applicationService.createApplication(request.getName());

        return ResponseEntity.status(201)
                .body(responseUtil.response(true, 201, app, "app registered successfully", null));

    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getApps() {
        List<Application> appList = applicationService.getAllApplications();

        return ResponseEntity.status(200)
                .body(responseUtil.response(true, 200, appList, "app's fetched successfully", null));
    }

}
