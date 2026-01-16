package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.Plan;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByPlanCode(String planCode);

    boolean existsByPlanCode(String planCode);
}