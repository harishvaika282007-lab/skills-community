package com.skillswap.skills.community.service;

import com.skillswap.skills.community.entity.CreditLedger;
import com.skillswap.skills.community.entity.Member;
import com.skillswap.skills.community.exception.ResourceNotFoundException;
import com.skillswap.skills.community.repository.CreditLedgerRepository;
import com.skillswap.skills.community.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for credit-related queries.
 * Actual credit transfers are done inside SessionRequestService.
 */
@Service
public class CreditService {

    private final MemberRepository memberRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    public CreditService(MemberRepository memberRepository,
                         CreditLedgerRepository creditLedgerRepository) {
        this.memberRepository = memberRepository;
        this.creditLedgerRepository = creditLedgerRepository;
    }

    /**
     * Return the current credit balance of a member.
     */
    public int getBalance(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        return member.getCreditBalance();
    }

    /**
     * Return all ledger entries where the member participated
     * (either as requester or provider).
     */
    public List<CreditLedger> getLedger(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));
        // Pass the same member object for both sides of the OR query
        return creditLedgerRepository.findByRequesterOrProvider(member, member);
    }

    /** Return all credit transactions across the entire system (sorted newest first) */
    public List<CreditLedger> getAllTransactions() {
        return creditLedgerRepository.findAllByOrderByIdDesc();
    }

    /** Return recent 5 credit transactions for dashboard */
    public List<CreditLedger> getRecentTransactions() {
        return creditLedgerRepository.findTop5ByOrderByIdDesc();
    }

    /** Direct DB sum of total credits circulating */
    public Long getTotalCreditsInCirculation() {
        return memberRepository.sumTotalCredits();
    }

    /** Direct DB sum of total hours/credits transferred */
    public Long getTotalCreditsTransferred() {
        return creditLedgerRepository.sumTotalHoursTransferred();
    }
}
