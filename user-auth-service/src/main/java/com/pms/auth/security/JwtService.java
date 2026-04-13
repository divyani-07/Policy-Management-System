package com.pms.auth.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    // Reads the secret key from application.yml
    // jwt.secret=mySecretKey123... (must be at least 32 characters)
    @Value("${jwt.secret}")
    private String secretKey;

    // Reads token validity from application.yml
    // jwt.expiration=86400000 (24 hours in milliseconds)
    @Value("${jwt.expiration}")
    private long expirationMs;

    // ── Helper: convert secret string → cryptographic key ──────────────────
    private SecretKey getSigningKey() {
        // Base64 decode the secret string, then wrap it as a HMAC-SHA key
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
        // HMAC-SHA256 is the algorithm that "signs" our token
    }

    // ── GENERATE TOKEN ──────────────────────────────────────────────────────
    public String generateToken(String username, String role) {

        return Jwts.builder()
                .subject(username)           // "sub" claim: who is this token for?
                .claim("role", role)         // custom claim: store the user's role
                .issuedAt(new Date())        // "iat" claim: when was this token created?
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                // "exp" claim: when does this token expire? (now + 24 hours)
                .signWith(getSigningKey())   // Sign with our secret key (prevents tampering)
                .compact();                  // Build it into a string like "eyJhbG..."
    }

    // ── EXTRACT USERNAME FROM TOKEN ─────────────────────────────────────────
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
        // "subject" = username we stored during generateToken()
    }

    // ── EXTRACT ROLE FROM TOKEN ─────────────────────────────────────────────
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
        // Get our custom "role" claim we stored
    }

    // ── CHECK IF TOKEN IS EXPIRED ───────────────────────────────────────────
    public boolean isTokenExpired(String token) {
        Date expiry = parseClaims(token).getExpiration();
        return expiry.before(new Date());
        // If expiry date is before now → token is expired
    }

    // ── VALIDATE TOKEN ──────────────────────────────────────────────────────
    public boolean validateToken(String token, String username) {
        String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
        // Valid if: username matches AND token not expired
    }

    // ── PRIVATE HELPER: Parse the token and get all claims ─────────────────
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())  // Use our secret to verify signature
                .build()
                .parseSignedClaims(token)    // Parse the token string
                .getPayload();               // Get the body (claims) of the token
        // If token is tampered or expired → throws exception automatically

    }

}
