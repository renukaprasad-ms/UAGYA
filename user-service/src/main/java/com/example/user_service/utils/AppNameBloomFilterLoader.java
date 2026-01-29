package com.example.user_service.utils;

import org.springframework.stereotype.Component;

import com.example.user_service.repository.ApplicationRepository;
import com.example.user_service.utils.bloom.AppNameBloomFilter;

import jakarta.annotation.PostConstruct;

@Component
public class AppNameBloomFilterLoader {
    private final ApplicationRepository applicationRepository;
    private final AppNameBloomFilter appNameBloomFilter;

    public AppNameBloomFilterLoader(ApplicationRepository applicationRepository, AppNameBloomFilter appNameBloomFilter) {
        this.applicationRepository = applicationRepository;
        this.appNameBloomFilter = appNameBloomFilter;
    }

    @PostConstruct
    public void load() {
        applicationRepository.findAllNames().forEach(appNameBloomFilter::add);
    }
}
