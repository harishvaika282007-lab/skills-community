package com.skillswap.skills.community.service;

import com.skillswap.skills.community.entity.CreditLedger;
import com.skillswap.skills.community.entity.Member;
import com.skillswap.skills.community.entity.SessionRequest;
import com.skillswap.skills.community.entity.SessionStatus;
import com.skillswap.skills.community.entity.SkillOffer;
import com.skillswap.skills.community.repository.CreditLedgerRepository;
import com.skillswap.skills.community.repository.MemberRepository;
import com.skillswap.skills.community.repository.SessionRequestRepository;
import com.skillswap.skills.community.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service dedicated to efficient dashboard aggregations and summaries.
 * Uses direct database count() and sum() queries to avoid in-memory overhead.
 */
@Service
public class DashboardService {

    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final SessionRequestRepository sessionRequestRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    public DashboardService(MemberRepository memberRepository,
                            SkillOfferRepository skillOfferRepository,
                            SessionRequestRepository sessionRequestRepository,
                            CreditLedgerRepository creditLedgerRepository) {
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.sessionRequestRepository = sessionRequestRepository;
        this.creditLedgerRepository = creditLedgerRepository;
    }

    /**
     * High-level metrics for dashboard cards:
     * Total Members, Total Skill Offers, Pending/Confirmed/Completed/Rejected sessions,
     * Total Credits in Circulation, and Total Credits Exchanged.
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalMembers = memberRepository.count();
        long totalSkills = skillOfferRepository.count();

        long pendingSessions = sessionRequestRepository.countByStatus(SessionStatus.PENDING);
        long confirmedSessions = sessionRequestRepository.countByStatus(SessionStatus.CONFIRMED);
        long completedSessions = sessionRequestRepository.countByStatus(SessionStatus.COMPLETED);
        long rejectedSessions = sessionRequestRepository.countByStatus(SessionStatus.REJECTED);
        long totalSessions = sessionRequestRepository.count();

        Long totalCreditsInCirculation = memberRepository.sumTotalCredits();
        Long totalCreditsTransferred = creditLedgerRepository.sumTotalHoursTransferred();

        summary.put("totalMembers", totalMembers);
        summary.put("totalSkillOffers", totalSkills);
        summary.put("totalSessions", totalSessions);
        summary.put("pendingSessions", pendingSessions);
        summary.put("confirmedSessions", confirmedSessions);
        summary.put("completedSessions", completedSessions);
        summary.put("rejectedSessions", rejectedSessions);
        summary.put("totalCreditsInCirculation", totalCreditsInCirculation != null ? totalCreditsInCirculation : 0L);
        summary.put("totalCreditsTransferred", totalCreditsTransferred != null ? totalCreditsTransferred : 0L);

        return summary;
    }

    /** Recent 5 members */
    public List<Member> getRecentMembers() {
        return memberRepository.findTop5ByOrderByIdDesc();
    }

    /** Recent 5 skills */
    public List<SkillOffer> getRecentSkills() {
        return skillOfferRepository.findTop5ByOrderByIdDesc();
    }

    /** Recent 5 sessions */
    public List<SessionRequest> getRecentSessions() {
        return sessionRequestRepository.findTop5ByOrderByIdDesc();
    }

    /** Credit overview with stats and recent transactions */
    public Map<String, Object> getCreditSummary() {
        Map<String, Object> creditData = new LinkedHashMap<>();
        creditData.put("totalCreditsInCirculation", memberRepository.sumTotalCredits());
        creditData.put("totalCreditsTransferred", creditLedgerRepository.sumTotalHoursTransferred());
        creditData.put("totalTransactionsCount", creditLedgerRepository.count());
        creditData.put("recentTransactions", creditLedgerRepository.findTop5ByOrderByIdDesc());
        return creditData;
    }

    /**
     * Aggregated distribution data for visual charts:
     * - Session status breakdown
     * - Skill categories breakdown
     */
    public Map<String, Object> getActivity() {
        Map<String, Object> activity = new LinkedHashMap<>();

        // Sessions distribution by status
        Map<String, Long> statusDistribution = new LinkedHashMap<>();
        for (SessionStatus status : SessionStatus.values()) {
            statusDistribution.put(status.name(), sessionRequestRepository.countByStatus(status));
        }
        activity.put("sessionStatusDistribution", statusDistribution);

        // Skills distribution by category
        Map<String, Long> categoryDistribution = new LinkedHashMap<>();
        List<Object[]> categoryCounts = skillOfferRepository.countGroupByCategory();
        for (Object[] row : categoryCounts) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            categoryDistribution.put(category != null ? category : "General", count);
        }
        activity.put("skillCategoryDistribution", categoryDistribution);

        return activity;
    }

    /**
     * Seeds rich demo data for instant dashboard demonstration:
     * - 4 Members (Alice, Bob, Carol, David)
     * - 5 Skills across different categories
     * - 3 Sessions with different lifecycle states (PENDING, CONFIRMED, COMPLETED)
     * - 1 CreditLedger record with balance transfer
     */
    @org.springframework.transaction.annotation.Transactional
    public Map<String, Object> seedSampleData() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. Create Members if not already present
        Member alice = memberRepository.findByEmail("alice@skillswap.com").orElseGet(() -> {
            Member m = new Member();
            m.setName("Alice Kumar");
            m.setEmail("alice@skillswap.com");
            m.setCreditBalance(12); // starts with 10 + earned 2
            return memberRepository.save(m);
        });

        Member bob = memberRepository.findByEmail("bob@skillswap.com").orElseGet(() -> {
            Member m = new Member();
            m.setName("Bob Sharma");
            m.setEmail("bob@skillswap.com");
            m.setCreditBalance(10);
            return memberRepository.save(m);
        });

        Member carol = memberRepository.findByEmail("carol@skillswap.com").orElseGet(() -> {
            Member m = new Member();
            m.setName("Carol Singh");
            m.setEmail("carol@skillswap.com");
            m.setCreditBalance(8); // spent 2 on David's session
            return memberRepository.save(m);
        });

        Member david = memberRepository.findByEmail("david@skillswap.com").orElseGet(() -> {
            Member m = new Member();
            m.setName("David Miller");
            m.setEmail("david@skillswap.com");
            m.setCreditBalance(10);
            return memberRepository.save(m);
        });

        // 2. Create Skill Offers if skill table is empty
        if (skillOfferRepository.count() == 0) {
            SkillOffer s1 = new SkillOffer();
            s1.setSkillName("Java Spring Boot Tutoring");
            s1.setCategory("Technology");
            s1.setDescription("Learn REST APIs, Spring Data JPA, Hibernate, and MySQL backend development.");
            s1.setHoursAvailable(10);
            s1.setProvider(alice);
            skillOfferRepository.save(s1);

            SkillOffer s2 = new SkillOffer();
            s2.setSkillName("Python for Data Science");
            s2.setCategory("Technology");
            s2.setDescription("Hands-on Pandas, NumPy, data cleaning, and machine learning fundamentals.");
            s2.setHoursAvailable(8);
            s2.setProvider(bob);
            skillOfferRepository.save(s2);

            SkillOffer s3 = new SkillOffer();
            s3.setSkillName("Authentic Italian Cooking");
            s3.setCategory("Cooking");
            s3.setDescription("Learn handmade pasta, traditional sauces, risotto, and tiramisu from scratch.");
            s3.setHoursAvailable(6);
            s3.setProvider(carol);
            skillOfferRepository.save(s3);

            SkillOffer s4 = new SkillOffer();
            s4.setSkillName("Conversational Spanish");
            s4.setCategory("Languages");
            s4.setDescription("Interactive speaking practice, everyday vocabulary, and travel fluency.");
            s4.setHoursAvailable(12);
            s4.setProvider(david);
            skillOfferRepository.save(s4);

            SkillOffer s5 = new SkillOffer();
            s5.setSkillName("Acoustic Guitar Lessons");
            s5.setCategory("Music");
            s5.setDescription("Master basic chords, fingerpicking patterns, rhythm, and your favorite songs.");
            s5.setHoursAvailable(5);
            s5.setProvider(alice);
            skillOfferRepository.save(s5);
        }

        // 3. Create Sample Sessions if session table is empty
        if (sessionRequestRepository.count() == 0 && skillOfferRepository.count() >= 3) {
            List<SkillOffer> skills = skillOfferRepository.findAll();

            // Session 1: Bob requests Alice's Java Tutoring -> CONFIRMED
            SessionRequest req1 = new SessionRequest();
            req1.setRequester(bob);
            req1.setSkillOffer(skills.get(0));
            req1.setHoursRequested(2);
            req1.setStatus(SessionStatus.CONFIRMED);
            req1.setNotes("Looking forward to learning JPA relationships!");
            sessionRequestRepository.save(req1);

            // Session 2: Carol requests David's Spanish -> COMPLETED + LEDGER
            SessionRequest req2 = new SessionRequest();
            req2.setRequester(carol);
            req2.setSkillOffer(skills.get(3));
            req2.setHoursRequested(2);
            req2.setActualHoursDelivered(2);
            req2.setStatus(SessionStatus.COMPLETED);
            req2.setCompletedAt(java.time.LocalDateTime.now().minusHours(2));
            req2.setNotes("Great interactive session on travel phrases.");
            SessionRequest savedReq2 = sessionRequestRepository.save(req2);

            // Record in Credit Ledger
            CreditLedger ledger = new CreditLedger();
            ledger.setRequester(carol);
            ledger.setProvider(david);
            ledger.setSessionRequest(savedReq2);
            ledger.setHours(2);
            ledger.setTransactionType("SESSION_COMPLETION");
            ledger.setDescription("Skill exchange: " + skills.get(3).getSkillName() + " (2 hours)");
            ledger.setRequesterBalanceAfter(carol.getCreditBalance());
            ledger.setProviderBalanceAfter(david.getCreditBalance());
            creditLedgerRepository.save(ledger);

            // Session 3: David requests Carol's Cooking -> PENDING
            SessionRequest req3 = new SessionRequest();
            req3.setRequester(david);
            req3.setSkillOffer(skills.get(2));
            req3.setHoursRequested(3);
            req3.setStatus(SessionStatus.PENDING);
            req3.setNotes("Want to learn how to make fresh gnocchi.");
            sessionRequestRepository.save(req3);
        }

        result.put("status", "SUCCESS");
        result.put("message", "Sample community data seeded successfully! 4 members, 5 skills, and 3 active sessions loaded.");
        return result;
    }
}
