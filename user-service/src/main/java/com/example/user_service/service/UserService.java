package com.example.user_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.UserCreateRequest;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.utils.Otp;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final Otp otpUtil;
    private final EmailService email;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository, Otp otpUtil, EmailOtpRepository emailOtpRepository, EmailService email) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpUtil = otpUtil;
        this.emailOtpRepository = emailOtpRepository;
        this.email = email;
    }

    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exits");
        }

        Role role = roleRepository.findByrole("user")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User saved_user = userRepository.save(user);


        String otp = otpUtil.generateOtp();

        EmailOtp email_otp = new EmailOtp();
        email_otp.setOtp(otp);
        email_otp.setUser(saved_user);

        emailOtpRepository.save(email_otp);

        email.sendOtpEmail(user.getEmail(), otp);

        return saved_user;

    }
}
