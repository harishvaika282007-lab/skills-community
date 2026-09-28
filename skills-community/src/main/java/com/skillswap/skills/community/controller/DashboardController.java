package com.skillswap.skills.community.controller;

import com.skillswap.skills.community.entity.Member;
import com.skillswap.skills.community.entity.SessionRequest;
import com.skillswap.skills.community.entity.SkillOffer;
import com.skillswap.skills.community.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller providing dedicated aggregation endpoints for the Dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** GET /api/dashboard/summary — Overall KPI metrics */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /** GET /api/dashboard/recent-members — Last 5 registered members */
    @GetMapping("/recent-members")
    public ResponseEntity<List<Member>> getRecentMembers() {
        return ResponseEntity.ok(dashboardService.getRecentMembers());
    }

    /** GET /api/dashboard/recent-skills — Last 5 posted skills */
    @GetMapping("/recent-skills")
    public ResponseEntity<List<SkillOffer>> getRecentSkills() {
        return ResponseEntity.ok(dashboardService.getRecentSkills());
    }

    /** GET /api/dashboard/recent-sessions — Last 5 session requests */
    @GetMapping("/recent-sessions")
    public ResponseEntity<List<SessionRequest>> getRecentSessions() {
        return ResponseEntity.ok(dashboardService.getRecentSessions());
    }

    /** GET /api/dashboard/credit-summary — Credit circulation and recent transfers */
    @GetMapping("/credit-summary")
    public ResponseEntity<Map<String, Object>> getCreditSummary() {
        return ResponseEntity.ok(dashboardService.getCreditSummary());
    }

    /** GET /api/dashboard/activity — Aggregated distribution data for charts */
    @GetMapping("/activity")
    public ResponseEntity<Map<String, Object>> getActivity() {
        return ResponseEntity.ok(dashboardService.getActivity());
    }

    /** POST /api/dashboard/seed-sample-data — Populate rich sample demo data */
    @org.springframework.web.bind.annotation.PostMapping("/seed-sample-data")
    public ResponseEntity<Map<String, Object>> seedSampleData() {
        return ResponseEntity.ok(dashboardService.seedSampleData());
    }
}
