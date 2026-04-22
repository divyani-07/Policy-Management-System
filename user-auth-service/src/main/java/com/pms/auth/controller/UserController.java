package com.pms.auth.controller;

import com.pms.auth.dto.*;
import com.pms.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users/register — ADMIN only
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    // @PreAuthorize = Spring Security annotation — checks role BEFORE method runs
    // If not ADMIN → 403 Forbidden automatically
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserResponseDTO created = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 Created
    }

    // GET /api/users/{userId} — ADMIN or SELF
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
    // All roles can hit this endpoint BUT UserService checks SELF logic:
    // Non-admin can only see their own profile (checked inside service)
    public ResponseEntity<UserResponseDTO> getUser(
            @PathVariable Long userId,         // {userId} from URL
            Authentication authentication) {    // Spring injects current logged-in user info
        // authentication.getName() = username of who is calling this API
        // authentication.getAuthorities() = their roles

        String currentUsername = authentication.getName();
        String currentRole = authentication.getAuthorities()
                .iterator().next()                // get first authority
                .getAuthority()                   // "ROLE_ADMIN"
                .replace("ROLE_", "");            // → "ADMIN"

        return ResponseEntity.ok(userService.getUserById(userId, currentUsername, currentRole));
    }

    // PUT /api/users/{userId} — ADMIN or SELF
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('CUSTOMER')")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDTO request,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        String currentRole = authentication.getAuthorities()
                .iterator().next().getAuthority().replace("ROLE_", "");

        return ResponseEntity.ok(userService.updateUser(userId, request, currentUsername, currentRole));
    }

    // DELETE /api/users/{userId} — ADMIN only
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deactivated successfully");
        // We say "deactivated" not "deleted" because it's a soft delete
    }

    // GET /api/users?page=0&size=10 — ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            // ?page=0 from URL query param, default = 0 if not provided
            @RequestParam(defaultValue = "10") int size) {
        // ?size=10 from URL, default = 10

        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }
}