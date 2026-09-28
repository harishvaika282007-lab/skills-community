package com.skillswap.skills.community.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Immutable record of every credit transfer.
 * One record is created each time a session is marked COMPLETED.
 *
 * transactionType will be "DEBIT" for the requester and "CREDIT" for the provider,
 * but since we store both sides in one row we use a descriptive string like
 * "SESSION_COMPLETION" to describe the event.
 */
@Entity
@Table(name = "credit_ledger")
@Getter
@Setter
@NoArgsConstructor
public class CreditLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member who requested (paid credits)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    // Member who provided the skill (received credits)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "provider_id", nullable = false)
    private Member provider;

    // Associated session request (if applicable)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_request_id")
    private SessionRequest sessionRequest;

    // How many hours / credits were transferred
    @Column(nullable = false)
    private int hours;

    // Describes the nature of the transaction (e.g. "SESSION_COMPLETION")
    @Column(nullable = false)
    private String transactionType;

    // Human-readable description
    private String description;

    // Requester balance after this transaction
    private Integer requesterBalanceAfter;

    // Provider balance after this transaction
    private Integer providerBalanceAfter;

    // Timestamp automatically set when this record is persisted
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Automatically fill createdAt before the entity is saved for the first time
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
