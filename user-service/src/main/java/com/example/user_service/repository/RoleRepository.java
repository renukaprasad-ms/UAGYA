package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByrole(String role);

    boolean existsByRole(String role);
}
