package com.example.user_service.service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.user_service.dto.user.UserCreateRequest;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.exception.InternalServerException;
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

    public UserService(UserRepository userRepository, RoleRepository roleRepository, Otp otpUtil,
            EmailOtpRepository emailOtpRepository, EmailService email) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpUtil = otpUtil;
        this.emailOtpRepository = emailOtpRepository;
        this.email = email;
    }

    public User createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        Role role = roleRepository.findByrole("user")
                .orElseThrow(() -> new InternalServerException("Default role USER not configured"));

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        String otp = otpUtil.generateOtp();

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(savedUser);
        emailOtp.setOtp(otp);

        emailOtpRepository.save(emailOtp);
        email.sendOtpEmail(savedUser.getEmail(), otp);

        return savedUser;
    }
}
