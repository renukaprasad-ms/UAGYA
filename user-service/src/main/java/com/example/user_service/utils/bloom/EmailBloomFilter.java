package com.example.user_service.utils.bloom;

import org.springframework.stereotype.Component;

@Component
public class EmailBloomFilter extends BaseBloomFilter {

    public EmailBloomFilter() {
        super(1_000_000, 3);
    }

    @Override
    protected String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
