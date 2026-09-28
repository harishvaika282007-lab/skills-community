package com.skillswap.skills.community.service;

import com.skillswap.skills.community.dto.SessionRequestDto;
import com.skillswap.skills.community.entity.*;
import com.skillswap.skills.community.exception.BusinessException;
import com.skillswap.skills.community.exception.ResourceNotFoundException;
import com.skillswap.skills.community.repository.CreditLedgerRepository;
import com.skillswap.skills.community.repository.MemberRepository;
import com.skillswap.skills.community.repository.SessionRequestRepository;
import com.skillswap.skills.community.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service class that contains all business logic for session requests
 * and credit transfers between members.
 *
 * Key rules enforced here:
 *  1. Requester must have enough credits before creating a request.
 *  2. Credits are NOT moved on request creation — only on COMPLETED.
 *  3. Only a PENDING request can be confirmed.
 *  4. Only a CONFIRMED request can be completed.
 *  5. A COMPLETED session cannot be completed again.
 */
@Service
public class SessionRequestService {

    private final SessionRequestRepository sessionRequestRepository;
    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    public SessionRequestService(SessionRequestRepository sessionRequestRepository,
                                 MemberRepository memberRepository,
                                 SkillOfferRepository skillOfferRepository,
                                 CreditLedgerRepository creditLedgerRepository) {
        this.sessionRequestRepository = sessionRequestRepository;
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.creditLedgerRepository = creditLedgerRepository;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE a new session request
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public SessionRequest createRequest(SessionRequestDto dto) {

        // 1. Load the requester
        Member requester = memberRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requester not found with id: " + dto.getRequesterId()));

        // 2. Load the skill offer
        SkillOffer skillOffer = skillOfferRepository.findById(dto.getSkillOfferId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Skill offer not found with id: " + dto.getSkillOfferId()));

        // 3. A member cannot request their own skill
        if (skillOffer.getProvider().getId().equals(requester.getId())) {
            throw new BusinessException("You cannot request your own skill offer.");
        }

        // 4. Requester must have enough credits
        if (requester.getCreditBalance() < dto.getHoursRequested()) {
            throw new BusinessException(
                    "Insufficient credits. You have " + requester.getCreditBalance()
                    + " credits but need " + dto.getHoursRequested() + ".");
        }

        // 5. Build and save the request — status defaults to PENDING
        SessionRequest request = new SessionRequest();
        request.setRequester(requester);
        request.setSkillOffer(skillOffer);
        request.setHoursRequested(dto.getHoursRequested());
        // Note: credits are NOT transferred here (business rule #6)

        return sessionRequestRepository.save(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONFIRM a request (provider accepts it)
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public SessionRequest confirmRequest(Long requestId) {
        SessionRequest request = getRequestById(requestId);

        // Only PENDING requests can be confirmed
        if (request.getStatus() != SessionStatus.PENDING) {
            throw new BusinessException(
                    "Only PENDING requests can be confirmed. Current status: " + request.getStatus());
        }

        request.setStatus(SessionStatus.CONFIRMED);
        return sessionRequestRepository.save(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COMPLETE a session — this is where credits are transferred
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public SessionRequest completeRequest(Long requestId) {
        return completeRequest(requestId, null);
    }

    @Transactional
    public SessionRequest completeRequest(Long requestId, Integer actualHours) {
        SessionRequest request = getRequestById(requestId);

        // Only CONFIRMED requests can be completed (prevents double-completion)
        if (request.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException("This session has already been completed. Credits cannot be transferred again.");
        }
        if (request.getStatus() != SessionStatus.CONFIRMED) {
            throw new BusinessException(
                    "Only CONFIRMED requests can be completed. Current status: " + request.getStatus());
        }

        Member requester = request.getRequester();
        Member provider  = request.getSkillOffer().getProvider();

        // Determine actual hours delivered (defaults to requested hours if not specified)
        int hoursToTransfer = (actualHours != null && actualHours > 0) ? actualHours : request.getHoursRequested();

        // Re-check credit balance at completion time (balance could have changed)
        if (requester.getCreditBalance() < hoursToTransfer) {
            throw new BusinessException(
                    "Requester no longer has enough credits. Balance: "
                    + requester.getCreditBalance() + ", required: " + hoursToTransfer);
        }

        // ── Transfer credits ──────────────────────────────────────────────────
        requester.setCreditBalance(requester.getCreditBalance() - hoursToTransfer);  // deduct from requester
        provider.setCreditBalance(provider.getCreditBalance() + hoursToTransfer);    // add to provider

        memberRepository.save(requester);
        memberRepository.save(provider);

        // ── Update session details ───────────────────────────────────────────
        request.setActualHoursDelivered(hoursToTransfer);
        request.setStatus(SessionStatus.COMPLETED);
        request.setCompletedAt(java.time.LocalDateTime.now());
        SessionRequest savedRequest = sessionRequestRepository.save(request);

        // ── Record the transaction in the ledger ─────────────────────────────
        CreditLedger ledger = new CreditLedger();
        ledger.setRequester(requester);
        ledger.setProvider(provider);
        ledger.setSessionRequest(savedRequest);
        ledger.setHours(hoursToTransfer);
        ledger.setTransactionType("SESSION_COMPLETION");
        ledger.setDescription("Skill exchange: " + request.getSkillOffer().getSkillName() + " (" + hoursToTransfer + " hours)");
        ledger.setRequesterBalanceAfter(requester.getCreditBalance());
        ledger.setProviderBalanceAfter(provider.getCreditBalance());
        creditLedgerRepository.save(ledger);

        return savedRequest;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REJECT a request
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public SessionRequest rejectRequest(Long requestId) {
        SessionRequest request = getRequestById(requestId);
        if (request.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException("Cannot reject an already completed session.");
        }
        request.setStatus(SessionStatus.REJECTED);
        return sessionRequestRepository.save(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CANCEL a request
    // ─────────────────────────────────────────────────────────────────────────
    @Transactional
    public SessionRequest cancelRequest(Long requestId) {
        SessionRequest request = getRequestById(requestId);
        if (request.getStatus() == SessionStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel an already completed session.");
        }
        request.setStatus(SessionStatus.CANCELLED);
        return sessionRequestRepository.save(request);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ operations
    // ─────────────────────────────────────────────────────────────────────────
    public List<SessionRequest> getAllRequests() {
        return sessionRequestRepository.findAllByOrderByIdDesc();
    }

    public SessionRequest getRequestById(Long id) {
        return sessionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session request not found with id: " + id));
    }

    public List<SessionRequest> getRecentSessions() {
        return sessionRequestRepository.findTop5ByOrderByIdDesc();
    }
}
