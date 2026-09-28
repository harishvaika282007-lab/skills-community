package com.skillswap.skills.community.repository;

import com.skillswap.skills.community.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Member.
 * JpaRepository provides save(), findById(), findAll(), deleteById(), etc. for free.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // Check whether an email is already registered (used to prevent duplicates)
    boolean existsByEmail(String email);

    // Find a member by their email address
    Optional<Member> findByEmail(String email);

    // Recent members for dashboard
    java.util.List<Member> findTop5ByOrderByIdDesc();

    // All members sorted newest first
    java.util.List<Member> findAllByOrderByIdDesc();

    // Direct DB sum of all credit balances for dashboard
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(m.creditBalance), 0) FROM Member m")
    Long sumTotalCredits();
}
