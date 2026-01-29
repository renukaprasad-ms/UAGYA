package com.example.user_service.entity;

import java.time.LocalDateTime;

import com.example.user_service.entity.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_app_mapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "app_id" })
}, indexes = {
        @Index(name = "idx_user_app", columnList = "user_id, app_id")
})
public class UserApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_id", nullable = false)
    private Application app;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private boolean isAppAccessEnabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getters

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public Application getApp() {
        return this.app;
    }

    public Role getRole() {
        return this.role;
    }

    public UserStatus getStatus() {
        return this.status;
    }

    public boolean getIsAppAccessEnabled() {
        return this.isAppAccessEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    // setters

    public void setUser(User user) {
        this.user = user;
    }

    public void setApp(Application app) {
        this.app = app;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setIsAppAccessEnabled(boolean isAppAccessEnabled) {
        this.isAppAccessEnabled = isAppAccessEnabled;
    }

}
