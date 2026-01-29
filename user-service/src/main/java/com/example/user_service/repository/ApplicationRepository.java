package com.example.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.user_service.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByAppKey(String key);

    boolean existsByName(String name);

    @Query("select a.name from Application a")
    List<String> findAllNames();
}
