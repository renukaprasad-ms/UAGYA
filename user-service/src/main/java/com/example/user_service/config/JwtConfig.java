package com.example.user_service.config;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    @Value("classpath:keys/private.key")
    private Resource privateKeyResource;

    @Value("classpath:keys/public.key")
    private Resource publicKeyResource;

    @Bean
    JwtEncoder jwtEncoder() {
        RSAPrivateKey privateKey = readPrivateKey(privateKeyResource);
        RSAPublicKey publicKey = readPublicKey(publicKeyResource);

        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    private RSAPrivateKey readPrivateKey(Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.pkcs8().convert(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load private key", ex);
        }
    }

    private RSAPublicKey readPublicKey(Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            return RsaKeyConverters.x509().convert(input);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load public key", ex);
        }
    }
}
