package com.example.user_service.entity;

import java.time.LocalDateTime;

import com.example.user_service.entity.enums.SubscriptionStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @PrePersist
    protected void onCreate() {
        this.startDate = LocalDateTime.now();
    }

    // ----- Getters -----

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Plan getPlan() {
        return plan;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

}