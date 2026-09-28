package com.skillswap.skills.community.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler.
 * All controllers automatically benefit from these handlers.
 * Every error is returned as a consistent JSON structure.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        String errorCode = "RESOURCE_NOT_FOUND";
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("session")) {
            errorCode = "SESSION_NOT_FOUND";
        } else if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("member")) {
            errorCode = "MEMBER_NOT_FOUND";
        } else if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("skill")) {
            errorCode = "SKILL_NOT_FOUND";
        }
        return buildResponse(HttpStatus.NOT_FOUND, errorCode, ex.getMessage());
    }

    // ── 400 Bad Request (business rule violations) ─────────────────────────────
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        String errorCode = "BUSINESS_RULE_VIOLATION";
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("insufficient credits")) {
            errorCode = "INSUFFICIENT_CREDITS";
        }
        return buildResponse(HttpStatus.BAD_REQUEST, errorCode, ex.getMessage());
    }

    // ── 400 Bad Request (Bean Validation failures from @Valid) ─────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        // Collect all field-level validation errors into a map
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("details", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── 500 Catch-all ──────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred: " + ex.getMessage());
    }

    // Helper: build a standard error response body with custom error code
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", errorCode != null ? errorCode : status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, status.getReasonPhrase(), message);
    }
}
