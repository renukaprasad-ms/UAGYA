package com.example.billing_service.config;

import com.example.billing_service.security.CustomAccessDeniedHandler;
import com.example.billing_service.security.CustomAuthenticationEntryPoint;
import com.example.billing_service.utils.RsaKeyLoader;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.jwt.*;

import java.security.interfaces.RSAPublicKey;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(oauth -> oauth.bearerTokenResolver(new CookieJwtTokenResolver())
                        .jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        RSAPublicKey publicKey = RsaKeyLoader.loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
