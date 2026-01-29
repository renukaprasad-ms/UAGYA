package com.example.user_service.utils.bloom;

import org.springframework.stereotype.Component;

@Component
public class AppNameBloomFilter extends BaseBloomFilter {

    public AppNameBloomFilter() {
        super(500_000, 3);
    }

    @Override
    protected String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
