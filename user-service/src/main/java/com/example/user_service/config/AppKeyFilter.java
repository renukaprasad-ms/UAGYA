package com.example.user_service.config;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.user_service.entity.Application;
import com.example.user_service.repository.ApplicationRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AppKeyFilter extends OncePerRequestFilter {

    private final ApplicationRepository applicationRepository;

    public AppKeyFilter(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return path.startsWith("/api/app") || path.startsWith("/health-check");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String appKey = request.getHeader("X-APP-KEY");

        if (appKey == null || appKey.isBlank()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");

            response.getWriter().write("""
                        {
                          "success": false,
                          "status_code": 400,
                          "error": "App Key is missing"
                        }
                    """);
            return;
        }

        Application app = applicationRepository.findByAppKey(appKey).orElse(null);

        if (app == null || !app.getIsActive()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            response.getWriter().write("""
                        {
                          "success": false,
                          "status_code": 401,
                          "error": "Invalid App Key"
                        }
                    """);
            return;
        }

        request.setAttribute("APPLICATION", app);
        filterChain.doFilter(request, response);
    }
}
