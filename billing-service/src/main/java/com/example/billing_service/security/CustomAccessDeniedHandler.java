package com.example.billing_service.security;

import com.example.billing_service.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final Response responseUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAccessDeniedHandler(Response responseUtil) {
        this.responseUtil = responseUtil;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        Map<String, Object> body = responseUtil.response(
                false,
                HttpServletResponse.SC_FORBIDDEN,
                null,
                null,
                "Access denied"
        );

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
