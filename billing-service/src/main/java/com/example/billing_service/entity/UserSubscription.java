package com.example.billing_service.entity;

import com.example.billing_service.entity.enums.BillingCycle;
import com.example.billing_service.entity.enums.PaymentMethod;
import com.example.billing_service.entity.enums.PaymentProvider;
import com.example.billing_service.entity.enums.SubscriptionStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
    name = "user_subscriptions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "plan_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "auto_debit_enabled", nullable = false)
    private boolean autoDebitEnabled;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "next_billing_date")
    private Instant nextBillingDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
