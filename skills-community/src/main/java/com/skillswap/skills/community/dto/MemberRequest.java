package com.skillswap.skills.community.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for creating a new Member.
 * Only the fields the client needs to send are included.
 * creditBalance is NOT included – it is set automatically by the service.
 */
@Getter
@Setter
public class MemberRequest {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be a valid email address")
    private String email;
}
