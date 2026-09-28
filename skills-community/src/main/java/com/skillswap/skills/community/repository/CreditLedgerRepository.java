package com.skillswap.skills.community.repository;

import com.skillswap.skills.community.entity.CreditLedger;
import com.skillswap.skills.community.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for CreditLedger.
 */
@Repository
public interface CreditLedgerRepository extends JpaRepository<CreditLedger, Long> {

    /**
     * Returns all ledger entries where the given member is either the
     * requester OR the provider – giving the full transaction history.
     */
    List<CreditLedger> findByRequesterOrProvider(Member requester, Member provider);

    // Recent transactions for dashboard
    List<CreditLedger> findTop5ByOrderByIdDesc();

    // All transactions sorted newest first
    List<CreditLedger> findAllByOrderByIdDesc();

    // Direct DB sum of all credits transferred across the system
    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(c.hours), 0) FROM CreditLedger c")
    Long sumTotalHoursTransferred();
}
