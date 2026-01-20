package com.example.billing_service.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing_service.entity.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    Optional<UserSubscription> findByUserId(Long userId);
}
