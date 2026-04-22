package com.pms.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// When user logs out, we store their token here
// JwtFilter checks this list — if token is here, reject it
@Data
@Entity
@Table(name = "tbl_token_blacklist")
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String token;          // The actual JWT string

    @Column(nullable = false)
    private LocalDateTime blacklistedAt;   // When was it blacklisted

    @Column(nullable = false)
    private LocalDateTime expiresAt;       // When does the token expire naturally
    // We store expiresAt so we can auto-delete expired tokens later (cleanup job)
}
