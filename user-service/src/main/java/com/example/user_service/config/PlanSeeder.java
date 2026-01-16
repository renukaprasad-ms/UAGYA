package com.example.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.user_service.entity.Plan;
import com.example.user_service.repository.PlanRepository;

@Configuration
public class PlanSeeder {
     @Bean
     CommandLineRunner seedPlans(PlanRepository planRepository) {
        return args -> {
            seedPlanIfNotExists(
                planRepository,
                "FREE",
                "Free Plan",
                0,
                30
            );
            seedPlanIfNotExists(
                    planRepository,
                    "BASIC",
                    "Basic Plan",
                    499,
                    30
            );

            seedPlanIfNotExists(
                    planRepository,
                    "PRO",
                    "Pro Plan",
                    999,
                    30
            );
        };
     }

     private void seedPlanIfNotExists(
        PlanRepository repo,
        String planCode,
        String name,
        int price,
        int durationDays
     ) {
        if(!repo.existsByPlanCode(planCode)) {
            Plan plan = new Plan();
            plan.setPlanCode(planCode);
            plan.setName(name);
            plan.setPrice(price);
            plan.setDurationDays(durationDays);
            plan.setActive(true);

            repo.save(plan);
        }
     }
}
