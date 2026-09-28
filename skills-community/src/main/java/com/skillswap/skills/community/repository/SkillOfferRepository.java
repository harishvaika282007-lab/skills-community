package com.skillswap.skills.community.repository;

import com.skillswap.skills.community.entity.SkillOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SkillOffer.
 * Inherits all standard CRUD operations from JpaRepository.
 */
@Repository
public interface SkillOfferRepository extends JpaRepository<SkillOffer, Long> {
    // Recent skill offers for dashboard
    java.util.List<SkillOffer> findTop5ByOrderByIdDesc();

    // All skill offers sorted newest first
    java.util.List<SkillOffer> findAllByOrderByIdDesc();

    // Search by title/name or description
    java.util.List<SkillOffer> findBySkillNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String skillName, String description);

    // Find offers by provider member ID
    java.util.List<SkillOffer> findByProviderId(Long providerId);

    // Category aggregation for dashboard charts
    @org.springframework.data.jpa.repository.Query("SELECT s.category, COUNT(s) FROM SkillOffer s GROUP BY s.category")
    java.util.List<Object[]> countGroupByCategory();
}
