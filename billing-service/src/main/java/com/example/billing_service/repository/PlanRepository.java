package com.example.billing_service.repository;

import com.example.billing_service.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByCode(String code);
    Optional<Plan> findById(Long id);
    List<Plan> findByActiveTrue();
    boolean existsByCode(String code);
}
