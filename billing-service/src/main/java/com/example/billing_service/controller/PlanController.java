package com.example.billing_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.billing_service.dto.CreatePlanRequest;
import com.example.billing_service.entity.Plan;
import com.example.billing_service.service.PlanService;
import com.example.billing_service.utils.Response;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;
    private final Response responseUtil;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPlans() {
        return ResponseEntity.status(200).body(
                responseUtil.response(true, 200, planService.getAllPlans(), "Plans fetched successfully", null));
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addPlan(@Valid @RequestBody CreatePlanRequest request) {
        Plan plan = planService.addPlan(request.getCode(), request.getName(), request.getPlanType(),
                request.getBillingCycle(), request.getPrice(), request.getCurrency());

        return ResponseEntity.status(201).body(
                responseUtil.response(true, 201, plan, "plan created", null));
    }

}
