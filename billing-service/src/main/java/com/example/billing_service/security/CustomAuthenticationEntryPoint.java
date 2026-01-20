package com.example.billing_service.security;


import java.io.IOException;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.billing_service.utils.Response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;


@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Response responseUtil;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    )throws IOException {
         Map<String, Object> body = responseUtil.response(
                false,
                HttpServletResponse.SC_UNAUTHORIZED,
                null,
                null,
                "Unauthorized or invalid token"
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
