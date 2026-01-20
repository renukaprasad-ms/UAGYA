package com.example.billing_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.billing_service.entity.Plan;
import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.PlanType;
import com.example.billing_service.repository.PlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;

    public List<Plan> getAllPlans() {
        return planRepository.findByActiveTrue();
    }

    public Plan addPlan(
            String code,
            String name,
            PlanType planType,
            BillingCycle billingCycle,
            BigDecimal price,
            String currency) {
        Plan plan = Plan.builder()
                .code(code)
                .name(name)
                .planType(planType)
                .billingCycle(billingCycle)
                .price(price)
                .currency(currency)
                .active(true)
                .build();
        Plan savedPlan = planRepository.save(plan);
        return savedPlan;
    }
}
