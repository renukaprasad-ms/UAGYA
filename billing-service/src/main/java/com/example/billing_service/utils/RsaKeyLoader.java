package com.example.billing_service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;

import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;

public class RsaKeyLoader {
    public static RSAPublicKey loadPublicKey() {
        try (InputStream input = new ClassPathResource("keys/public.key").getInputStream()) {
            return RsaKeyConverters.x509().convert(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load public key", ex);
        }
    }
}
