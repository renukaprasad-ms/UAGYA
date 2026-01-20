package com.example.billing_service.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.billing_service.utils.Response;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExecptionHandler {
    private final Response responseUtil;

    @ExceptionHandler(BadRequestExecption.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestExecption ex) {
        return ResponseEntity.status(400).body(
                responseUtil.response(false, 400, null, null, ex.getMessage()));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Map<String, Object>> handleInternalERror(InternalServerException ex) {
        return ResponseEntity.status(500).body(
                responseUtil.response(false, 500, null, null, ex.getMessage()));
    }


}
