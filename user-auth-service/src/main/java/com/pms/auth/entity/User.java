package com.pms.auth.entity;

// These are JPA annotations — they tell Spring how to map this class to DB table
import jakarta.persistence.*;
import lombok.Data;

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
}