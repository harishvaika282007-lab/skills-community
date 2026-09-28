package com.skillswap.skills.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a request from a member (requester) to attend
 * a session for a specific SkillOffer.
 *
 * Credits are NOT moved when the request is created.
 * Credits are moved only when the status becomes COMPLETED.
 */
@Entity
@Table(name = "session_requests")
@Getter
@Setter
@NoArgsConstructor
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The member who wants to learn / use the skill
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    // The skill offer being requested
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_offer_id", nullable = false)
    private SkillOffer skillOffer;

    // Number of hours the requester is asking for (must be > 0)
    @Min(value = 1, message = "Requested hours must be at least 1")
    @Column(nullable = false)
    private int hoursRequested;

    // Actual hours delivered when session completes (may equal hoursRequested)
    private Integer actualHoursDelivered;

    // Optional notes/remarks
    @Column(length = 500)
    private String notes;

    // Current lifecycle status of this session
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }
}
