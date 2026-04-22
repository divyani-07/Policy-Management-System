//What policy-service gets back when it calls /api/auth/validate
package com.pms.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidateTokenResponseDTO {
    private boolean valid;        // true or false
    private String username;      // extracted from token
    private String role;          // extracted from token
    private Long userId;          // looked up from DB
}