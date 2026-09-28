package com.skillswap.skills.community.controller;

import com.skillswap.skills.community.entity.CreditLedger;
import com.skillswap.skills.community.service.CreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for credit-related queries.
 * Exposes endpoints under both /api/members and /api/credits for maximum flexibility.
 */
@RestController
public class CreditController {

    private final CreditService creditService;

    public CreditController(CreditService creditService) {
        this.creditService = creditService;
    }

    /** GET /api/members/{id}/balance — Check a member's current credit balance */
    @GetMapping({"/api/members/{id}/balance", "/api/credits/balance/{id}"})
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long id) {
        int balance = creditService.getBalance(id);

        Map<String, Object> response = new HashMap<>();
        response.put("memberId", id);
        response.put("creditBalance", balance);
        return ResponseEntity.ok(response);
    }

    /** GET /api/members/{id}/ledger — Get full transaction history for a member */
    @GetMapping({"/api/members/{id}/ledger", "/api/credits/history/{id}"})
    public ResponseEntity<List<CreditLedger>> getMemberLedger(@PathVariable Long id) {
        return ResponseEntity.ok(creditService.getLedger(id));
    }

    /** GET /api/credits — View all credit transactions in the entire system */
    @GetMapping({"/api/credits", "/api/credits/ledger"})
    public ResponseEntity<List<CreditLedger>> getAllTransactions() {
        return ResponseEntity.ok(creditService.getAllTransactions());
    }
}
