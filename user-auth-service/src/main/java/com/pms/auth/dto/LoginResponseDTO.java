package com.pms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor  // Lombok: generates constructor with all fields
public class LoginResponseDTO {

    private String token;      // The JWT token string
    private String role;       // "ADMIN" / "AGENT" / "CUSTOMER"
    private Long userId;
    private String expiresIn;  // "24 hours" — just for display
}
