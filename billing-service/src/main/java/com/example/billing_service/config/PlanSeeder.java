package com.example.billing_service.config;

import com.example.billing_service.entity.Plan;
import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.PlanType;
import com.example.billing_service.repository.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class PlanSeeder {

    @Bean
    CommandLineRunner seedPlans(PlanRepository planRepository) {
        return args -> {

            seedPlanIfNotExist(
                    planRepository,
                    "BASIC_MONTHLY",
                    "Basic Monthly Plan",
                    PlanType.SUBSCRIPTION,
                    BillingCycle.MONTHLY,
                    BigDecimal.valueOf(9.99),
                    "USD"
            );

            seedPlanIfNotExist(
                    planRepository,
                    "BASIC_YEARLY",
                    "Basic Yearly Plan",
                    PlanType.SUBSCRIPTION,
                    BillingCycle.YEARLY,
                    BigDecimal.valueOf(99.99),
                    "USD"
            );

            seedPlanIfNotExist(
                    planRepository,
                    "ONE_TIME_PASS",
                    "One Time Access",
                    PlanType.ONE_TIME,
                    BillingCycle.MONTHLY,
                    BigDecimal.valueOf(19.99),
                    "USD"
            );
        };
    }

    private void seedPlanIfNotExist(
            PlanRepository planRepository,
            String code,
            String name,
            PlanType planType,
            BillingCycle billingCycle,
            BigDecimal price,
            String currency
    ) {

        boolean exists = planRepository.findByCode(code).isPresent();
        if (exists) {
            return;
        }

        Plan plan = Plan.builder()
                .code(code)
                .name(name)
                .planType(planType)
                .billingCycle(billingCycle)
                .price(price)
                .currency(currency)
                .active(true)
                .build();

        planRepository.save(plan);
    }
}
