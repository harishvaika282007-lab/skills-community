package com.skillswap.skills.community.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating a new SessionRequest.
 * The client sends: requesterId, skillOfferId, hoursRequested.
 */
@Getter
@Setter
public class SessionRequestDto {

    // ID of the member who wants to learn
    @NotNull(message = "Requester ID is required")
    private Long requesterId;

    // ID of the SkillOffer being requested
    @NotNull(message = "Skill Offer ID is required")
    private Long skillOfferId;

    // Number of hours being requested (must be positive)
    @Min(value = 1, message = "Requested hours must be at least 1")
    private int hoursRequested;
}
