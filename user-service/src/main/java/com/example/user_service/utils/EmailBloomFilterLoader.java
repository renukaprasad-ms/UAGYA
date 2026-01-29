package com.example.user_service.utils;

import org.springframework.stereotype.Component;

import com.example.user_service.repository.UserRepository;
import com.example.user_service.utils.bloom.EmailBloomFilter;

import jakarta.annotation.PostConstruct;

@Component
public class EmailBloomFilterLoader {
    private final UserRepository userRepository;
    private final EmailBloomFilter emailBloomFilter;

    public EmailBloomFilterLoader(UserRepository userRepository, EmailBloomFilter emailBloomFilter) {
        this.userRepository = userRepository;
        this.emailBloomFilter = emailBloomFilter;
    }

    @PostConstruct
    public void load() {
        userRepository.findAllEmails().forEach(emailBloomFilter::add);
    }
}
