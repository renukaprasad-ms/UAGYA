package com.example.billing_service.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.billing_service.entity.Plan;
import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.PlanType;
import com.example.billing_service.exception.BadRequestExecption;
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
            String currency
    ) {

        if (code == null || code.isBlank()) {
            throw new BadRequestExecption("Plan code is required");
        }

        if (name == null || name.isBlank()) {
            throw new BadRequestExecption("Plan name is required");
        }

        if (planType == null) {
            throw new BadRequestExecption("Plan type is required");
        }

        if (billingCycle == null) {
            throw new BadRequestExecption("Billing cycle is required");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestExecption("Price must be greater than 0");
        }

        if (currency == null || currency.length() != 3) {
            throw new BadRequestExecption("Currency must be a 3-letter ISO code");
        }

        if (planRepository.existsByCode(code)) {
            throw new BadRequestExecption(code);
        }

        Plan plan = Plan.builder()
                .code(code)
                .name(name)
                .planType(planType)
                .billingCycle(billingCycle)
                .price(price)
                .currency(currency.toUpperCase())
                .active(true)
                .build();

        return planRepository.save(plan);
    }
}
