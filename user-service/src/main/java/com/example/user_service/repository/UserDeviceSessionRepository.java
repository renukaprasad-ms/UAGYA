package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.Application;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserDeviceSession;

public interface UserDeviceSessionRepository extends JpaRepository<UserDeviceSession, Long>{
    Optional<UserDeviceSession> findByUserAndAppAndDeviceId(User user, Application app, String deviceId);
    long countByUserAndAppAndActiveTrue(User user, Application app);
}
