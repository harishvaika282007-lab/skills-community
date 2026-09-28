package com.skillswap.skills.community.service;

import com.skillswap.skills.community.dto.MemberRequest;
import com.skillswap.skills.community.entity.Member;
import com.skillswap.skills.community.exception.BusinessException;
import com.skillswap.skills.community.exception.ResourceNotFoundException;
import com.skillswap.skills.community.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for Member operations.
 * Contains all business logic; the controller only delegates here.
 */
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    // Constructor injection – preferred over @Autowired for easier testing
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Register a new member.
     * Throws BusinessException if the email is already in use.
     */
    public Member createMember(MemberRequest request) {
        // Business rule: email must be unique
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("A member with email '" + request.getEmail() + "' already exists.");
        }

        Member member = new Member();
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        // creditBalance defaults to 10 in the entity – no need to set it here

        return memberRepository.save(member);
    }

    /** Return all registered members. */
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Find a single member by ID.
     * Throws ResourceNotFoundException if not found.
     */
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    /**
     * Update an existing member's profile (name, email).
     */
    public Member updateMember(Long id, MemberRequest request) {
        Member member = getMemberById(id);

        // Check if email changed and is already taken by someone else
        if (!member.getEmail().equalsIgnoreCase(request.getEmail()) &&
                memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("A member with email '" + request.getEmail() + "' already exists.");
        }

        member.setName(request.getName());
        member.setEmail(request.getEmail());
        return memberRepository.save(member);
    }

    /**
     * Delete member if they have no active dependencies.
     */
    public void deleteMember(Long id) {
        Member member = getMemberById(id);
        memberRepository.delete(member);
    }

    /** Get recent 5 members for dashboard */
    public List<Member> getRecentMembers() {
        return memberRepository.findTop5ByOrderByIdDesc();
    }
}
