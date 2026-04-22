package com.pms.auth.service;

import com.pms.auth.dto.*;
import com.pms.auth.entity.User;
import com.pms.auth.exception.*;
import com.pms.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── REGISTER ─────────────────────────────────────────────────────────
    public UserResponseDTO register(RegisterRequestDTO request) {

        log.info("[UserService] Registering new user: {}", request.getUsername());

        // Check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("username");
        }

        // Check if email already registered
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("email");
        }

        // Build the User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // encode() = BCrypt hash — "pass123" → "$2a$10$..."
        user.setRole(request.getRole());
        user.setIsActive(true);

        User saved = userRepository.save(user);
        // save() = INSERT INTO tbl_users (...) VALUES (...)
        // @PrePersist fires here → sets createdAt automatically

        log.info("[UserService] User registered with id: {}", saved.getUserId());
        return mapToDTO(saved);  // convert Entity → DTO (never return raw entity)
    }

    // ── GET USER BY ID ────────────────────────────────────────────────────
    public UserResponseDTO getUserById(Long userId, String requestingUsername, String requestingRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // SELF check: if not ADMIN, can only see their own profile
        // requestingUsername = the logged-in person's username (from JWT)
        if (!requestingRole.equals("ADMIN") && !user.getUsername().equals(requestingUsername)) {
            throw new AccessDeniedException();
            // Customer/Agent trying to view someone else's profile → 403
        }

        return mapToDTO(user);
    }

    // ── UPDATE USER ───────────────────────────────────────────────────────
    public UserResponseDTO updateUser(Long userId, UpdateUserRequestDTO request,
                                      String requestingUsername, String requestingRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // SELF check: non-admin can only update themselves
        if (!requestingRole.equals("ADMIN") && !user.getUsername().equals(requestingUsername)) {
            throw new AccessDeniedException();
        }

        // Only update fields that were actually sent (not null)
        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            // Check new email isn't taken by another user
            if (userRepository.existsByEmail(request.getEmail())
                    && !user.getEmail().equals(request.getEmail())) {
                throw new DuplicateUserException("email");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            // Re-encode the new password before saving
        }

        user.setRole(request.getRole());
        User updated = userRepository.save(user);
        // save() on existing entity = UPDATE (because userId is already set)
        // @PreUpdate fires here → sets updatedAt automatically

        log.info("[UserService] User {} updated", userId);
        return mapToDTO(updated);
    }

    // ── SOFT DELETE ───────────────────────────────────────────────────────
    public void deleteUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setIsActive(false);
        // We do NOT call userRepository.delete(user)
        // Soft delete = just mark isActive = false
        // The record stays in DB — useful for audit trail

        userRepository.save(user);
        log.info("[UserService] User {} soft-deleted", userId);
    }

    // ── LIST ALL USERS WITH PAGINATION ────────────────────────────────────
    public UserPageResponseDTO getAllUsers(int page, int size) {
        // page = which page (0 = first), size = how many per page
        // Sort.by("createdAt").descending() = newest users first

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // PageRequest.of() creates a Pageable instruction:
        // "Give me page 0, 10 items per page, sorted by createdAt DESC"

        Page<User> userPage = userRepository.findAll(pageable);
        // Spring JPA runs: SELECT * FROM tbl_users ORDER BY created_at DESC LIMIT 10 OFFSET 0

        // Convert each User entity → UserResponseDTO
        var dtos = userPage.getContent()   // getContent() = just the list of users
                .stream()
                .map(this::mapToDTO)           // this::mapToDTO = shorthand for u -> mapToDTO(u)
                .toList();

        return new UserPageResponseDTO(
                dtos,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                size
        );
    }

    // ── HELPER: Entity → DTO ──────────────────────────────────────────────
    // Private helper — converts User entity to UserResponseDTO
    // Called from every method that returns user data
    private UserResponseDTO mapToDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        // passwordHash intentionally NOT mapped
        return dto;
    }
}