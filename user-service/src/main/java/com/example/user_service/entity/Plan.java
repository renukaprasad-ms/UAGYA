package com.example.user_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "plans",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plan_code"})
    }
)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_code", nullable = false)
    private String planCode;   

    @Column(name = "name", nullable = false)
    private String name;       

    @Column(name = "price", nullable = false)
    private Integer price;    

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Plan() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // -------- Getters --------

    public Long getId() {
        return id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // -------- Setters --------

    public void setId(Long id) {
        this.id = id;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
