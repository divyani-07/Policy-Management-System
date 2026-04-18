package com.pms.auth.entity;

// These are JPA annotations — they tell Spring how to map this class to DB table
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tbl_users")
public class User {

    @Id                          // This field is the Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment (1,2,3...)
    private Long userId;

    @Column(nullable = false, unique = true)  // NOT NULL + UNIQUE in DB
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;   // We store BCrypt hash, NOT plain password

    @Enumerated(EnumType.STRING)   // Store enum as text ("ADMIN") not number (0)
    private Role role;             // ADMIN, AGENT, CUSTOMER

    private Boolean isActive = true;  // Default: account is active

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;   // set once on insert, never changed

    private LocalDateTime updatedAt;   // updated every time record changes

    // Runs automatically before INSERT — sets createdAt
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // Runs automatically before UPDATE — sets updatedAt
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}