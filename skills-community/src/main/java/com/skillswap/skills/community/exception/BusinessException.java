package com.skillswap.skills.community.exception;

/**
 * Thrown when a business rule is violated.
 * Examples: insufficient credits, duplicate email, completing an already-completed session.
 * Maps to HTTP 400 Bad Request via the GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
