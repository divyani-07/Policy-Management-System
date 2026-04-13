package com.pms.auth.controller;

import com.pms.auth.dto.LoginRequestDTO;
import com.pms.auth.dto.LoginResponseDTO;
import com.pms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController           // @Controller + @ResponseBody — returns JSON automatically
@RequestMapping("/api/auth")  // All endpoints here start with /api/auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")   // POST /api/auth/login
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request) {
        // @RequestBody = read JSON from request body
        // @Valid = run the @NotBlank validations from the DTO

        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);  // 200 OK + response body as JSON
    }
}
