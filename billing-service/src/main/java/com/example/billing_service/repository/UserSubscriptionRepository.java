package com.example.billing_service.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing_service.entity.UserSubscription;
import com.example.billing_service.entity.enums.SubscriptionStatus;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    
    Optional<UserSubscription> findByUserId(String userId);
    Optional<UserSubscription>findByUserIdAndStatusIn(String userId, List<SubscriptionStatus> statusList);
}
