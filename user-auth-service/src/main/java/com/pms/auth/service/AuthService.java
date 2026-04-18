package com.pms.auth.service;

import com.pms.auth.dto.*;
import com.pms.auth.entity.TokenBlacklist;
import com.pms.auth.entity.User;
import com.pms.auth.exception.*;
import com.pms.auth.repository.TokenBlacklistRepository;
import com.pms.auth.repository.UserRepository;
import com.pms.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // ── LOGIN (same as before) ────────────────────────────────────────────
    public LoginResponseDTO login(LoginRequestDTO request) {

        log.info("[AuthService] Login attempt: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);
        // :: is method reference — same as () -> new InvalidCredentialsException()

        if (!user.getIsActive()) {
            throw new AccountDisabledException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());

        log.info("[AuthService] Login success for userId: {}", user.getUserId());
        return new LoginResponseDTO(token, user.getRole().name(), user.getUserId(), "24 hours");
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────
    public void logout(String authHeader) {
        // authHeader = "Bearer eyJhbGci..."

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException();
        }

        String token = authHeader.substring(7);   // Remove "Bearer " prefix

        // Check token is not already blacklisted
        if (tokenBlacklistRepository.existsByToken(token)) {
            return;  // Already logged out — do nothing, not an error
        }

        // Get the token's expiry date so we know when to auto-clean it from DB
        Date expiryDate = jwtService.extractExpiration(token);
        // Convert Java Date → LocalDateTime
        LocalDateTime expiresAt = expiryDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Save to blacklist table
        TokenBlacklist blacklisted = new TokenBlacklist();
        blacklisted.setToken(token);
        blacklisted.setBlacklistedAt(LocalDateTime.now());
        blacklisted.setExpiresAt(expiresAt);

        tokenBlacklistRepository.save(blacklisted);
        // Now this token is permanently invalid even if it hasn't expired yet

        log.info("[AuthService] Token blacklisted (logout)");
    }

    // ── VALIDATE TOKEN (called by policy-service, claim-service) ─────────
    public ValidateTokenResponseDTO validateToken(String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return new ValidateTokenResponseDTO(false, null, null, null);
        }

        String token = authHeader.substring(7);

        try {
            // Check 1: Is it on the blacklist? (user logged out)
            if (tokenBlacklistRepository.existsByToken(token)) {
                log.warn("[AuthService] Blacklisted token presented for validation");
                return new ValidateTokenResponseDTO(false, null, null, null);
            }

            // Check 2: Is the token valid and not expired?
            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);

            if (jwtService.isTokenExpired(token)) {
                return new ValidateTokenResponseDTO(false, null, null, null);
            }

            // Check 3: Does the user still exist and is still active?
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null || !user.getIsActive()) {
                return new ValidateTokenResponseDTO(false, null, null, null);
            }

            log.debug("[AuthService] Token validated for user: {}", username);
            return new ValidateTokenResponseDTO(true, username, role, user.getUserId());

        } catch (Exception e) {
            // Token is malformed or tampered
            log.warn("[AuthService] Token validation failed: {}", e.getMessage());
            return new ValidateTokenResponseDTO(false, null, null, null);
        }
    }
}