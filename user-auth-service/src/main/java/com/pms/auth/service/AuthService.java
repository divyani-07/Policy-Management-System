package com.pms.auth.service;

import com.pms.auth.dto.LoginRequestDTO;
import com.pms.auth.dto.LoginResponseDTO;
import com.pms.auth.entity.User;
import com.pms.auth.exception.AccountDisabledException;
import com.pms.auth.exception.InvalidCredentialsException;
import com.pms.auth.repository.UserRepository;
import com.pms.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j                  // Lombok: gives us log.info(), log.error() etc. for free
@Service
@RequiredArgsConstructor  // Lombok: generates constructor for all final fields (= auto DI)
public class AuthService {

    // These are injected by Spring automatically (because of @RequiredArgsConstructor)
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;  // BCrypt encoder (defined in AppConfig)

    public LoginResponseDTO login(LoginRequestDTO request) {

        log.info("[AuthService] Login attempt for username: {}", request.getUsername());

        // Step 1: Find user in DB by username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("[AuthService] Username not found: {}", request.getUsername());
                    return new InvalidCredentialsException();
                    // If not found → throw 401 (don't say "user not found" — security risk!)
                });

        // Step 2: Check if account is active (not soft-deleted)
        if (!user.getIsActive()) {
            log.warn("[AuthService] Disabled account login attempt: {}", request.getUsername());
            throw new AccountDisabledException();
        }

        // Step 3: Verify password
        // passwordEncoder.matches("rawPassword", "storedBcryptHash")
        // BCrypt hashes are one-way — we can't decrypt, only compare
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("[AuthService] Wrong password for username: {}", request.getUsername());
            throw new InvalidCredentialsException();
        }

        // Step 4: Generate JWT token
        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole().name()  // .name() converts enum → string: ADMIN → "ADMIN"
        );

        log.info("[AuthService] Login successful for userId: {}", user.getUserId());

        // Step 5: Return response with token
        return new LoginResponseDTO(
                token,
                user.getRole().name(),
                user.getUserId(),
                "24 hours"
        );
    }
}
