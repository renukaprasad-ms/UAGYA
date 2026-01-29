package com.example.user_service.entity;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "applications",
    indexes = {
        @Index(name = "idx_app_key", columnList = "app_key")
    }
)
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="app_key", unique = true, nullable = false)
    private String appKey;

     @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean active;


    @Column(nullable = false )
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    

    @PrePersist
    protected void onCreate() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    //getters

    public Long getId() {
        return this.id;
    }
    public String getAppKey() {
        return this.appKey;
    }
    public String getName() {
        return this.name;
    }
    public boolean getIsActive() {
        return this.active;
    }

    //setters

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setIsActive(boolean active) {
        this.active = active;
    }
}
