package com.skillswap.skills.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a skill that a member is offering to the community.
 * Other members can request sessions for this skill.
 */
@Entity
@Table(name = "skill_offers")
@Getter
@Setter
@NoArgsConstructor
public class SkillOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Name of the skill being offered (e.g., "Java Programming")
    @NotBlank(message = "Skill name cannot be empty")
    @Column(nullable = false)
    private String skillName;

    // A brief description of what the provider will teach / help with
    @Column(length = 500)
    private String description;

    // How many hours the provider is willing to offer in total
    @Min(value = 1, message = "Hours available must be at least 1")
    @Column(nullable = false)
    private int hoursAvailable;

    // Category (e.g. Technology, Academics, Cooking, Languages, Fitness, Music, Other)
    @Column(nullable = false)
    private String category = "Technology";

    // Status: ACTIVE or INACTIVE
    @Column(nullable = false)
    private String status = "ACTIVE";

    // The member who is providing this skill (Many skill offers → One member)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private Member provider;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }
}
