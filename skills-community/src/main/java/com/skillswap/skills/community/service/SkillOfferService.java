package com.skillswap.skills.community.service;

import com.skillswap.skills.community.dto.SkillOfferRequest;
import com.skillswap.skills.community.entity.Member;
import com.skillswap.skills.community.entity.SkillOffer;
import com.skillswap.skills.community.exception.ResourceNotFoundException;
import com.skillswap.skills.community.repository.MemberRepository;
import com.skillswap.skills.community.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for SkillOffer operations.
 */
@Service
public class SkillOfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final MemberRepository memberRepository;

    public SkillOfferService(SkillOfferRepository skillOfferRepository,
                             MemberRepository memberRepository) {
        this.skillOfferRepository = skillOfferRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Create a new skill offer.
     * The provider must be an existing member.
     */
    public SkillOffer createSkillOffer(SkillOfferRequest request) {
        // Verify the provider member exists
        Member provider = memberRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Provider member not found with id: " + request.getProviderId()));

        SkillOffer offer = new SkillOffer();
        offer.setSkillName(request.getSkillName());
        offer.setDescription(request.getDescription());
        offer.setHoursAvailable(request.getHoursAvailable());
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            offer.setCategory(request.getCategory());
        }
        offer.setProvider(provider);

        return skillOfferRepository.save(offer);
    }

    /** Return all skill offers (the community skill marketplace). */
    public List<SkillOffer> getAllSkillOffers() {
        return skillOfferRepository.findAllByOrderByIdDesc();
    }

    /**
     * Find a skill offer by ID.
     * Throws ResourceNotFoundException if not found.
     */
    public SkillOffer getSkillOfferById(Long id) {
        return skillOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill offer not found with id: " + id));
    }

    /** Update an existing skill offer */
    public SkillOffer updateSkillOffer(Long id, SkillOfferRequest request) {
        SkillOffer offer = getSkillOfferById(id);
        offer.setSkillName(request.getSkillName());
        offer.setDescription(request.getDescription());
        offer.setHoursAvailable(request.getHoursAvailable());
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            offer.setCategory(request.getCategory());
        }
        return skillOfferRepository.save(offer);
    }

    /** Delete or deactivate skill offer */
    public void deleteSkillOffer(Long id) {
        SkillOffer offer = getSkillOfferById(id);
        skillOfferRepository.delete(offer);
    }

    /** Search skills by keyword in name or description */
    public List<SkillOffer> searchSkills(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllSkillOffers();
        }
        return skillOfferRepository.findBySkillNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    /** Get recent 5 skills for dashboard */
    public List<SkillOffer> getRecentSkills() {
        return skillOfferRepository.findTop5ByOrderByIdDesc();
    }
}
