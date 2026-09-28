package com.skillswap.skills.community.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating a new SkillOffer.
 * The client sends: skillName, description, hoursAvailable, and providerId.
 */
@Getter
@Setter
public class SkillOfferRequest {

    @NotBlank(message = "Skill name cannot be empty")
    private String skillName;

    // Description is optional
    private String description;

    @Min(value = 1, message = "Hours available must be at least 1")
    private int hoursAvailable;

    // Optional category (e.g. Technology, Academics, Cooking, Languages, Fitness, Music, Other)
    private String category;

    // ID of the member who is providing this skill
    @NotNull(message = "Provider ID is required")
    private Long providerId;
}
