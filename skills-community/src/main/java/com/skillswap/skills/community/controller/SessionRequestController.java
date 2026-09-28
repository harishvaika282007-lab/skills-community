package com.skillswap.skills.community.controller;

import com.skillswap.skills.community.dto.SessionRequestDto;
import com.skillswap.skills.community.entity.SessionRequest;
import com.skillswap.skills.community.service.SessionRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Session Request endpoints.
 * Handles creating, confirming, and completing session requests.
 */
@RestController
@RequestMapping({"/api/requests", "/api/session-requests"})
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    public SessionRequestController(SessionRequestService sessionRequestService) {
        this.sessionRequestService = sessionRequestService;
    }

    /** POST /api/requests (or /api/session-requests) — Submit a new session request */
    @PostMapping
    public ResponseEntity<SessionRequest> createRequest(@Valid @RequestBody SessionRequestDto dto) {
        SessionRequest created = sessionRequestService.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/requests — List all session requests */
    @GetMapping
    public ResponseEntity<List<SessionRequest>> getAllRequests() {
        return ResponseEntity.ok(sessionRequestService.getAllRequests());
    }

    /** GET /api/requests/{id} — Get a single session request */
    @GetMapping("/{id}")
    public ResponseEntity<SessionRequest> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.getRequestById(id));
    }

    /** PUT /api/requests/{id}/confirm — Provider confirms the session */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<SessionRequest> confirmRequest(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.confirmRequest(id));
    }

    /** PUT /api/requests/{id}/complete — Mark session done and transfer credits (supports optional actualHours) */
    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionRequest> completeRequest(
            @PathVariable Long id,
            @RequestParam(required = false) Integer actualHours) {
        return ResponseEntity.ok(sessionRequestService.completeRequest(id, actualHours));
    }

    /** PUT /api/requests/{id}/reject — Provider rejects the session request */
    @PutMapping("/{id}/reject")
    public ResponseEntity<SessionRequest> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.rejectRequest(id));
    }

    /** PUT /api/requests/{id}/cancel — Requester cancels the session request */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionRequest> cancelRequest(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.cancelRequest(id));
    }
}
