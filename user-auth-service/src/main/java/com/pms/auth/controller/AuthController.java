package com.pms.auth.controller;

import com.pms.auth.dto.*;
import com.pms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login — PUBLIC
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // POST /api/auth/logout — AUTHENTICATED
    // SecurityConfig already ensures only authenticated users reach here
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader) {
        // @RequestHeader reads the "Authorization" header directly

        authService.logout(authHeader);
        return ResponseEntity.ok("Logged out successfully");
    }

    // GET /api/auth/validate — INTERNAL (called by other microservices)
    // No @PreAuthorize — other services pass their own token for validation
    @GetMapping("/validate")
    public ResponseEntity<ValidateTokenResponseDTO> validate(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // required = false → don't throw error if header missing, we handle it ourselves

        return ResponseEntity.ok(authService.validateToken(authHeader));
    }
}