package com.skillswap.skills.community.controller;

import com.skillswap.skills.community.dto.SkillOfferRequest;
import com.skillswap.skills.community.entity.SkillOffer;
import com.skillswap.skills.community.service.SkillOfferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Skill endpoints.
 */
@RestController
@RequestMapping("/api/skills")
public class SkillOfferController {

    private final SkillOfferService skillOfferService;

    public SkillOfferController(SkillOfferService skillOfferService) {
        this.skillOfferService = skillOfferService;
    }

    /** POST /api/skills — Post a new skill offer */
    @PostMapping
    public ResponseEntity<SkillOffer> createSkillOffer(@Valid @RequestBody SkillOfferRequest request) {
        SkillOffer created = skillOfferService.createSkillOffer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** GET /api/skills — Browse all available skill offers */
    @GetMapping
    public ResponseEntity<List<SkillOffer>> getAllSkillOffers() {
        return ResponseEntity.ok(skillOfferService.getAllSkillOffers());
    }

    /** GET /api/skills/{id} — Get details of a specific skill offer */
    @GetMapping("/{id}")
    public ResponseEntity<SkillOffer> getSkillOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(skillOfferService.getSkillOfferById(id));
    }

    /** PUT /api/skills/{id} — Update an existing skill offer */
    @PutMapping("/{id}")
    public ResponseEntity<SkillOffer> updateSkillOffer(@PathVariable Long id, @Valid @RequestBody SkillOfferRequest request) {
        SkillOffer updated = skillOfferService.updateSkillOffer(id, request);
        return ResponseEntity.ok(updated);
    }

    /** DELETE /api/skills/{id} — Delete skill offer */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkillOffer(@PathVariable Long id) {
        skillOfferService.deleteSkillOffer(id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/skills/search?keyword=... — Search skill offers */
    @GetMapping("/search")
    public ResponseEntity<List<SkillOffer>> searchSkills(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(skillOfferService.searchSkills(keyword));
    }
}
