package com.example.user_service.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.user_service.utils.Response;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Response responseUtil;

    public GlobalExceptionHandler(Response responseUtil) {
        this.responseUtil = responseUtil;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {

        return ResponseEntity.status(200).body(
                responseUtil.response(false, 400, null, null, ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(200).body(responseUtil.response(false, 400, null, null, ex.getMessage()));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<Map<String, Object>> handleInternalServer(RuntimeException ex) {
        return ResponseEntity.status(200).body(responseUtil.response(false, 500, null, null, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        StringBuilder errors = new StringBuilder();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {
                    if (errors.length() > 0) {
                        errors.append(", ");
                    }
                    errors.append(error.getDefaultMessage());
                });

        return ResponseEntity.badRequest().body(
                responseUtil.response(false, 400, null, null, errors.toString()));
    }
}
