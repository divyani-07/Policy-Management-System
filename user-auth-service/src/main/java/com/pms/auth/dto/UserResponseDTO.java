package com.pms.auth.dto;

import com.pms.auth.entity.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data   // Lombok: generates getters, setters, toString automatically
public class UserResponseDTO {

    private Long userId;          // user's database
    private String username;      // login username
    private String email;         // email address
    private Role role;            // ADMIN | AGENT | CUSTOMER
    private Boolean isActive;     // true = active, false = soft deleted
    private LocalDateTime createdAt;   // when was account created
    private LocalDateTime updatedAt;   // when was it last changed

    // ⚠️ NO passwordHash field here — we NEVER send password back to client
}