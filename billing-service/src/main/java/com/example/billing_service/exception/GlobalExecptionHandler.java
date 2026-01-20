package com.example.billing_service.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.billing_service.utils.Response;
import tools.jackson.databind.exc.InvalidFormatException;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(
                responseUtil.response(
                        false,
                        400,
                        fieldErrors,
                        null,
                        "Validation failed"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex) {

        return ResponseEntity.badRequest().body(
                responseUtil.response(
                        false,
                        400,
                        ex.getErrors(),
                        null,
                        ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        String errorMessage = "Invalid request body";

        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormat) {
            String field = getFieldName(invalidFormat);
            String message = buildInvalidFormatMessage(invalidFormat);
            if (field != null && !field.isBlank()) {
                fieldErrors.put(field, message);
            } else {
                errorMessage = message;
            }
        }

        return ResponseEntity.badRequest().body(
                responseUtil.response(
                        false,
                        400,
                        fieldErrors.isEmpty() ? null : fieldErrors,
                        null,
                        errorMessage));
    }

    private String getFieldName(InvalidFormatException ex) {
        if (ex.getPath() == null || ex.getPath().isEmpty()) {
            return null;
        }
        return ex.getPath().get(ex.getPath().size() - 1).getPropertyName();
    }

    private String buildInvalidFormatMessage(InvalidFormatException ex) {
        Object value = ex.getValue();
        Class<?> targetType = ex.getTargetType();
        if (targetType != null && targetType.isEnum()) {
            String allowedValues = Arrays.stream(targetType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Invalid value '" + value + "'. Allowed values: " + allowedValues;
        }
        return "Invalid value '" + value + "'";
    }
}
