package com.example.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email); 
}
