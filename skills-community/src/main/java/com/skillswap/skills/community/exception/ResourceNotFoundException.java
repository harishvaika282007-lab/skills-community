package com.skillswap.skills.community.exception;

/**
 * Thrown when a requested resource (Member, SkillOffer, SessionRequest)
 * is not found in the database.
 * Maps to HTTP 404 Not Found via the GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
