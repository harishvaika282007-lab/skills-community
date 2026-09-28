package com.skillswap.skills.community.entity;

/**
 * Lifecycle states of a SessionRequest.
 *
 * PENDING   → request submitted, waiting for provider to confirm
 * CONFIRMED → provider accepted, session to be held
 * COMPLETED → session done, credits transferred
 * REJECTED  → provider declined the request
 */
public enum SessionStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    REJECTED,
    CANCELLED
}
