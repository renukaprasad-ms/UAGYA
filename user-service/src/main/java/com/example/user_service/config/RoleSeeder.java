package com.example.user_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.user_service.entity.Role;
import com.example.user_service.repository.RoleRepository;

@Configuration
public class RoleSeeder {

    @Bean
    CommandLineRunner seedRoles(RoleRepository roleRepository) {
        return args -> {
            seedRoleIfNotExists(roleRepository, "user");
        };
    }

    private void seedRoleIfNotExists(
            RoleRepository roleRepository,
            String roleName) {
                
        if(!roleRepository.existsByRole(roleName)) {
            Role role = new Role();

        role.setRole(roleName);

        roleRepository.save(role);
        }
    }

}
