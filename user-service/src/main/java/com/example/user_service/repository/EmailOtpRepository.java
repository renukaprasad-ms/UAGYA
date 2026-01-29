package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.User;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findByUser(User user);
    Optional<EmailOtp> findByUserAndUsedFalse(User user);
    void deleteByUser(User user);
}
