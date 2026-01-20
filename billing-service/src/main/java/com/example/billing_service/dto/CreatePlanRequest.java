package com.example.billing_service.dto;

import java.math.BigDecimal;

import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.PlanType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePlanRequest {

    @NotNull(message = "Plan type is required")
    private PlanType planType;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle;

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    private boolean active;
}
