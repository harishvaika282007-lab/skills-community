package com.skillswap.skills.community.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a community member who can offer and request skills.
 * Each member starts with an initial credit balance (e.g., 10 credits).
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member's full name – cannot be blank
    @NotBlank(message = "Name cannot be empty")
    @Column(nullable = false)
    private String name;

    // Unique email address for the member
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    // Time-credit balance: increases when providing skills, decreases when consuming skills
    @jakarta.validation.constraints.Min(value = 0, message = "Credit balance cannot be negative")
    @Column(nullable = false)
    private int creditBalance = 10; // every new member starts with 10 credits

    @Column(nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
    }
}
