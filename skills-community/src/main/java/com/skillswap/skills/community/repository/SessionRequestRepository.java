package com.skillswap.skills.community.repository;

import com.skillswap.skills.community.entity.SessionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for SessionRequest.
 */
@Repository
public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {
    // Count sessions by lifecycle status (direct DB count, very fast)
    long countByStatus(com.skillswap.skills.community.entity.SessionStatus status);

    // Recent session requests for dashboard
    java.util.List<SessionRequest> findTop5ByOrderByIdDesc();

    // All session requests sorted newest first
    java.util.List<SessionRequest> findAllByOrderByIdDesc();

    // Filter by status
    java.util.List<SessionRequest> findByStatus(com.skillswap.skills.community.entity.SessionStatus status);

    // Filter by requester
    java.util.List<SessionRequest> findByRequesterId(Long requesterId);

    // Filter by provider
    java.util.List<SessionRequest> findBySkillOffer_Provider_Id(Long providerId);
}
