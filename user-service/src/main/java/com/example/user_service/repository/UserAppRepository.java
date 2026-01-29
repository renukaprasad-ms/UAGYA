package com.example.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_service.entity.Application;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserApp;

public interface UserAppRepository extends JpaRepository<UserApp, Long>{
    Optional<UserApp> findByUser(User user);
    boolean existsByuserAndApp( User user, Application app );
}
