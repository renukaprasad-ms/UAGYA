package com.example.user_service.service.user;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user_service.dto.user.UserCreateRequest;
import com.example.user_service.entity.Application;
import com.example.user_service.entity.EmailOtp;
import com.example.user_service.entity.Role;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserApp;
import com.example.user_service.entity.enums.UserStatus;
import com.example.user_service.exception.BadRequestException;
import com.example.user_service.exception.InternalServerException;
import com.example.user_service.repository.EmailOtpRepository;
import com.example.user_service.repository.RoleRepository;
import com.example.user_service.repository.UserAppRepository;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.auth.EmailService;
import com.example.user_service.utils.bloom.EmailBloomFilter;
import com.example.user_service.utils.Otp;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final Otp otpUtil;
    private final EmailService email;
    private final EmailBloomFilter emailBloomFilter;
    private final UserAppRepository userAppRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository, Otp otpUtil,
            EmailOtpRepository emailOtpRepository, EmailService email, EmailBloomFilter emailBloomFilter, UserAppRepository userAppRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpUtil = otpUtil;
        this.emailOtpRepository = emailOtpRepository;
        this.email = email;
        this.emailBloomFilter = emailBloomFilter;
        this.userAppRepository = userAppRepository;
    }

    @Transactional
    public User createUser(UserCreateRequest request, Application app) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("User already exists with this email");
        }

        Role role = roleRepository.findByrole("USER").orElseThrow(() -> new InternalServerException("Role not found"));

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        emailBloomFilter.add(savedUser.getEmail());


        UserApp userApp = new UserApp();

        userApp.setUser(user);
        userApp.setApp(app);
        userApp.setRole(role);
        userApp.setIsAppAccessEnabled(true);
        userApp.setStatus(UserStatus.ACTIVE);

        userAppRepository.save(userApp);

        String otp = otpUtil.generateOtp();

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setUser(savedUser);
        emailOtp.setOtp(passwordEncoder.encode(otp));

        emailOtpRepository.save(emailOtp);
        email.sendOtpEmail(savedUser.getEmail(), otp);

        return savedUser;
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

    public boolean checkEmail(String email, Application app) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("email is required");
        }

        String normalizedEmail = normalizeEmail(email);
        if (!emailBloomFilter.mightContain(normalizedEmail)) {
            return false;
        }

        return userRepository.existsByEmailIgnoreCase(normalizedEmail);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
